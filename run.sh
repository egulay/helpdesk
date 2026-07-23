#!/usr/bin/env bash
set -euo pipefail

VAULT_CONTAINER=${VAULT_CONTAINER:-vault}
VAULT_PORT=${VAULT_PORT:-8200}
VAULT_TOKEN=${VAULT_TOKEN:-root}
SECRET_PATH=${SECRET_PATH:-secret/helpdesk}

export VAULT_ADDR=${VAULT_ADDR:-http://127.0.0.1:${VAULT_PORT}}
export VAULT_TOKEN

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '🚀 Helpdesk Application Launcher' \
  '══════════════════════════════════════════════'

command -v docker >/dev/null 2>&1 || {
  printf '❌ Docker is required but was not found.\n' >&2
  exit 1
}

if ! docker container inspect "${VAULT_CONTAINER}" >/dev/null 2>&1; then
  printf '❌ Vault container %s does not exist. Run ./build.sh first.\n' "${VAULT_CONTAINER}" >&2
  exit 1
fi

if [[ "$(docker inspect -f '{{.State.Running}}' "${VAULT_CONTAINER}")" != "true" ]]; then
  printf '▶️ Starting existing Vault container: %s\n' "${VAULT_CONTAINER}"
  docker start "${VAULT_CONTAINER}" >/dev/null
fi

# Commands execute inside the Vault container, where Vault always listens on 8200.
VAULT_EXEC=(
  docker exec
  -e VAULT_ADDR=http://127.0.0.1:8200
  -e VAULT_TOKEN="${VAULT_TOKEN}"
  "${VAULT_CONTAINER}"
  vault
)

printf '⏳ Waiting for Vault to be ready'
VAULT_READY=false
for _ in {1..40}; do
  if "${VAULT_EXEC[@]}" status >/dev/null 2>&1; then
    VAULT_READY=true
    break
  fi
  printf '.'
  sleep 0.25
done

if [[ "${VAULT_READY}" != "true" ]]; then
  printf '\n❌ Vault did not become ready in time.\n' >&2
  exit 1
fi
printf ' - ✅ ready\n'

if ! "${VAULT_EXEC[@]}" kv get -format=json "${SECRET_PATH}" >/dev/null 2>&1; then
  printf '❌ Vault secret %s does not exist. Run ./build.sh first.\n' "${SECRET_PATH}" >&2
  exit 1
fi

printf '✅ Vault configuration is available at %s.\n' "${SECRET_PATH}"

if [[ -n "${OPENAI_API_KEY:-}" ]]; then
  printf '🔑 Updating the OpenAI API key in Vault...\n'
  "${VAULT_EXEC[@]}" kv patch "${SECRET_PATH}" \
    helpdesk.ai.openai.api-key="${OPENAI_API_KEY}" >/dev/null
  printf '✅ OpenAI API key updated.\n'
else
  printf 'ℹ️ OPENAI_API_KEY is not set; the Vault key was not changed.\n'
fi

printf '🚀 Starting Spring Boot; Flyway will validate and migrate the database...\n'
./mvnw spring-boot:run \
  -Dspring-boot.run.main-class=io.gulay.helpdesk.HelpdeskApplication \
  -Dspring-boot.run.arguments=--spring.ai.mcp.server.enabled=true

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '✅ Application finished.' \
  '══════════════════════════════════════════════'
