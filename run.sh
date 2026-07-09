#!/bin/bash
set -euo pipefail

export VAULT_ADDR=${VAULT_ADDR:-http://127.0.0.1:8200}
export VAULT_TOKEN=${VAULT_TOKEN:-root}

echo "Using VAULT_ADDR=$VAULT_ADDR"
echo "Using VAULT_TOKEN=$VAULT_TOKEN"

VAULT_EXEC=(docker exec -e VAULT_ADDR="${VAULT_ADDR}" -e VAULT_TOKEN="${VAULT_TOKEN}" vault vault)

SECRET_PATH="secret/helpdesk"
OPENAI_MODEL_VALUE="${OPENAI_MODEL:-gpt-5.2}"

if "${VAULT_EXEC[@]}" kv get -format=json "$SECRET_PATH" >/dev/null 2>&1; then
  echo "Vault secret '$SECRET_PATH' already exists."
else
  echo "Vault secret '$SECRET_PATH' not found. Creating with default datasource values..."
  "${VAULT_EXEC[@]}" kv put "$SECRET_PATH" \
    spring.datasource.url="${SPRING_DATASOURCE_URL:-jdbc:mysql://127.0.0.1:3306/help_desk?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}" \
    spring.datasource.username="${SPRING_DATASOURCE_USERNAME:-help_user}" \
    spring.datasource.password="${SPRING_DATASOURCE_PASSWORD:-help_pass}" \
    spring.datasource.driver-class-name="${SPRING_DATASOURCE_DRIVER_CLASS_NAME:-com.mysql.cj.jdbc.Driver}" \
    spring.jpa.properties.hibernate.dialect="${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT:-org.hibernate.dialect.MySQLDialect}" \
    spring.datasource.hikari.auto-commit="${SPRING_DATASOURCE_HIKARI_AUTO_COMMIT:-false}" \
    spring.datasource.hikari.transaction-isolation="${SPRING_DATASOURCE_HIKARI_TRANSACTION_ISOLATION:-TRANSACTION_READ_COMMITTED}" \
    spring.datasource.hikari.minimum-idle="${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:-2}" \
    spring.datasource.hikari.maximum-pool-size="${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:-10}" \
    spring.datasource.hikari.pool-name="${SPRING_DATASOURCE_HIKARI_POOL_NAME:-HikariPool}"
fi

if [ -n "${OPENAI_API_KEY:-}" ]; then
  echo "Patching OpenAI settings into Vault..."
  "${VAULT_EXEC[@]}" kv patch "$SECRET_PATH" \
    helpdesk.ai.openai.api-key="${OPENAI_API_KEY}" \
    helpdesk.ai.openai.model="${OPENAI_MODEL_VALUE}" \
    spring.ai.openai.api-key="${OPENAI_API_KEY}" \
    spring.ai.openai.chat.options.model="${OPENAI_MODEL_VALUE}"
else
  echo "OPENAI_API_KEY is not set. Skipping OpenAI Vault patch."
fi

"${VAULT_EXEC[@]}" kv get "$SECRET_PATH"

mvn spring-boot:run \
  -Dspring-boot.run.main-class=com.helpdesk.HelpdeskApplication \
  -Dspring-boot.run.arguments="--spring.ai.mcp.server.enabled=true"