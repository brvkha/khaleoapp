variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-southeast-1"
}

variable "project" {
  description = "Project name"
  type        = string
  default     = "khaleoapp"
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "vpc_cidr" {
  description = "Primary VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "Public subnet CIDR block"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_a_cidr" {
  description = "Private subnet A CIDR block"
  type        = string
  default     = "10.0.2.0/24"
}

variable "private_subnet_b_cidr" {
  description = "Private subnet B CIDR block"
  type        = string
  default     = "10.0.3.0/24"
}

variable "route53_zone_name" {
  description = "Existing Route53 hosted zone name (no trailing dot), e.g. khaleoshop.click"
  type        = string
}

variable "frontend_domain_name" {
  description = "Frontend domain managed by CloudFront, e.g. khaleoshop.click or staging.khaleoshop.click"
  type        = string
}

variable "api_domain_name" {
  description = "API domain pointing to EC2 elastic IP, e.g. api.khaleoshop.click"
  type        = string
}

variable "ec2_instance_type" {
  description = "Backend EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "ec2_key_pair_name" {
  description = "Optional EC2 key pair name for manual SSH access"
  type        = string
  default     = null
}

variable "db_instance_class" {
  description = "RDS MySQL instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "Application database name"
  type        = string
  default     = "khaleoapp"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "app_user"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_backup_retention_days" {
  description = "RDS backup retention period"
  type        = number
  default     = 7
}

variable "allowed_http_cidrs" {
  description = "CIDRs allowed to reach 80/443 on EC2"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "allowed_ssh_cidrs" {
  description = "CIDRs allowed to reach 22 on EC2 (leave empty when using SSM-only access)"
  type        = list(string)
  default     = []
}

variable "enable_frontend_custom_domain" {
  description = "If true, create ACM cert + Route53 records for frontend custom domain"
  type        = bool
  default     = true
}

variable "common_tags" {
  description = "Extra tags"
  type        = map(string)
  default     = {}
}

