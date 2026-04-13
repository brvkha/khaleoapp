variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-southeast-1"
}

variable "project" {
  description = "Project name prefix"
  type        = string
  default     = "khaleoapp"
}

variable "tf_state_bucket_name" {
  description = "Globally unique S3 bucket name for Terraform remote state"
  type        = string
}

variable "tf_lock_table_name" {
  description = "DynamoDB table for Terraform state locking"
  type        = string
  default     = "khaleoapp-terraform-lock"
}

variable "enable_ssm_session_logs" {
  description = "Create Session Manager preferences and CloudWatch log group"
  type        = bool
  default     = true
}

variable "ssm_session_log_group_name" {
  description = "CloudWatch log group for Session Manager logs"
  type        = string
  default     = "/aws/ssm/khaleoapp/session"
}

variable "session_log_retention_days" {
  description = "Retention for SSM session logs"
  type        = number
  default     = 7
}

