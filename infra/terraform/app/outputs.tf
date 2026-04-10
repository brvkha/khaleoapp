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

output "rds_endpoint" {
  value       = aws_db_instance.main.endpoint
  description = "RDS endpoint"
}

output "rds_db_name" {
  value       = aws_db_instance.main.db_name
  description = "RDS database name"
}

