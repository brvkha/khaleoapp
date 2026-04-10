data "aws_caller_identity" "current" {}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_route53_zone" "main" {
  name         = var.route53_zone_name
  private_zone = false
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

locals {
  name_prefix = "${var.project}-${var.environment}"

  tags = merge(var.common_tags, {
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
  })

  frontend_bucket_name = "${var.project}-${var.environment}-frontend-${data.aws_caller_identity.current.account_id}"
}

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-vpc"
  })
}

resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[0]

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-public-a"
    Tier = "public"
  })
}

resource "aws_subnet" "private_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_a_cidr
  availability_zone = data.aws_availability_zones.available.names[0]

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-private-a"
    Tier = "private"
  })
}

resource "aws_subnet" "private_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_b_cidr
  availability_zone = data.aws_availability_zones.available.names[1]

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-private-b"
    Tier = "private"
  })
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-igw"
  })
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-public-rt"
  })
}

resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_eip" "nat" {
  domain = "vpc"

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-nat-eip"
  })
}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_a.id

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-nat"
  })

  depends_on = [aws_internet_gateway.main]
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-private-rt"
  })
}

resource "aws_route_table_association" "private_a" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_b" {
  subnet_id      = aws_subnet.private_b.id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "ec2" {
  name        = "${local.name_prefix}-ec2-sg"
  description = "Allow web traffic to backend host"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.allowed_http_cidrs
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.allowed_http_cidrs
  }

  ingress {
    description = "Backend app"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = var.allowed_http_cidrs
  }

  dynamic "ingress" {
    for_each = length(var.allowed_ssh_cidrs) > 0 ? [1] : []
    content {
      description = "SSH"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = var.allowed_ssh_cidrs
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = local.tags
}

resource "aws_security_group" "rds" {
  name        = "${local.name_prefix}-rds-sg"
  description = "Allow MySQL from backend EC2 only"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "MySQL from EC2"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = local.tags
}

resource "aws_iam_role" "ec2_ssm" {
  name = "${local.name_prefix}-ec2-ssm-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = local.tags
}

resource "aws_iam_role_policy_attachment" "ec2_ssm_core" {
  role       = aws_iam_role.ec2_ssm.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_cloudwatch_agent" {
  role       = aws_iam_role.ec2_ssm.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${local.name_prefix}-instance-profile"
  role = aws_iam_role.ec2_ssm.name
}

resource "aws_instance" "backend" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = var.ec2_instance_type
  key_name                    = var.ec2_key_pair_name
  subnet_id                   = aws_subnet.public_a.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  associate_public_ip_address = true

  user_data = <<-EOT
    #!/bin/bash
    dnf update -y
    dnf install -y docker nginx
    systemctl enable docker
    systemctl start docker
    systemctl enable nginx
    systemctl start nginx
  EOT

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-backend-ec2"
  })
}

resource "aws_eip" "backend" {
  domain   = "vpc"
  instance = aws_instance.backend.id

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-backend-eip"
  })
}

resource "aws_db_subnet_group" "main" {
  name       = "${local.name_prefix}-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = local.tags
}

resource "aws_db_instance" "main" {
  identifier              = "${var.project}-${var.environment}-db"
  engine                  = "mysql"
  engine_version          = "8.0"
  instance_class          = var.db_instance_class
  allocated_storage       = 20
  max_allocated_storage   = 20
  db_name                 = var.db_name
  username                = var.db_username
  password                = var.db_password
  db_subnet_group_name    = aws_db_subnet_group.main.name
  vpc_security_group_ids  = [aws_security_group.rds.id]
  publicly_accessible     = false
  backup_retention_period = var.db_backup_retention_days
  skip_final_snapshot     = true
  deletion_protection     = false
  multi_az                = false
  storage_encrypted       = true

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-mysql"
  })
}

resource "aws_s3_bucket" "frontend" {
  bucket = local.frontend_bucket_name

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-frontend-bucket"
  })
}

resource "aws_s3_bucket_versioning" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_cloudfront_origin_access_control" "frontend" {
  name                              = "${local.name_prefix}-oac"
  description                       = "OAC for ${local.name_prefix} frontend"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_acm_certificate" "frontend" {
  count                     = var.enable_frontend_custom_domain ? 1 : 0
  provider                  = aws.us_east_1
  domain_name               = var.frontend_domain_name
  validation_method         = "DNS"
  subject_alternative_names = []

  lifecycle {
    create_before_destroy = true
  }

  tags = local.tags
}

resource "aws_route53_record" "frontend_cert_validation" {
  for_each = var.enable_frontend_custom_domain ? {
    for dvo in aws_acm_certificate.frontend[0].domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  } : {}

  zone_id = data.aws_route53_zone.main.zone_id
  name    = each.value.name
  type    = each.value.type
  ttl     = 60
  records = [each.value.record]
}

resource "aws_acm_certificate_validation" "frontend" {
  count           = var.enable_frontend_custom_domain ? 1 : 0
  provider        = aws.us_east_1
  certificate_arn = aws_acm_certificate.frontend[0].arn
  validation_record_fqdns = [
    for record in aws_route53_record.frontend_cert_validation : record.fqdn
  ]
}

resource "aws_cloudfront_distribution" "frontend" {
  enabled             = true
  comment             = "${local.name_prefix} frontend"
  default_root_object = "index.html"
  aliases             = var.enable_frontend_custom_domain ? [var.frontend_domain_name] : []

  origin {
    domain_name              = aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.frontend.id
    origin_id                = "s3-frontend"
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "s3-frontend"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn            = var.enable_frontend_custom_domain ? aws_acm_certificate_validation.frontend[0].certificate_arn : null
    cloudfront_default_certificate = var.enable_frontend_custom_domain ? false : true
    ssl_support_method             = var.enable_frontend_custom_domain ? "sni-only" : null
    minimum_protocol_version       = var.enable_frontend_custom_domain ? "TLSv1.2_2021" : null
  }

  # SPA fallback
  custom_error_response {
    error_code            = 403
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 300
  }

  custom_error_response {
    error_code            = 404
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 300
  }

  tags = local.tags
}

resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontServicePrincipalReadOnly"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = ["s3:GetObject"]
        Resource = ["${aws_s3_bucket.frontend.arn}/*"]
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.frontend.arn
          }
        }
      }
    ]
  })
}

resource "aws_route53_record" "frontend_alias" {
  count   = var.enable_frontend_custom_domain ? 1 : 0
  zone_id = data.aws_route53_zone.main.zone_id
  name    = var.frontend_domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "api" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = var.api_domain_name
  type    = "A"
  ttl     = 300
  records = [aws_eip.backend.public_ip]
}

