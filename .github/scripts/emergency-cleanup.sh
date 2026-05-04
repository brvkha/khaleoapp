#!/usr/bin/env bash
set -euo pipefail

log() {
  printf '[cleanup] %s\n' "$*"
}

need_env() {
  local name="$1"
  local value="${!name:-}"
  if [[ -z "$value" ]]; then
    log "Missing required environment variable: $name"
    exit 1
  fi
}

aws_wait_for() {
  local seconds="${1:-2}"
  sleep "$seconds"
}

AWS_REGION="${AWS_REGION:-ap-southeast-1}"
PROJECT_NAME="${PROJECT_NAME:-khaleoapp}"
ENVIRONMENT="${ENVIRONMENT:-prod}"
HOSTED_ZONE_ID="${HOSTED_ZONE_ID:-Z09479913APS5DRNMZYZ3}"
FRONTEND_BUCKET="${FRONTEND_BUCKET:-khaleoapp-prod-frontend-817888697629}"
CLOUDFRONT_DISTRIBUTION_ID="${CLOUDFRONT_DISTRIBUTION_ID:-E3SCOR5XMR0TOY}"
EC2_INSTANCE_ID="${EC2_INSTANCE_ID:-i-0fa666265f036c141}"
RDS_INSTANCE_ID="${RDS_INSTANCE_ID:-khaleoapp-prod-db}"
TF_STATE_BUCKET="${TF_STATE_BUCKET:-khaleoapp-tf-state-20260410}"
TF_STATE_KEY="${TF_STATE_KEY:-khaleoapp/prod/terraform.tfstate}"
TF_LOCK_TABLE="${TF_LOCK_TABLE:-khaleoapp-terraform-lock}"
DESTROY_BACKEND_STATE="${DESTROY_BACKEND_STATE:-NO}"
EC2_ROLE_NAME="${EC2_ROLE_NAME:-khaleoapp-prod-ec2-ssm-role}"
EC2_INSTANCE_PROFILE_NAME="${EC2_INSTANCE_PROFILE_NAME:-khaleoapp-prod-instance-profile}"
SSM_DOCUMENT_NAME="${SSM_DOCUMENT_NAME:-khaleoapp-prod-nginx-tls-bootstrap}"
FRONTEND_ACM_DOMAIN="${FRONTEND_ACM_DOMAIN:-khaleoshop.click}"
FRONTEND_ACM_ALT_DOMAIN="${FRONTEND_ACM_ALT_DOMAIN:-www.khaleoshop.click}"
FRONTEND_OAC_NAME="${FRONTEND_OAC_NAME:-khaleoapp-prod-oac}"
SSM_SESSION_LOG_GROUP="${SSM_SESSION_LOG_GROUP:-/aws/ssm/khaleoapp/session}"

VPC_ID=""
EIP_ALLOCATION_ID=""

cleanup_route53() {
  log "Cleaning Route53 hosted zone $HOSTED_ZONE_ID"

  local records_json
  if ! records_json=$(aws route53 list-resource-record-sets --hosted-zone-id "$HOSTED_ZONE_ID" --output json); then
    log "Hosted zone not found or inaccessible, skipping Route53 cleanup"
    return 0
  fi

  local change_batch
  change_batch=$(echo "$records_json" | jq -c '{Changes: [.ResourceRecordSets[] | select(.Type != "NS" and .Type != "SOA") | {Action: "DELETE", ResourceRecordSet: .}] }')
  local change_count
  change_count=$(echo "$change_batch" | jq '.Changes | length')

  if [[ "$change_count" -gt 0 ]]; then
    local response change_id
    response=$(aws route53 change-resource-record-sets --hosted-zone-id "$HOSTED_ZONE_ID" --change-batch "$change_batch")
    change_id=$(echo "$response" | jq -r '.ChangeInfo.Id' | sed 's|.*/||')
    if [[ -n "$change_id" && "$change_id" != "null" ]]; then
      aws route53 wait resource-record-sets-changed --id "$change_id" || true
    fi
    log "Route53 records deleted"
  else
    log "No Route53 records to delete"
  fi

  aws route53 delete-hosted-zone --id "$HOSTED_ZONE_ID" >/dev/null
  log "Hosted zone deleted"
}

cleanup_cloudfront() {
  log "Cleaning CloudFront distribution $CLOUDFRONT_DISTRIBUTION_ID"

  local config_json etag config enabled update_response new_etag
  if ! config_json=$(aws cloudfront get-distribution-config --id "$CLOUDFRONT_DISTRIBUTION_ID"); then
    log "CloudFront distribution not found, skipping"
    return 0
  fi

  etag=$(echo "$config_json" | jq -r '.ETag')
  config=$(echo "$config_json" | jq '.DistributionConfig')
  enabled=$(echo "$config" | jq -r '.Enabled')

  if [[ "$enabled" == "true" ]]; then
    log "Disabling CloudFront distribution"
    update_response=$(aws cloudfront update-distribution --id "$CLOUDFRONT_DISTRIBUTION_ID" --if-match "$etag" --distribution-config "$(echo "$config" | jq '.Enabled = false')")
    new_etag=$(echo "$update_response" | jq -r '.ETag')
    aws cloudfront wait distribution-deployed --id "$CLOUDFRONT_DISTRIBUTION_ID" || true
    etag="$new_etag"
  else
    log "CloudFront distribution already disabled"
  fi

  # Refresh ETag before delete in case the update response is stale.
  config_json=$(aws cloudfront get-distribution-config --id "$CLOUDFRONT_DISTRIBUTION_ID")
  etag=$(echo "$config_json" | jq -r '.ETag')

  aws cloudfront delete-distribution --id "$CLOUDFRONT_DISTRIBUTION_ID" --if-match "$etag" >/dev/null
  log "CloudFront distribution deleted"
}

cleanup_cloudfront_oac() {
  log "Cleaning CloudFront origin access controls"

  local oac_list oac_id oac_config oac_etag
  if ! oac_list=$(aws cloudfront list-origin-access-controls); then
    log "No CloudFront OAC list available, skipping"
    return 0
  fi

  oac_id=$(echo "$oac_list" | jq -r ".OriginAccessControlList.Items[]? | select(.Name == \"$FRONTEND_OAC_NAME\") | .Id" | head -n 1)
  if [[ -z "$oac_id" || "$oac_id" == "null" ]]; then
    log "No matching OAC found"
    return 0
  fi

  oac_config=$(aws cloudfront get-origin-access-control-config --id "$oac_id")
  oac_etag=$(echo "$oac_config" | jq -r '.ETag')
  aws cloudfront delete-origin-access-control --id "$oac_id" --if-match "$oac_etag" >/dev/null
  log "CloudFront OAC deleted"
}

cleanup_acm() {
  log "Cleaning ACM certificate for frontend domain"

  local cert_arns
  cert_arns=$(aws acm list-certificates --region us-east-1 --query "CertificateSummaryList[?DomainName=='$FRONTEND_ACM_DOMAIN'].CertificateArn" --output text || true)
  if [[ -z "$cert_arns" ]]; then
    log "No matching ACM cert found"
    return 0
  fi

  for arn in $cert_arns; do
    aws acm delete-certificate --region us-east-1 --certificate-arn "$arn" >/dev/null || true
    log "Deleted ACM certificate: $arn"
  done
}

cleanup_s3_bucket() {
  local bucket="$1"
  log "Emptying S3 bucket: $bucket"

  local versions_json payload_count payload uploads_json upload_count
  versions_json=$(aws s3api list-object-versions --bucket "$bucket" --output json --query '{Objects: Versions[].{Key:Key,VersionId:VersionId}, DeleteMarkers: DeleteMarkers[].{Key:Key,VersionId:VersionId}}' || echo '{"Objects":[],"DeleteMarkers":[]}')
  payload_count=$(echo "$versions_json" | jq '([.Objects[]?, .DeleteMarkers[]?] | length)')

  if [[ "$payload_count" -gt 0 ]]; then
    payload=$(echo "$versions_json" | jq -c '{Objects: ([.Objects[]?, .DeleteMarkers[]?] | map({Key, VersionId})), Quiet: false}')
    aws s3api delete-objects --bucket "$bucket" --delete "$payload" >/dev/null
  fi

  aws s3 rm "s3://$bucket" --recursive >/dev/null 2>&1 || true

  uploads_json=$(aws s3api list-multipart-uploads --bucket "$bucket" --output json --query 'Uploads[].{Key:Key,UploadId:UploadId}' || echo '[]')
  upload_count=$(echo "$uploads_json" | jq 'length')
  if [[ "$upload_count" -gt 0 ]]; then
    echo "$uploads_json" | jq -c '.[]' | while read -r upload; do
      local key upload_id
      key=$(echo "$upload" | jq -r '.Key')
      upload_id=$(echo "$upload" | jq -r '.UploadId')
      aws s3api abort-multipart-upload --bucket "$bucket" --key "$key" --upload-id "$upload_id" >/dev/null 2>&1 || true
    done
  fi

  aws s3api delete-bucket-policy --bucket "$bucket" >/dev/null 2>&1 || true
  aws s3api delete-bucket --bucket "$bucket" >/dev/null 2>&1 || aws s3 rb "s3://$bucket" --force >/dev/null 2>&1 || true
  log "Bucket cleanup done: $bucket"
}

cleanup_rds() {
  log "Deleting RDS instance $RDS_INSTANCE_ID"

  if ! aws rds describe-db-instances --db-instance-identifier "$RDS_INSTANCE_ID" >/dev/null 2>&1; then
    log "RDS instance not found, skipping"
    return 0
  fi

  aws rds modify-db-instance --db-instance-identifier "$RDS_INSTANCE_ID" --no-deletion-protection --apply-immediately >/dev/null 2>&1 || true
  aws rds delete-db-instance --db-instance-identifier "$RDS_INSTANCE_ID" --skip-final-snapshot --delete-automated-backups >/dev/null
  aws rds wait db-instance-deleted --db-instance-identifier "$RDS_INSTANCE_ID" || true
  log "RDS instance deleted"
}

cleanup_ec2() {
  log "Deleting EC2 instance $EC2_INSTANCE_ID"

  if ! aws ec2 describe-instances --instance-ids "$EC2_INSTANCE_ID" >/dev/null 2>&1; then
    log "EC2 instance not found, skipping"
    return 0
  fi

  EIP_ALLOCATION_ID=$(aws ec2 describe-addresses --filters Name=instance-id,Values="$EC2_INSTANCE_ID" --query 'Addresses[0].AllocationId' --output text 2>/dev/null || true)
  [[ "$EIP_ALLOCATION_ID" == "None" ]] && EIP_ALLOCATION_ID=""

  aws ec2 terminate-instances --instance-ids "$EC2_INSTANCE_ID" >/dev/null
  aws ec2 wait instance-terminated --instance-ids "$EC2_INSTANCE_ID" || true

  if [[ -n "$EIP_ALLOCATION_ID" ]]; then
    aws ec2 release-address --allocation-id "$EIP_ALLOCATION_ID" >/dev/null 2>&1 || true
    log "Released EIP allocation: $EIP_ALLOCATION_ID"
  fi

  log "EC2 instance terminated"
}

cleanup_iam_and_ssm() {
  log "Cleaning IAM / SSM artifacts"

  aws ssm delete-document --name "$SSM_DOCUMENT_NAME" >/dev/null 2>&1 || true
  aws logs delete-log-group --log-group-name "$SSM_SESSION_LOG_GROUP" >/dev/null 2>&1 || true

  local attached_policies
  attached_policies=$(aws iam list-attached-role-policies --role-name "$EC2_ROLE_NAME" --query 'AttachedPolicies[].PolicyArn' --output text 2>/dev/null || true)
  if [[ -n "$attached_policies" ]]; then
    for policy_arn in $attached_policies; do
      aws iam detach-role-policy --role-name "$EC2_ROLE_NAME" --policy-arn "$policy_arn" >/dev/null 2>&1 || true
    done
  fi

  aws iam remove-role-from-instance-profile --instance-profile-name "$EC2_INSTANCE_PROFILE_NAME" --role-name "$EC2_ROLE_NAME" >/dev/null 2>&1 || true
  aws iam delete-instance-profile --instance-profile-name "$EC2_INSTANCE_PROFILE_NAME" >/dev/null 2>&1 || true
  aws iam delete-role --role-name "$EC2_ROLE_NAME" >/dev/null 2>&1 || true
}

cleanup_network() {
  log "Cleaning network resources"

  if [[ -z "$VPC_ID" ]]; then
    VPC_ID=$(aws ec2 describe-instances --instance-ids "$EC2_INSTANCE_ID" --query 'Reservations[0].Instances[0].VpcId' --output text 2>/dev/null || true)
  fi
  [[ "$VPC_ID" == "None" ]] && VPC_ID=""
  if [[ -z "$VPC_ID" ]]; then
    log "Unable to determine VPC ID, skipping network cleanup"
    return 0
  fi

  # Delete any leftover ENIs in the VPC.
  local eni_ids
  eni_ids=$(aws ec2 describe-network-interfaces --filters Name=vpc-id,Values="$VPC_ID" Name=status,Values=available --query 'NetworkInterfaces[].NetworkInterfaceId' --output text 2>/dev/null || true)
  if [[ -n "$eni_ids" ]]; then
    for eni_id in $eni_ids; do
      aws ec2 delete-network-interface --network-interface-id "$eni_id" >/dev/null 2>&1 || true
    done
  fi

  local sg_ids
  sg_ids=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values="$VPC_ID" Name=tag:Project,Values="$PROJECT_NAME" Name=tag:Environment,Values="$ENVIRONMENT" --query 'SecurityGroups[?GroupName!=`default`].GroupId' --output text 2>/dev/null || true)
  if [[ -n "$sg_ids" ]]; then
    for sg_id in $sg_ids; do
      aws ec2 delete-security-group --group-id "$sg_id" >/dev/null 2>&1 || true
    done
  fi

  local subnet_ids
  subnet_ids=$(aws ec2 describe-subnets --filters Name=vpc-id,Values="$VPC_ID" Name=tag:Project,Values="$PROJECT_NAME" Name=tag:Environment,Values="$ENVIRONMENT" --query 'Subnets[].SubnetId' --output text 2>/dev/null || true)
  if [[ -n "$subnet_ids" ]]; then
    for subnet_id in $subnet_ids; do
      aws ec2 delete-subnet --subnet-id "$subnet_id" >/dev/null 2>&1 || true
    done
  fi

  local route_table_ids
  route_table_ids=$(aws ec2 describe-route-tables --filters Name=vpc-id,Values="$VPC_ID" Name=tag:Project,Values="$PROJECT_NAME" Name=tag:Environment,Values="$ENVIRONMENT" --query 'RouteTables[].RouteTableId' --output text 2>/dev/null || true)
  if [[ -n "$route_table_ids" ]]; then
    for rt_id in $route_table_ids; do
      local assoc_ids
      assoc_ids=$(aws ec2 describe-route-tables --route-table-ids "$rt_id" --query 'RouteTables[0].Associations[?Main==`false`].RouteTableAssociationId' --output text 2>/dev/null || true)
      if [[ -n "$assoc_ids" ]]; then
        for assoc_id in $assoc_ids; do
          aws ec2 disassociate-route-table --association-id "$assoc_id" >/dev/null 2>&1 || true
        done
      fi
      aws ec2 delete-route-table --route-table-id "$rt_id" >/dev/null 2>&1 || true
    done
  fi

  local igw_ids
  igw_ids=$(aws ec2 describe-internet-gateways --filters Name=attachment.vpc-id,Values="$VPC_ID" Name=tag:Project,Values="$PROJECT_NAME" --query 'InternetGateways[].InternetGatewayId' --output text 2>/dev/null || true)
  if [[ -n "$igw_ids" ]]; then
    for igw_id in $igw_ids; do
      aws ec2 detach-internet-gateway --internet-gateway-id "$igw_id" --vpc-id "$VPC_ID" >/dev/null 2>&1 || true
      aws ec2 delete-internet-gateway --internet-gateway-id "$igw_id" >/dev/null 2>&1 || true
    done
  fi

  local vpc_endpoint_ids
  vpc_endpoint_ids=$(aws ec2 describe-vpc-endpoints --filters Name=vpc-id,Values="$VPC_ID" Name=tag:Project,Values="$PROJECT_NAME" --query 'VpcEndpoints[].VpcEndpointId' --output text 2>/dev/null || true)
  if [[ -n "$vpc_endpoint_ids" ]]; then
    aws ec2 delete-vpc-endpoints --vpc-endpoint-ids $vpc_endpoint_ids >/dev/null 2>&1 || true
  fi

  aws ec2 delete-vpc --vpc-id "$VPC_ID" >/dev/null 2>&1 || true
  log "Network cleanup attempted for VPC: $VPC_ID"
}

cleanup_backend() {
  if [[ "$DESTROY_BACKEND_STATE" != "DESTROY_BACKEND" ]]; then
    log "Backend destruction not requested; preserving Terraform backend"
    return 0
  fi

  log "Deleting Terraform backend bucket and lock table"
  cleanup_s3_bucket "$TF_STATE_BUCKET"
  aws dynamodb delete-table --table-name "$TF_LOCK_TABLE" >/dev/null 2>&1 || true
  log "Terraform backend cleanup complete"
}

main() {
  need_env AWS_ACCESS_KEY_ID
  need_env AWS_SECRET_ACCESS_KEY

  cleanup_route53
  cleanup_cloudfront
  cleanup_cloudfront_oac
  cleanup_acm
  cleanup_s3_bucket "$FRONTEND_BUCKET"
  cleanup_rds
  cleanup_ec2
  cleanup_iam_and_ssm
  cleanup_network
  cleanup_backend

  log "Emergency cleanup finished"
}

main "$@"



