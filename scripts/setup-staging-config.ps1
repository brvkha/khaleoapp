param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp",
    [string]$AwsRegion = "ap-southeast-1",
    [string]$Project = "khaleoapp",
    [string]$Route53ZoneName = "khaleoshop.click",
    [string]$FrontendDomain = "staging.khaleoshop.click",
    [string]$ApiDomain = "api-staging.khaleoshop.click"
)

$ErrorActionPreference = "Stop"

function Read-Required([string]$Prompt) {
    do {
        $value = Read-Host $Prompt
    } while ([string]::IsNullOrWhiteSpace($value))
    return $value.Trim()
}

Write-Host "=== KhaLeo Staging Config Wizard ==="
Write-Host "This script writes non-sensitive config files and a local secrets template."

$tfStateBucket = Read-Required "Terraform state S3 bucket name (global unique)"
$dbPassword = Read-Required "Staging DB password"
$ec2Host = Read-Required "Staging EC2 public host/IP"
$rdsHost = Read-Required "Staging RDS endpoint hostname"
$jwtSecret = Read-Required "JWT secret for staging"
$dockerHubUser = Read-Required "Docker Hub username"
$dockerHubToken = Read-Required "Docker Hub token/password"
$ec2SshUser = Read-Required "EC2 SSH user (example: ec2-user)"
$ec2SshPrivateKeyPath = Read-Required "Path to EC2 SSH private key file (.pem)"
$s3BucketStaging = Read-Required "S3 bucket name for staging frontend"
$cfDistIdStaging = Read-Required "CloudFront distribution id for staging"
$awsAccessKeyId = Read-Required "AWS access key id (for GitHub Actions)"
$awsSecretAccessKey = Read-Required "AWS secret access key (for GitHub Actions)"

if (-not (Test-Path $ec2SshPrivateKeyPath)) {
    throw "EC2 SSH private key path does not exist: $ec2SshPrivateKeyPath"
}

$bootstrapTfvars = @"
aws_region           = "$AwsRegion"
project              = "$Project"
tf_state_bucket_name = "$tfStateBucket"
tf_lock_table_name   = "khaleoapp-terraform-lock"
"@
Set-Content -Path "$RepoRoot\infra\terraform\bootstrap\terraform.tfvars" -Value $bootstrapTfvars -NoNewline -Encoding UTF8

$backendStagingHcl = @"
bucket         = "$tfStateBucket"
key            = "khaleoapp/staging/terraform.tfstate"
region         = "$AwsRegion"
dynamodb_table = "khaleoapp-terraform-lock"
encrypt        = true
"@
Set-Content -Path "$RepoRoot\infra\terraform\app\backend-staging.hcl" -Value $backendStagingHcl -NoNewline -Encoding UTF8

$stagingTfvars = @"
aws_region                    = "$AwsRegion"
project                       = "$Project"
environment                   = "staging"
vpc_cidr                      = "10.0.0.0/16"
public_subnet_cidr            = "10.0.1.0/24"
private_subnet_a_cidr         = "10.0.2.0/24"
private_subnet_b_cidr         = "10.0.3.0/24"
route53_zone_name             = "$Route53ZoneName"
frontend_domain_name          = "$FrontendDomain"
api_domain_name               = "$ApiDomain"
ec2_instance_type             = "t3.micro"
db_instance_class             = "db.t3.micro"
db_name                       = "khaleoapp"
db_username                   = "app_user"
db_password                   = "$dbPassword"
db_backup_retention_days      = 7
allowed_http_cidrs            = ["0.0.0.0/0"]
enable_frontend_custom_domain = true
"@
Set-Content -Path "$RepoRoot\infra\terraform\app\env\staging.tfvars" -Value $stagingTfvars -NoNewline -Encoding UTF8

$frontendEnvStaging = @"
VITE_API_BASE_URL=https://$ApiDomain
VITE_APP_ENV=staging
VITE_MEDIA_MAX_MB=5
"@
Set-Content -Path "$RepoRoot\frontend\.env.staging" -Value $frontendEnvStaging -NoNewline -Encoding UTF8

$ec2SshPrivateKey = Get-Content -Path $ec2SshPrivateKeyPath -Raw

$githubSecretsLocal = @"
AWS_ACCESS_KEY_ID=$awsAccessKeyId
AWS_SECRET_ACCESS_KEY=$awsSecretAccessKey
S3_BUCKET_STAGING=$s3BucketStaging
CF_DIST_ID_STAGING=$cfDistIdStaging
DOCKERHUB_USERNAME=$dockerHubUser
DOCKERHUB_TOKEN=$dockerHubToken
EC2_HOST_STAGING=$ec2Host
EC2_SSH_USER=$ec2SshUser
EC2_SSH_PRIVATE_KEY=$ec2SshPrivateKey
RDS_HOST_STAGING=$rdsHost
RDS_DB_NAME=khaleoapp
RDS_DB_USER=app_user
RDS_DB_PASSWORD=$dbPassword
JWT_SECRET_STAGING=$jwtSecret
"@
Set-Content -Path "$RepoRoot\docs\manual-e2e\staging-secrets.local.env" -Value $githubSecretsLocal -NoNewline -Encoding UTF8

Write-Host "Generated files:"
Write-Host "- infra/terraform/bootstrap/terraform.tfvars"
Write-Host "- infra/terraform/app/backend-staging.hcl"
Write-Host "- infra/terraform/app/env/staging.tfvars"
Write-Host "- frontend/.env.staging"
Write-Host "- docs/manual-e2e/staging-secrets.local.env"
Write-Host ""
Write-Host "Next: run scripts\set-github-staging-secrets.ps1 to push secrets via gh CLI."

