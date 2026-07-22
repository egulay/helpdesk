#!/usr/bin/env bash
set -euo pipefail

MYSQL_IMAGE=${MYSQL_IMAGE:-mysql:8.0}
MYSQL_CONTAINER=${MYSQL_CONTAINER:-mysql8}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DATABASE=${MYSQL_DATABASE:-help_desk}
MYSQL_USER=${MYSQL_USER:-help_user}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-help_pass}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root_pass}

VAULT_IMAGE=${VAULT_IMAGE:-hashicorp/vault:latest}
VAULT_CONTAINER=${VAULT_CONTAINER:-vault}
VAULT_PORT=${VAULT_PORT:-8200}
VAULT_TOKEN=${VAULT_TOKEN:-root}
SECRET_PATH=${SECRET_PATH:-secret/helpdesk}

export VAULT_ADDR=${VAULT_ADDR:-http://127.0.0.1:${VAULT_PORT}}
export VAULT_TOKEN

container_exists() {
  docker container inspect "$1" >/dev/null 2>&1
}

container_is_running() {
  [[ "$(docker inspect -f '{{.State.Running}}' "$1" 2>/dev/null || true)" == "true" ]]
}

wait_for_mysql() {
  printf '⏳ Waiting for MySQL initialization to complete'
  for _ in {1..90}; do
    if docker exec -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" "${MYSQL_CONTAINER}" \
      mysql -uroot -Nse 'SELECT 1' >/dev/null 2>&1; then
      printf ' - ✅ ready\n'
      return
    fi
    printf '.'
    sleep 1
  done
  printf '\n❌ MySQL initialization did not complete in time or the configured root password is incorrect.\n' >&2
  exit 1
}

wait_for_vault() {
  printf '⏳ Waiting for Vault to be ready'
  for _ in {1..40}; do
    if "${VAULT_EXEC[@]}" status >/dev/null 2>&1; then
      printf ' - ✅ ready\n'
      return
    fi
    printf '.'
    sleep 0.25
  done
  printf '\n❌ Vault did not become ready in time.\n' >&2
  exit 1
}

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '🚀 Helpdesk Build & Environment Setup' \
  '══════════════════════════════════════════════'

command -v docker >/dev/null 2>&1 || {
  printf '❌ Docker is required but was not found.\n' >&2
  exit 1
}

if [[ ! "${MYSQL_PORT}" =~ ^[0-9]+$ || ! "${VAULT_PORT}" =~ ^[0-9]+$ ]]; then
  printf '❌ MYSQL_PORT and VAULT_PORT must be numeric.\n' >&2
  exit 1
fi

if [[ ! "${MYSQL_DATABASE}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '❌ MYSQL_DATABASE may contain only letters, numbers, and underscores.\n' >&2
  exit 1
fi

if [[ ! "${MYSQL_USER}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '❌ MYSQL_USER may contain only letters, numbers, and underscores.\n' >&2
  exit 1
fi

if [[ "${RESET_DB:-false}" == "true" ]] && container_exists "${MYSQL_CONTAINER}"; then
  printf '🧹 RESET_DB=true: removing MySQL container and its anonymous volumes (%s)\n' "${MYSQL_CONTAINER}"
  docker rm -fv "${MYSQL_CONTAINER}" >/dev/null
fi

if ! container_exists "${MYSQL_CONTAINER}"; then
  printf '📦 Pulling %s...\n' "${MYSQL_IMAGE}"
  docker pull "${MYSQL_IMAGE}"

  printf '🐬 Starting MySQL container: %s\n' "${MYSQL_CONTAINER}"
  docker run -d --name "${MYSQL_CONTAINER}" \
    -p "${MYSQL_PORT}:3306" \
    -e MYSQL_DATABASE="${MYSQL_DATABASE}" \
    -e MYSQL_USER="${MYSQL_USER}" \
    -e MYSQL_PASSWORD="${MYSQL_PASSWORD}" \
    -e MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
    "${MYSQL_IMAGE}" >/dev/null
elif ! container_is_running "${MYSQL_CONTAINER}"; then
  printf '▶️ Starting existing MySQL container: %s\n' "${MYSQL_CONTAINER}"
  docker start "${MYSQL_CONTAINER}" >/dev/null
else
  printf '✅ MySQL container %s is already running.\n' "${MYSQL_CONTAINER}"
fi

wait_for_mysql

if ! docker exec -e MYSQL_PWD="${MYSQL_PASSWORD}" "${MYSQL_CONTAINER}" \
  mysql -u"${MYSQL_USER}" -D"${MYSQL_DATABASE}" -e 'SELECT 1' >/dev/null 2>&1; then
  printf '⚠️ Cannot connect to %s as %s; repairing the local application user and grants...\n' \
    "${MYSQL_DATABASE}" "${MYSQL_USER}"

  MYSQL_PASSWORD_SQL=${MYSQL_PASSWORD//\'/\'\'}
  docker exec -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" "${MYSQL_CONTAINER}" \
    mysql -uroot -e \
    "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
     CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD_SQL}';
     ALTER USER '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD_SQL}';
     GRANT ALL PRIVILEGES ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_USER}'@'%';
     FLUSH PRIVILEGES;" >/dev/null

  if ! docker exec -e MYSQL_PWD="${MYSQL_PASSWORD}" "${MYSQL_CONTAINER}" \
    mysql -u"${MYSQL_USER}" -D"${MYSQL_DATABASE}" -e 'SELECT 1' >/dev/null 2>&1; then
    printf '%s\n' \
      "❌ Could not repair access to ${MYSQL_DATABASE} for ${MYSQL_USER}." \
      'Check MYSQL_ROOT_PASSWORD or run RESET_DB=true ./build.sh for a disposable local database.' >&2
    exit 1
  fi

  printf '✅ Application user and grants repaired.\n'
fi

APP_TABLE_COUNT=$(docker exec -e MYSQL_PWD="${MYSQL_PASSWORD}" "${MYSQL_CONTAINER}" \
  mysql -u"${MYSQL_USER}" -Nse \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${MYSQL_DATABASE}' AND table_name IN ('issue_requester','issue_request','issue_response')")

FLYWAY_TABLE_COUNT=$(docker exec -e MYSQL_PWD="${MYSQL_PASSWORD}" "${MYSQL_CONTAINER}" \
  mysql -u"${MYSQL_USER}" -Nse \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${MYSQL_DATABASE}' AND table_name='flyway_schema_history'")

if (( APP_TABLE_COUNT > 0 && FLYWAY_TABLE_COUNT == 0 )); then
  printf '%s\n' \
    '❌ Legacy helpdesk tables exist without Flyway schema history.' \
    'The previous build script created these tables directly, so V1 cannot be applied safely.' \
    'Back up any required data, then run RESET_DB=true ./build.sh to create a Flyway-managed database.' >&2
  exit 1
fi

printf '✅ Database is reachable. Flyway will create or migrate the application schema at startup.\n'

if ! container_exists "${VAULT_CONTAINER}"; then
  printf '📦 Pulling %s...\n' "${VAULT_IMAGE}"
  docker pull "${VAULT_IMAGE}"

  printf '🔐 Starting Vault container in development mode: %s\n' "${VAULT_CONTAINER}"
  docker run -d --name "${VAULT_CONTAINER}" \
    -p "${VAULT_PORT}:8200" \
    -e VAULT_DEV_ROOT_TOKEN_ID="${VAULT_TOKEN}" \
    -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 \
    "${VAULT_IMAGE}" \
    server -dev -dev-root-token-id="${VAULT_TOKEN}" -dev-listen-address=0.0.0.0:8200 >/dev/null
elif ! container_is_running "${VAULT_CONTAINER}"; then
  printf '▶️ Starting existing Vault container: %s\n' "${VAULT_CONTAINER}"
  docker start "${VAULT_CONTAINER}" >/dev/null
else
  printf '✅ Vault container %s is already running.\n' "${VAULT_CONTAINER}"
fi

# Commands execute inside the Vault container, where Vault always listens on 8200.
VAULT_EXEC=(
  docker exec
  -e VAULT_ADDR=http://127.0.0.1:8200
  -e VAULT_TOKEN="${VAULT_TOKEN}"
  "${VAULT_CONTAINER}"
  vault
)

wait_for_vault

if "${VAULT_EXEC[@]}" kv get -format=json "${SECRET_PATH}" >/dev/null 2>&1; then
  printf '✅ Vault secret %s already exists.\n' "${SECRET_PATH}"
else
  printf '🔐 Creating Vault datasource secret: %s\n' "${SECRET_PATH}"
  "${VAULT_EXEC[@]}" kv put "${SECRET_PATH}" \
    spring.datasource.url="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    spring.datasource.username="${MYSQL_USER}" \
    spring.datasource.password="${MYSQL_PASSWORD}" \
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect \
    spring.datasource.hikari.auto-commit=false \
    spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED \
    spring.datasource.hikari.minimum-idle=2 \
    spring.datasource.hikari.maximum-pool-size=10 \
    spring.datasource.hikari.pool-name=HikariPool >/dev/null
  printf '✅ Vault datasource secret created.\n'
fi

if [[ -n "${OPENAI_API_KEY:-}" ]]; then
  printf '🔑 Updating the OpenAI API key in Vault...\n'
  "${VAULT_EXEC[@]}" kv patch "${SECRET_PATH}" \
    helpdesk.ai.openai.api-key="${OPENAI_API_KEY}" >/dev/null
  printf '✅ OpenAI API key updated.\n'
else
  printf 'ℹ️ OPENAI_API_KEY is not set; the Vault key was not changed.\n'
fi

printf '🔨 Running Maven clean verify...\n'
./mvnw clean verify

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '✅ Build completed successfully.' \
  'Run ./run.sh to apply Flyway migrations and start the application.' \
  '══════════════════════════════════════════════'
