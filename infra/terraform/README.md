# Terraform Guide (KhaLeo)

Thu muc nay chua ha tang AWS toi gian cho KhaLeo theo huong Free Tier first + de destroy.

## Cau truc

- `bootstrap/`: tao S3 remote state + DynamoDB lock + Session Manager basic logging.
- `app/`: stack ha tang app (VPC, EC2, RDS, S3, CloudFront, Route53).

## 1) Bootstrap remote backend (chay 1 lan)

1. Copy `bootstrap/terraform.tfvars.example` -> `bootstrap/terraform.tfvars` va sua `tf_state_bucket_name`.
2. Apply bootstrap bang local state.

```powershell
terraform -chdir="infra/terraform/bootstrap" init
terraform -chdir="infra/terraform/bootstrap" apply -auto-approve
```

## 2) Deploy Production

1. Copy `app/backend-prod.hcl.example` -> `app/backend-prod.hcl` va thay `bucket`.
2. Copy `app/env/prod.tfvars.example` -> `app/env/prod.tfvars` va sua `db_password`.
3. Init + Apply.

```powershell
terraform -chdir="infra/terraform/app" init -backend-config="backend-prod.hcl"
terraform -chdir="infra/terraform/app" apply -var-file="env/prod.tfvars"
```

## 3) Deploy Staging

1. Copy `app/backend-staging.hcl.example` -> `app/backend-staging.hcl` va thay `bucket`.
2. Copy `app/env/staging.tfvars.example` -> `app/env/staging.tfvars` va sua `db_password`.
3. Init lai backend cho state staging, sau do apply.

```powershell
terraform -chdir="infra/terraform/app" init -reconfigure -backend-config="backend-staging.hcl"
terraform -chdir="infra/terraform/app" apply -var-file="env/staging.tfvars"
```

## 4) Destroy theo moi truong

```powershell
terraform -chdir="infra/terraform/app" init -reconfigure -backend-config="backend-staging.hcl"
terraform -chdir="infra/terraform/app" destroy -var-file="env/staging.tfvars"

terraform -chdir="infra/terraform/app" init -reconfigure -backend-config="backend-prod.hcl"
terraform -chdir="infra/terraform/app" destroy -var-file="env/prod.tfvars"
```

## Luu y chi phi

- Thiet ke nay toi uu theo Free Tier, nhung van co the phat sinh phi neu vuot nguong Free Tier.
- Khong su dung NAT Gateway de tranh chi phi cao.
- Route53 hosted zone va mot so request co the van tinh phi nho.

