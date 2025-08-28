#!/bin/bash
set -e

export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root

echo "Using VAULT_ADDR=$VAULT_ADDR"
echo "Using VAULT_TOKEN=$VAULT_TOKEN"

mvn clean install