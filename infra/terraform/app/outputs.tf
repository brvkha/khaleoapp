output "frontend_bucket_name" {
  value       = aws_s3_bucket.frontend.bucket
  description = "S3 bucket for frontend artifacts"
}

output "cloudfront_distribution_id" {
  value       = aws_cloudfront_distribution.frontend.id
  description = "CloudFront distribution id"
}

output "cloudfront_domain_name" {
  value       = aws_cloudfront_distribution.frontend.domain_name
  description = "CloudFront domain"
}

output "api_public_ip" {
  value       = aws_eip.backend.public_ip
  description = "EC2 elastic IP for API"
}

output "backend_instance_id" {
  value       = aws_instance.backend.id
  description = "EC2 instance id for backend host"
}

output "nginx_tls_ssm_document_name" {
  value       = aws_ssm_document.nginx_tls_bootstrap.name
  description = "SSM document name for idempotent Nginx/TLS bootstrap"
}

output "backend_container_port" {
  value       = var.backend_container_port
  description = "Backend port exposed on EC2 for reverse proxy"
}

output "vpc_id" {
  value       = aws_vpc.main.id
  description = "VPC id"
}

output "rds_endpoint" {
  value       = aws_db_instance.main.endpoint
  description = "RDS endpoint"
}

output "rds_db_name" {
  value       = aws_db_instance.main.db_name
  description = "RDS database name"
}

output "frontend_url" {
  value       = var.enable_frontend_custom_domain ? "https://${var.frontend_domain_name}" : "https://${aws_cloudfront_distribution.frontend.domain_name}"
  description = "Frontend URL"
}

output "api_url" {
  value       = "https://${var.api_domain_name}"
  description = "API URL"
}

