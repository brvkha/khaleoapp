param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp",
    [string]$TerraformDir = "C:\Workspace\FPT\github\khaleoapp\infra\terraform\app",
    [string]$BootstrapTfvarsPath = "C:\Workspace\FPT\github\khaleoapp\infra\terraform\bootstrap\terraform.tfvars"
)

$ErrorActionPreference = "Stop"

function Read-Required([string]$Prompt) {
    do {
        $value = Read-Host $Prompt
    } while ([string]::IsNullOrWhiteSpace($value))
    return $value.Trim()
}

function Get-TfvarsValue([string]$Path, [string]$Key) {
    if (-not (Test-Path $Path)) {
        return ""
    }

    $pattern = '^[\s]*' + [Regex]::Escape($Key) + '[\s]*=[\s]*"([^"]+)"'
    foreach ($line in (Get-Content -Path $Path)) {
        if ($line -match $pattern) {
            return $Matches[1]
        }
    }

    return ""
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
$backendInstanceId = $tf.backend_instance_id.value
$rdsEndpoint = $tf.rds_endpoint.value
$rdsHost = ($rdsEndpoint -split ":")[0]
$apiDomain = if ($tf.staging_api_url.value) { ($tf.staging_api_url.value -replace '^https://', '') } else { "api-staging.khaleoshop.click" }
$tfStateBucket = Get-TfvarsValue -Path $BootstrapTfvarsPath -Key "tf_state_bucket_name"
if ([string]::IsNullOrWhiteSpace($tfStateBucket)) {
    $tfStateBucket = Read-Required "TF_STATE_BUCKET_STAGING"
}

$awsAccessKeyId = aws configure get aws_access_key_id
if ([string]::IsNullOrWhiteSpace($awsAccessKeyId)) {
    $awsAccessKeyId = Read-Required "AWS_ACCESS_KEY_ID"
}

$awsSecretAccessKey = Read-Required "AWS_SECRET_ACCESS_KEY"
$dockerUser = Read-Required "DOCKERHUB_USERNAME"
$dockerToken = Read-Required "DOCKERHUB_TOKEN"
$jwtSecret = Read-Required "JWT_SECRET_STAGING"
$tlsEmail = Read-Host "TLS_EMAIL_STAGING (optional)"

$secretsFile = "$RepoRoot\docs\manual-e2e\staging-secrets.local.env"
@(
    "AWS_ACCESS_KEY_ID=$awsAccessKeyId",
    "AWS_SECRET_ACCESS_KEY=$awsSecretAccessKey",
    "S3_BUCKET_STAGING=$s3Bucket",
    "CF_DIST_ID_STAGING=$cfDistId",
    "DOCKERHUB_USERNAME=$dockerUser",
    "DOCKERHUB_TOKEN=$dockerToken",
    "EC2_HOST_STAGING=$apiPublicIp",
    "EC2_INSTANCE_ID_STAGING=$backendInstanceId",
    "RDS_HOST_STAGING=$rdsHost",
    "RDS_DB_NAME=khaleoapp",
    "RDS_DB_USER=app_user",
    "RDS_DB_PASSWORD=",
    "JWT_SECRET_STAGING=$jwtSecret",
    "API_DOMAIN_STAGING=$apiDomain",
    "VITE_API_BASE_URL_STAGING=https://$apiDomain",
    "TLS_EMAIL_STAGING=$tlsEmail",
    "TF_STATE_BUCKET_STAGING=$tfStateBucket",
    "TF_LOCK_TABLE_STAGING=khaleoapp-terraform-lock",
    "TF_BACKEND_KEY_STAGING=khaleoapp/staging/terraform.tfstate"
) | Set-Content -Path $secretsFile -Encoding UTF8

Write-Host "Wrote: $secretsFile"
Write-Host "Detected from Terraform outputs:"
Write-Host "- S3_BUCKET_STAGING=$s3Bucket"
Write-Host "- CF_DIST_ID_STAGING=$cfDistId"
Write-Host "- EC2_HOST_STAGING=$apiPublicIp"
Write-Host "- EC2_INSTANCE_ID_STAGING=$backendInstanceId"
Write-Host "- RDS_HOST_STAGING=$rdsHost"
Write-Host "- API_DOMAIN_STAGING=$apiDomain"
Write-Host "- TF_STATE_BUCKET_STAGING=$tfStateBucket"
Write-Host "Next: run scripts/set-github-staging-secrets.ps1"
