#!/usr/bin/env bash

set -Eeuo pipefail

api_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
infra_dir="${INFRA_DIR:-/home/evellyn/projetos/espresso-infra}"
stack="${PULUMI_STACK:-dev}"
image_tag="${IMAGE_TAG:-$(git -C "$api_dir" rev-parse --short HEAD)}"

for command in aws docker git poetry; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "Erro: comando obrigatório não encontrado: $command" >&2
    exit 1
  fi
done

if [[ ! -d "$infra_dir" ]]; then
  echo "Erro: diretório da infraestrutura não encontrado: $infra_dir" >&2
  exit 1
fi

pulumi_cmd=(poetry -C "$infra_dir" run pulumi)

if [[ -z "${AWS_REGION:-}" ]]; then
  AWS_REGION="$("${pulumi_cmd[@]}" config get aws:region --stack "$stack" 2>/dev/null || true)"
fi
AWS_REGION="${AWS_REGION:-$(aws configure get region 2>/dev/null || true)}"
if [[ -z "$AWS_REGION" ]]; then
  echo "Erro: defina AWS_REGION ou configure a região no AWS CLI/Pulumi." >&2
  exit 1
fi
export AWS_REGION

project_name="$("${pulumi_cmd[@]}" config get projectName --stack "$stack" 2>/dev/null || true)"
project_name="${project_name:-espresso}"
configured_repository="$("${pulumi_cmd[@]}" config get ecrRepositoryName --stack "$stack" 2>/dev/null || true)"
ecr_repository="${ECR_REPOSITORY:-${configured_repository:-${project_name}-api}}"

account_id="$(aws sts get-caller-identity --query Account --output text)"
registry="${account_id}.dkr.ecr.${AWS_REGION}.amazonaws.com"
image="${registry}/${ecr_repository}:${image_tag}"

echo "Autenticando no ECR ${registry}..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$registry"

echo "Buildando ${image}..."
#docker build --network=host --progress=plain -t espresso-api .
docker build --tag "$image" "$api_dir" --network=host

echo "Enviando ${image}..."
docker push "$image"

echo "Configurando a imagem no Pulumi (${stack})..."
"${pulumi_cmd[@]}" config set --stack "$stack" applicationImage "$image"

echo "Executando pulumi up (${stack})..."
"${pulumi_cmd[@]}" up --stack "$stack" --yes

echo "Deploy concluído: ${image}"
