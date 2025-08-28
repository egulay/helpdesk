#!/bin/bash
set -euo pipefail

MYSQL_IMAGE=${MYSQL_IMAGE:-mysql:8.0}
MYSQL_CONTAINER=${MYSQL_CONTAINER:-mysql8}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DATABASE=${MYSQL_DATABASE:-help_desk}
MYSQL_USER=${MYSQL_USER:-help_user}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-help_pass}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root_pass}

DDL_PATH=${DDL_PATH:-./src/test/resources/ddl.sql}

VAULT_IMAGE=${VAULT_IMAGE:-hashicorp/vault:latest}
VAULT_CONTAINER=${VAULT_CONTAINER:-vault}
VAULT_PORT=${VAULT_PORT:-8200}
export VAULT_ADDR=${VAULT_ADDR:-http://localhost:${VAULT_PORT}}
export VAULT_TOKEN=${VAULT_TOKEN:-root}

SECRET_PATH=${SECRET_PATH:-secret/helpdesk}


 if [[ "${RESET_DB:-false}" == "true" ]]; then
   if docker ps -a --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
     echo "RESET_DB=true -> removing existing MySQL container and volumes (${MYSQL_CONTAINER})"
     docker rm -fv "${MYSQL_CONTAINER}" >/dev/null || true
   fi
 fi

if ! docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
  if docker ps -a --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    echo "Removing existing stopped container: ${MYSQL_CONTAINER}"
    docker rm -f "${MYSQL_CONTAINER}" >/dev/null
  fi
  echo "Pulling ${MYSQL_IMAGE} ..."
  docker pull "${MYSQL_IMAGE}"
  echo "Starting MySQL container: ${MYSQL_CONTAINER}"
  docker run -d --name "${MYSQL_CONTAINER}" \
    -p ${MYSQL_PORT}:3306 \
    -e MYSQL_DATABASE="${MYSQL_DATABASE}" \
    -e MYSQL_USER="${MYSQL_USER}" \
    -e MYSQL_PASSWORD="${MYSQL_PASSWORD}" \
    -e MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
    "${MYSQL_IMAGE}" >/dev/null
else
  echo "MySQL container '${MYSQL_CONTAINER}' already running."
fi

echo -n "Waiting for MySQL to be ready"
for i in {1..60}; do
  if docker exec "${MYSQL_CONTAINER}" bash -lc "mysqladmin ping -uroot -p\"${MYSQL_ROOT_PASSWORD}\" --silent" >/dev/null 2>&1; then
    echo " - ready"
    break
  fi
  echo -n "."
  sleep 1
  if [[ $i -eq 60 ]]; then
    echo "\nERROR: MySQL did not become ready in time." >&2
    exit 1
  fi
done

if [[ ! -f "${DDL_PATH}" ]]; then
  echo "ERROR: DDL file not found at ${DDL_PATH}" >&2
  exit 1
fi

echo "Copying DDL into MySQL container..."
docker cp "${DDL_PATH}" "${MYSQL_CONTAINER}:/ddl.sql"

echo "Ensuring application user can connect (retry up to 20x)..."
APP_OK=false
for i in {1..20}; do
  if docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -u\"${MYSQL_USER}\" -p\"${MYSQL_PASSWORD}\" -e 'SELECT 1' \"${MYSQL_DATABASE}\" >/dev/null 2>&1"; then
    APP_OK=true
    break
  fi
  echo "  attempt $i/20: app user not ready yet; waiting 2s..."
  sleep 2
done

if [[ "$APP_OK" == "true" ]]; then
  echo "Executing DDL with app user..."
  docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -u\"${MYSQL_USER}\" -p\"${MYSQL_PASSWORD}\" \"${MYSQL_DATABASE}\" < /ddl.sql"
  echo "DDL executed successfully."
else
  echo "App user still not ready; attempting repair using root and re-running DDL..."
  if docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -uroot -p\"${MYSQL_ROOT_PASSWORD}\" -e 'SELECT 1' >/dev/null 2>&1"; then
    docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -uroot -p\"${MYSQL_ROOT_PASSWORD}\" -e \"CREATE DATABASE IF NOT EXISTS \\\`${MYSQL_DATABASE}\\\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}'; GRANT ALL ON \\\`${MYSQL_DATABASE}\\\`.* TO '${MYSQL_USER}'@'%'; FLUSH PRIVILEGES;\""
    docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -u\"${MYSQL_USER}\" -p\"${MYSQL_PASSWORD}\" \"${MYSQL_DATABASE}\" < /ddl.sql"
    echo "DDL executed successfully after repair."
  else
    echo "WARNING: Root login failed; if this is a reused data dir, remove the container with volumes: 'docker rm -fv ${MYSQL_CONTAINER}' and re-run this script." >&2
    exit 1
  fi
fi

if ! docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -u\"${MYSQL_USER}\" -p\"${MYSQL_PASSWORD}\" -e 'SELECT 1' \"${MYSQL_DATABASE}\" >/dev/null 2>&1"; then
  echo "App user ${MYSQL_USER} cannot connect; attempting repair using root..."
  if docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -uroot -p\"${MYSQL_ROOT_PASSWORD}\" -e 'SELECT 1' >/dev/null 2>&1"; then
    docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -uroot -p\"${MYSQL_ROOT_PASSWORD}\" -e \"CREATE DATABASE IF NOT EXISTS \\\`${MYSQL_DATABASE}\\\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}'; GRANT ALL ON \\\`${MYSQL_DATABASE}\\\`.* TO '${MYSQL_USER}'@'%'; FLUSH PRIVILEGES;\""
    docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -u\"${MYSQL_USER}\" -p\"${MYSQL_PASSWORD}\" \"${MYSQL_DATABASE}\" < /ddl.sql"
    echo "DDL re-executed after repairing user/grants."
  else
    echo "WARNING: Root login failed; if this is a reused data dir, remove the container with volumes: 'docker rm -fv ${MYSQL_CONTAINER}' and re-run this script." >&2
    exit 1
  fi
fi


SEED_PATH=./src/test/resources/seed-data.sql
if [[ -f "${SEED_PATH}" ]]; then
  echo "Copying seed data into MySQL container and executing..."
  docker cp "${SEED_PATH}" "${MYSQL_CONTAINER}:/seed-data.sql"
  docker exec "${MYSQL_CONTAINER}" bash -lc "mysql -u\"${MYSQL_USER}\" -p\"${MYSQL_PASSWORD}\" \"${MYSQL_DATABASE}\" < /seed-data.sql"
  echo "Seed data executed successfully."
else
  echo "No seed-data.sql file found at ${SEED_PATH}, skipping seed step."
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${VAULT_CONTAINER}$"; then
  if docker ps -a --format '{{.Names}}' | grep -q "^${VAULT_CONTAINER}$"; then
    echo "Removing existing stopped container: ${VAULT_CONTAINER}"
    docker rm -f "${VAULT_CONTAINER}" >/dev/null
  fi
  echo "Pulling ${VAULT_IMAGE} ..."
  docker pull "${VAULT_IMAGE}"
  echo "Starting Vault (dev mode) container: ${VAULT_CONTAINER}"
  docker run -d --name "${VAULT_CONTAINER}" \
    -p ${VAULT_PORT}:8200 \
    -e VAULT_DEV_ROOT_TOKEN_ID="${VAULT_TOKEN}" \
    -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 \
    "${VAULT_IMAGE}" \
    server -dev -dev-root-token-id="${VAULT_TOKEN}" -dev-listen-address=0.0.0.0:8200 >/dev/null
else
  echo "Vault container '${VAULT_CONTAINER}' already running."
fi

sleep 2

VAULT_EXEC=(docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN=${VAULT_TOKEN} "${VAULT_CONTAINER}" vault)

if "${VAULT_EXEC[@]}" kv get -format=json "${SECRET_PATH}" >/dev/null 2>&1; then
  echo "Vault secret '${SECRET_PATH}' already exists."
else
  echo "Creating Vault secret '${SECRET_PATH}' with datasource values..."
  "${VAULT_EXEC[@]}" kv put "${SECRET_PATH}" \
    spring.datasource.url="jdbc:mysql://localhost:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    spring.datasource.username="${MYSQL_USER}" \
    spring.datasource.password="${MYSQL_PASSWORD}" \
    spring.datasource.driver-class-name="com.mysql.cj.jdbc.Driver" \
    spring.datasource.hikari.auto-commit="false" \
    spring.datasource.hikari.transaction-isolation="TRANSACTION_READ_COMMITTED" \
    spring.datasource.hikari.minimum-idle="2" \
    spring.datasource.hikari.maximum-pool-size="10" \
    spring.datasource.hikari.pool-name="HikariPool"
fi

"${VAULT_EXEC[@]}" kv get "${SECRET_PATH}"

echo "Running mvn clean install ..."
mvn clean install