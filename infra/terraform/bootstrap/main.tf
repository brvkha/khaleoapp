resource "aws_s3_bucket" "tf_state" {
  bucket        = var.tf_state_bucket_name
  force_destroy = true

  tags = {
    Project = var.project
    Scope   = "terraform-backend"
  }
}

resource "aws_s3_bucket_versioning" "tf_state" {
  bucket = aws_s3_bucket.tf_state.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "tf_state" {
  bucket = aws_s3_bucket.tf_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "tf_state" {
  bucket = aws_s3_bucket.tf_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_dynamodb_table" "tf_lock" {
  name         = var.tf_lock_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Project = var.project
    Scope   = "terraform-backend"
  }
}

resource "aws_cloudwatch_log_group" "ssm_session" {
  count             = var.enable_ssm_session_logs ? 1 : 0
  name              = var.ssm_session_log_group_name
  retention_in_days = var.session_log_retention_days

  tags = {
    Project = var.project
    Scope   = "ssm"
  }
}

# Account-level Session Manager preferences document.
# Keep this in the bootstrap stack so separate Terraform states do not fight over one global resource.
resource "aws_ssm_document" "session_preferences" {
  count           = var.enable_ssm_session_logs ? 1 : 0
  name            = "SSM-SessionManagerRunShell"
  document_type   = "Session"
  document_format = "JSON"

  content = jsonencode({
    schemaVersion = "1.0"
    description   = "KhaLeo Session Manager preferences"
    sessionType   = "Standard_Stream"
    inputs = {
      cloudWatchLogGroupName      = var.ssm_session_log_group_name
      cloudWatchEncryptionEnabled = false
      idleSessionTimeout          = "20"
      maxSessionDuration          = "60"
      runAsEnabled                = false
      shellProfile = {
        linux = ""
      }
    }
  })
}

