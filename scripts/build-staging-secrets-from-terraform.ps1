param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp",
    [string]$TerraformDir = "C:\Workspace\FPT\github\khaleoapp\infra\terraform\app"
)

$ErrorActionPreference = "Stop"

function Read-Required([string]$Prompt) {
    do {
        $value = Read-Host $Prompt
    } while ([string]::IsNullOrWhiteSpace($value))
    return $value.Trim()
}

Set-Location $TerraformDir

# Requires backend-staging.hcl + staging.tfvars already prepared and applied.
$tfJson = terraform output -json | Out-String
if ([string]::IsNullOrWhiteSpace($tfJson)) {
    throw "No Terraform outputs found. Run apply first."
}

$tf = $tfJson | ConvertFrom-Json

$s3Bucket = $tf.frontend_bucket_name.value
$cfDistId = $tf.cloudfront_distribution_id.value
$apiPublicIp = $tf.api_public_ip.value
$rdsEndpoint = $tf.rds_endpoint.value
$rdsHost = ($rdsEndpoint -split ":")[0]

$awsAccessKeyId = aws configure get aws_access_key_id
if ([string]::IsNullOrWhiteSpace($awsAccessKeyId)) {
    $awsAccessKeyId = Read-Required "AWS_ACCESS_KEY_ID"
}

$awsSecretAccessKey = Read-Required "AWS_SECRET_ACCESS_KEY"
$dockerUser = Read-Required "DOCKERHUB_USERNAME"
$dockerToken = Read-Required "DOCKERHUB_TOKEN"
$ec2SshUser = Read-Required "EC2_SSH_USER (usually ec2-user)"
$ec2SshPrivateKeyPath = Read-Required "EC2_SSH_PRIVATE_KEY_PATH (.pem)"
$jwtSecret = Read-Required "JWT_SECRET_STAGING"

if (-not (Test-Path $ec2SshPrivateKeyPath)) {
    throw "SSH private key not found: $ec2SshPrivateKeyPath"
}

$secretsFile = "$RepoRoot\docs\manual-e2e\staging-secrets.local.env"
@(
    "AWS_ACCESS_KEY_ID=$awsAccessKeyId",
    "AWS_SECRET_ACCESS_KEY=$awsSecretAccessKey",
    "S3_BUCKET_STAGING=$s3Bucket",
    "CF_DIST_ID_STAGING=$cfDistId",
    "DOCKERHUB_USERNAME=$dockerUser",
    "DOCKERHUB_TOKEN=$dockerToken",
    "EC2_HOST_STAGING=$apiPublicIp",
    "EC2_SSH_USER=$ec2SshUser",
    "EC2_SSH_PRIVATE_KEY_PATH=$ec2SshPrivateKeyPath",
    "RDS_HOST_STAGING=$rdsHost",
    "RDS_DB_NAME=khaleoapp",
    "RDS_DB_USER=app_user",
    "RDS_DB_PASSWORD=",
    "JWT_SECRET_STAGING=$jwtSecret"
) | Set-Content -Path $secretsFile -Encoding UTF8

Write-Host "Wrote: $secretsFile"
Write-Host "Detected from Terraform outputs:"
Write-Host "- S3_BUCKET_STAGING=$s3Bucket"
Write-Host "- CF_DIST_ID_STAGING=$cfDistId"
Write-Host "- EC2_HOST_STAGING=$apiPublicIp"
Write-Host "- RDS_HOST_STAGING=$rdsHost"
Write-Host "Next: run scripts/set-github-staging-secrets.ps1"

