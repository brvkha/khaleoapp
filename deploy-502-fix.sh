#!/bin/bash

################################################################################
# 502 Gateway Error - Automated Fix Deployment Script
#
# This script automates the database schema fix and container restart.
# Usage: bash ./deploy-502-fix.sh <INSTANCE_ID> <REGION>
#
# Prerequisites:
#   - AWS CLI configured with appropriate permissions
#   - jq installed (for JSON parsing)
#   - DB credentials available in runtime-secrets.env on EC2
#
# Time: ~10 minutes
# Risk: LOW (schema addition only, rollback available)
################################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
INSTANCE_ID="${1:-}"
REGION="${2:-ap-southeast-1}"
RDS_HOST="khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com"
RDS_DB="khaleoapp"
DOCKER_IMAGE="brvkha/khaleoapp:latest"
CONTAINER_NAME="khaleo-backend"
CONTAINER_PORT="8080"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

print_banner() {
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║  502 Gateway Error - Automated Fix Deployment                 ║"
    echo "║  Status: EXECUTING                                            ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
}

print_completion() {
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║  502 Gateway Error - Fix Complete ✓                           ║"
    echo "║  Status: READY FOR VERIFICATION                               ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
}

# Validate inputs
if [ -z "$INSTANCE_ID" ]; then
    log_error "Usage: $0 <INSTANCE_ID> [REGION]"
fi

print_banner

log_info "Configuration:"
log_info "  Instance ID: $INSTANCE_ID"
log_info "  Region: $REGION"
log_info "  RDS Host: $RDS_HOST"
log_info "  Container: $CONTAINER_NAME"

# Phase 1: Database Schema Update
log_info ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "PHASE 1: Database Schema Update (Adding parent_id column)"
log_info "═══════════════════════════════════════════════════════════════"

log_info "Executing ALTER TABLE commands on RDS..."

# Create SQL commands
SQL_COMMANDS='
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
SHOW CREATE TABLE decks\G
SELECT * FROM flyway_schema_history WHERE version = '\''V20260416_014'\'';
'

# Execute via SSM
log_info "Sending SSM command to EC2 instance..."
COMMAND_ID=$(aws ssm send-command \
    --instance-ids "$INSTANCE_ID" \
    --region "$REGION" \
    --document-name "AWS-RunShellScript" \
    --parameters commands="[
        \"export DB_USER=\$(grep DB_USERNAME /opt/khaleo/flashcard-backend/runtime-secrets.env | cut -d= -f2)\",
        \"export DB_PASS=\$(grep DB_PASSWORD /opt/khaleo/flashcard-backend/runtime-secrets.env | cut -d= -f2)\",
        \"mysql -h $RDS_HOST -u\\${DB_USER} -p\\${DB_PASS} $RDS_DB <<< \\\"$SQL_COMMANDS\\\"\"
    ]" \
    --query "Command.CommandId" \
    --output text 2>/dev/null || echo "")

if [ -z "$COMMAND_ID" ]; then
    log_error "Failed to create SSM command"
fi

log_info "SSM Command ID: $COMMAND_ID"
log_info "Waiting for command to complete..."

# Wait for command completion
RETRY_COUNT=0
MAX_RETRIES=60
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    STATUS=$(aws ssm list-command-invocations \
        --command-id "$COMMAND_ID" \
        --region "$REGION" \
        --query "CommandInvocations[0].Status" \
        --output text 2>/dev/null || echo "Pending")

    case "$STATUS" in
        "Success")
            log_success "Database schema updated successfully"
            break
            ;;
        "Failed"|"Cancelled"|"TimedOut")
            log_error "Database update failed with status: $STATUS"
            ;;
        *)
            RETRY_COUNT=$((RETRY_COUNT + 1))
            echo -ne "\r  [$RETRY_COUNT/60] Status: $STATUS"
            sleep 5
            ;;
    esac
done

# Phase 2: Container Restart
log_info ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "PHASE 2: Backend Container Restart"
log_info "═══════════════════════════════════════════════════════════════"

log_info "Stopping and removing old container..."
STOP_COMMAND_ID=$(aws ssm send-command \
    --instance-ids "$INSTANCE_ID" \
    --region "$REGION" \
    --document-name "AWS-RunShellScript" \
    --parameters commands="[
        \"docker stop $CONTAINER_NAME || true\",
        \"docker rm $CONTAINER_NAME || true\"
    ]" \
    --query "Command.CommandId" \
    --output text)

log_info "SSM Command ID: $STOP_COMMAND_ID"
log_info "Waiting for container stop..."

# Wait for stop command
RETRY_COUNT=0
while [ $RETRY_COUNT -lt 30 ]; do
    STATUS=$(aws ssm list-command-invocations \
        --command-id "$STOP_COMMAND_ID" \
        --region "$REGION" \
        --query "CommandInvocations[0].Status" \
        --output text 2>/dev/null || echo "Pending")

    if [ "$STATUS" = "Success" ]; then
        log_success "Container stopped"
        break
    elif [[ "$STATUS" =~ ^(Failed|Cancelled|TimedOut)$ ]]; then
        log_warning "Container stop returned: $STATUS (continuing...)"
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -ne "\r  [$RETRY_COUNT/30] Status: $STATUS"
    sleep 2
done

log_info "Starting new container..."

# Prepare container start command
START_COMMAND_ID=$(aws ssm send-command \
    --instance-ids "$INSTANCE_ID" \
    --region "$REGION" \
    --document-name "AWS-RunShellScript" \
    --parameters commands="[
        \"docker pull $DOCKER_IMAGE\",
        \"docker run -d --restart unless-stopped --name $CONTAINER_NAME -p $CONTAINER_PORT:$CONTAINER_PORT --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env -e SPRING_PROFILES_ACTIVE=production -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate $DOCKER_IMAGE\",
        \"sleep 20\",
        \"docker ps -a --filter name=$CONTAINER_NAME\",
        \"echo 'Container Logs:'\",
        \"docker logs --tail 50 $CONTAINER_NAME\"
    ]" \
    --query "Command.CommandId" \
    --output text)

log_info "SSM Command ID: $START_COMMAND_ID"
log_info "Waiting for container to start..."

# Wait for start command
RETRY_COUNT=0
while [ $RETRY_COUNT -lt 120 ]; do
    STATUS=$(aws ssm list-command-invocations \
        --command-id "$START_COMMAND_ID" \
        --region "$REGION" \
        --query "CommandInvocations[0].Status" \
        --output text 2>/dev/null || echo "Pending")

    if [ "$STATUS" = "Success" ]; then
        log_success "Container started"
        break
    elif [[ "$STATUS" =~ ^(Failed|Cancelled|TimedOut)$ ]]; then
        log_error "Container start failed: $STATUS"
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -ne "\r  [$RETRY_COUNT/120] Status: $STATUS"
    sleep 5
done

# Phase 3: Verification
log_info ""
log_info "═══════════════════════════════════════════════════════════════"
log_info "PHASE 3: Health Verification"
log_info "═══════════════════════════════════════════════════════════════"

log_info "Checking container health..."
HEALTH_COMMAND_ID=$(aws ssm send-command \
    --instance-ids "$INSTANCE_ID" \
    --region "$REGION" \
    --document-name "AWS-RunShellScript" \
    --parameters commands="[
        \"echo 'Container Status:'\",
        \"docker ps -a --filter name=$CONTAINER_NAME\",
        \"echo ''\",
        \"echo 'Health Check:'\",
        \"curl -s http://127.0.0.1:$CONTAINER_PORT/actuator/health | jq . || echo 'Health check failed'\",
        \"echo ''\",
        \"echo 'Application Logs (last 50 lines):'\",
        \"docker logs --tail 50 $CONTAINER_NAME 2>&1 | grep -E '(error|schema|validation|started)' || echo 'No errors found'\"
    ]" \
    --query "Command.CommandId" \
    --output text)

log_info "SSM Command ID: $HEALTH_COMMAND_ID"
log_info "Waiting for health check..."

# Wait for health check
RETRY_COUNT=0
while [ $RETRY_COUNT -lt 60 ]; do
    STATUS=$(aws ssm list-command-invocations \
        --command-id "$HEALTH_COMMAND_ID" \
        --region "$REGION" \
        --query "CommandInvocations[0].Status" \
        --output text 2>/dev/null || echo "Pending")

    if [ "$STATUS" = "Success" ]; then
        log_success "Health verification complete"
        break
    elif [[ "$STATUS" =~ ^(Failed|Cancelled|TimedOut)$ ]]; then
        log_warning "Health check returned: $STATUS"
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -ne "\r  [$RETRY_COUNT/60] Status: $STATUS"
    sleep 2
done

# Get and display output
log_info "Retrieving verification output..."
HEALTH_OUTPUT=$(aws ssm get-command-invocation \
    --command-id "$HEALTH_COMMAND_ID" \
    --instance-id "$INSTANCE_ID" \
    --region "$REGION" \
    --query "StandardOutputContent" \
    --output text)

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "Health Check Output:"
echo "═══════════════════════════════════════════════════════════════"
echo "$HEALTH_OUTPUT"
echo ""

# Final Summary
print_completion

log_info "Summary:"
log_info "  ✓ Database schema updated (parent_id column added)"
log_info "  ✓ Container restarted with production profile"
log_info "  ✓ Health verification initiated"
log_info ""
log_info "Next Steps:"
log_info "  1. Verify the health check output above"
log_info "  2. Test API endpoints"
log_info "  3. Monitor application logs for errors"
log_info "  4. Confirm 502 error is resolved"
log_info ""
log_info "If issues occur, see troubleshooting guide in QUICK_FIX_CHECKLIST.md"

log_success "Deployment script completed!"

