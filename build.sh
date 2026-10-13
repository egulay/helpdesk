#!/usr/bin/env bash
set -euo pipefail

POSTGRES_IMAGE=${POSTGRES_IMAGE:-postgres:18}
POSTGRES_CONTAINER=${POSTGRES_CONTAINER:-postgres18}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_DB=${POSTGRES_DB:-help_desk}
POSTGRES_USER=${POSTGRES_USER:-help_user}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-help_pass}
POSTGRES_SCHEMA=${POSTGRES_SCHEMA:-help_desk}

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

wait_for_postgres() {
  printf '⏳ Waiting for PostgreSQL initialization to complete'
  for _ in {1..90}; do
    if docker exec "${POSTGRES_CONTAINER}" pg_isready -q -d "${POSTGRES_DB}" >/dev/null 2>&1; then
      printf ' - ✅ ready\n'
      return
    fi
    printf '.'
    sleep 1
  done
  printf '\n❌ PostgreSQL initialization did not complete in time.\n' >&2
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

# Connections to 127.0.0.1 inside the container match a "trust" rule in pg_hba.conf
# and never verify the password. The application reaches PostgreSQL through the
# published port, which arrives from the bridge network and matches the
# scram-sha-256 rule, so these checks use the container address instead.
container_address() {
  docker exec "${POSTGRES_CONTAINER}" hostname -i | awk '{print $1}'
}

app_user_can_connect() {
  docker exec -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
    psql -h "${POSTGRES_ADDRESS}" -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
    -tAc 'SELECT 1' >/dev/null 2>&1
}

psql_app() {
  docker exec -e PGPASSWORD="${POSTGRES_PASSWORD}" "${POSTGRES_CONTAINER}" \
    psql -h "${POSTGRES_ADDRESS}" -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc "$1"
}

# Local socket connections inside the official image use trust authentication, so an
# administrative role is reachable even when the stored password no longer matches.
# The superuser the data directory was initialised with, which is the only
# administrative role that exists when POSTGRES_USER was changed afterwards.
initial_superuser() {
  docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' "${POSTGRES_CONTAINER}" 2>/dev/null \
    | sed -n 's/^POSTGRES_USER=//p' | head -1
}

socket_admin_role() {
  local role
  for role in "${POSTGRES_USER}" "$(initial_superuser)" postgres; do
    [[ -n "${role}" ]] || continue
    if docker exec -u postgres "${POSTGRES_CONTAINER}" \
      psql -U "${role}" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
      printf '%s' "${role}"
      return 0
    fi
  done
  return 1
}

psql_admin() {
  docker exec -u postgres "${POSTGRES_CONTAINER}" \
    psql -v ON_ERROR_STOP=1 -U "${ADMIN_ROLE}" -d "$1" -tAc "$2"
}

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '🚀 Helpdesk Build & Environment Setup' \
  '══════════════════════════════════════════════'

command -v docker >/dev/null 2>&1 || {
  printf '❌ Docker is required but was not found.\n' >&2
  exit 1
}

if [[ ! "${POSTGRES_PORT}" =~ ^[0-9]+$ || ! "${VAULT_PORT}" =~ ^[0-9]+$ ]]; then
  printf '❌ POSTGRES_PORT and VAULT_PORT must be numeric.\n' >&2
  exit 1
fi

if [[ ! "${POSTGRES_DB}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '❌ POSTGRES_DB may contain only letters, numbers, and underscores.\n' >&2
  exit 1
fi

if [[ ! "${POSTGRES_USER}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '❌ POSTGRES_USER may contain only letters, numbers, and underscores.\n' >&2
  exit 1
fi

if [[ ! "${POSTGRES_SCHEMA}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '❌ POSTGRES_SCHEMA may contain only letters, numbers, and underscores.\n' >&2
  exit 1
fi

if [[ "${RESET_DB:-false}" == "true" ]] && container_exists "${POSTGRES_CONTAINER}"; then
  printf '🧹 RESET_DB=true: removing PostgreSQL container and its anonymous volumes (%s)\n' "${POSTGRES_CONTAINER}"
  docker rm -fv "${POSTGRES_CONTAINER}" >/dev/null
fi

if ! container_exists "${POSTGRES_CONTAINER}"; then
  printf '📦 Pulling %s...\n' "${POSTGRES_IMAGE}"
  docker pull "${POSTGRES_IMAGE}"

  printf '🐘 Starting PostgreSQL container: %s\n' "${POSTGRES_CONTAINER}"
  docker run -d --name "${POSTGRES_CONTAINER}" \
    -p "${POSTGRES_PORT}:5432" \
    -e POSTGRES_DB="${POSTGRES_DB}" \
    -e POSTGRES_USER="${POSTGRES_USER}" \
    -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
    "${POSTGRES_IMAGE}" >/dev/null
elif ! container_is_running "${POSTGRES_CONTAINER}"; then
  printf '▶️ Starting existing PostgreSQL container: %s\n' "${POSTGRES_CONTAINER}"
  docker start "${POSTGRES_CONTAINER}" >/dev/null
else
  printf '✅ PostgreSQL container %s is already running.\n' "${POSTGRES_CONTAINER}"
fi

wait_for_postgres

POSTGRES_ADDRESS=$(container_address)
if [[ -z "${POSTGRES_ADDRESS}" ]]; then
  printf '❌ Could not determine the address of container %s.\n' "${POSTGRES_CONTAINER}" >&2
  exit 1
fi

if ! app_user_can_connect; then
  printf '⚠️ Cannot connect to %s as %s; repairing the local application role and database...\n' \
    "${POSTGRES_DB}" "${POSTGRES_USER}"

  if ! ADMIN_ROLE=$(socket_admin_role); then
    printf '%s\n' \
      "❌ No administrative role is reachable inside ${POSTGRES_CONTAINER}." \
      'Run RESET_DB=true ./build.sh for a disposable local database.' >&2
    exit 1
  fi

  POSTGRES_PASSWORD_SQL=${POSTGRES_PASSWORD//\'/\'\'}

  if [[ "$(psql_admin postgres "SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRES_USER}'")" == "1" ]]; then
    psql_admin postgres \
      "ALTER ROLE \"${POSTGRES_USER}\" WITH LOGIN PASSWORD '${POSTGRES_PASSWORD_SQL}'" >/dev/null
  else
    psql_admin postgres \
      "CREATE ROLE \"${POSTGRES_USER}\" WITH LOGIN PASSWORD '${POSTGRES_PASSWORD_SQL}'" >/dev/null
  fi

  if [[ "$(psql_admin postgres "SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}'")" != "1" ]]; then
    docker exec -u postgres "${POSTGRES_CONTAINER}" \
      createdb -U "${ADMIN_ROLE}" -O "${POSTGRES_USER}" "${POSTGRES_DB}" >/dev/null
  fi

  # Flyway creates the help_desk schema itself, so the role only needs CREATE on the database.
  psql_admin postgres \
    "GRANT ALL ON DATABASE \"${POSTGRES_DB}\" TO \"${POSTGRES_USER}\"" >/dev/null

  if ! app_user_can_connect; then
    printf '%s\n' \
      "❌ Could not repair access to ${POSTGRES_DB} for ${POSTGRES_USER}." \
      'Run RESET_DB=true ./build.sh for a disposable local database.' >&2
    exit 1
  fi

  printf '✅ Application role and database repaired.\n'
fi

APP_TABLE_COUNT=$(psql_app \
  "SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = '${POSTGRES_SCHEMA}'
     AND table_name IN ('issue_requester', 'issue_request', 'issue_response')")

FLYWAY_TABLE_COUNT=$(psql_app \
  "SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = '${POSTGRES_SCHEMA}'
     AND table_name = 'flyway_schema_history'")

if (( APP_TABLE_COUNT > 0 && FLYWAY_TABLE_COUNT == 0 )); then
  printf '%s\n' \
    '❌ Legacy helpdesk tables exist without Flyway schema history.' \
    'V1 cannot be applied safely on top of a schema Flyway does not know about.' \
    'Back up any required data, then run RESET_DB=true ./build.sh to create a Flyway-managed database.' >&2
  exit 1
fi

printf '✅ Database is reachable. Flyway will create or migrate the %s schema at startup.\n' "${POSTGRES_SCHEMA}"

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
    spring.datasource.url="jdbc:postgresql://127.0.0.1:${POSTGRES_PORT}/${POSTGRES_DB}" \
    spring.datasource.username="${POSTGRES_USER}" \
    spring.datasource.password="${POSTGRES_PASSWORD}" \
    spring.datasource.driver-class-name=org.postgresql.Driver \
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
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
