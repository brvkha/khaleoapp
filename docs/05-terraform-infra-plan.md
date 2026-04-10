# 05 - Terraform Infra Plan (Prod + Staging)

## Muc tieu

- Dung Terraform de provision, update, va destroy ha tang de dang.
- Uu tien chi phi thap theo Free Tier.
- Van giu Kien truc: S3 + CloudFront + EC2 + RDS + Route53.

## Da chot

- Region: `ap-southeast-1`.
- Moi truong: `prod` + `staging`.
- Terraform remote state: S3 backend + DynamoDB lock.
- Truy cap EC2: AWS SSM Session Manager (khong can mo cong 22).
- Session log SSM: muc co ban vao CloudWatch.
- RDS backup retention: 7 ngay.

## Thu muc Terraform

- `infra/terraform/bootstrap`: tao backend state va SSM logging base.
- `infra/terraform/app`: stack app cho tung moi truong qua file `tfvars` + backend key rieng.

## Quy uoc ten chung

- DB name: `khaleoapp`.
- DB user: `app_user`.
- Prefix tai nguyen: `khaleoapp-<environment>-*`.

## Luong lam viec

1. Apply `bootstrap` (1 lan).
2. Deploy `prod` bang state key `khaleoapp/prod/terraform.tfstate`.
3. Deploy `staging` bang state key `khaleoapp/staging/terraform.tfstate`.
4. Destroy theo tung moi truong khi can.

