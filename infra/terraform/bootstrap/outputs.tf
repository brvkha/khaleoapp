output "tf_state_bucket_name" {
  description = "S3 bucket name for Terraform backend"
  value       = aws_s3_bucket.tf_state.bucket
}

output "tf_lock_table_name" {
  description = "DynamoDB lock table name for Terraform backend"
  value       = aws_dynamodb_table.tf_lock.name
}

output "ssm_session_log_group_name" {
  description = "CloudWatch log group for Session Manager logs"
  value       = var.enable_ssm_session_logs ? aws_cloudwatch_log_group.ssm_session[0].name : null
}

