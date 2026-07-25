#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8888}"
MCP_URL="${BASE_URL%/}/mcp"
TMP_DIR="$(mktemp -d)"
HEADERS_FILE="$TMP_DIR/headers.txt"
BODY_FILE="$TMP_DIR/body.txt"
TOOLS_FILE="$TMP_DIR/tools.txt"
SESSION_ID=""

cleanup() {
  rm -rf "$TMP_DIR"
}

trap cleanup EXIT

for command in curl jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    printf '❌ Required command is not installed: %s\n' "$command"
    exit 1
  fi
done

extract_json() {
  if jq -e . "$BODY_FILE" >/dev/null 2>&1; then
    cat "$BODY_FILE"
    return
  fi

  sed -n 's/^data:[[:space:]]*//p' "$BODY_FILE" | tail -n 1
}

post_mcp() {
  local payload="$1"
  local headers=(
    -H "Content-Type: application/json"
    -H "Accept: application/json, text/event-stream"
  )

  if [[ -n "$SESSION_ID" ]]; then
    headers+=(-H "Mcp-Session-Id: $SESSION_ID")
  fi

  curl -sS --max-time 10 \
    -D "$HEADERS_FILE" \
    -o "$BODY_FILE" \
    -w '%{http_code}' \
    -X POST "$MCP_URL" \
    "${headers[@]}" \
    --data "$payload"
}

printf '🚀 Initializing MCP Streamable HTTP session at %s...\n' "$MCP_URL"

STATUS="$(post_mcp '{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-03-26",
    "capabilities": {},
    "clientInfo": {
      "name": "bash-mcp-test",
      "version": "1.0.0"
    }
  }
}')"

if [[ "$STATUS" != "200" ]]; then
  printf '❌ MCP initialize returned HTTP %s.\n' "$STATUS"
  cat "$BODY_FILE"
  exit 1
fi

SESSION_ID="$(awk 'BEGIN { IGNORECASE=1 } /^Mcp-Session-Id:/ { sub(/^[^:]*:[[:space:]]*/, ""); sub(/\r$/, ""); print }' "$HEADERS_FILE" | tail -n 1)"

if [[ -z "$SESSION_ID" ]]; then
  printf '❌ MCP server did not return an Mcp-Session-Id header.\n'
  cat "$BODY_FILE"
  exit 1
fi

INITIALIZE_JSON="$(extract_json)"
if ! jq -e '.result.protocolVersion' >/dev/null 2>&1 <<< "$INITIALIZE_JSON"; then
  printf '❌ Invalid MCP initialize response.\n%s\n' "$INITIALIZE_JSON"
  exit 1
fi

printf '✅ Session established: %s\n' "$SESSION_ID"

STATUS="$(post_mcp '{
  "jsonrpc": "2.0",
  "method": "notifications/initialized",
  "params": {}
}')"

if [[ "$STATUS" != "200" && "$STATUS" != "202" ]]; then
  printf '❌ Initialized notification returned HTTP %s.\n' "$STATUS"
  cat "$BODY_FILE"
  exit 1
fi

STATUS="$(post_mcp '{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}')"

if [[ "$STATUS" != "200" ]]; then
  printf '❌ tools/list returned HTTP %s.\n' "$STATUS"
  cat "$BODY_FILE"
  exit 1
fi

TOOLS_JSON="$(extract_json)"
jq -r '.result.tools[]?.name' <<< "$TOOLS_JSON" > "$TOOLS_FILE"

if [[ ! -s "$TOOLS_FILE" ]]; then
  printf '❌ MCP returned no tools.\n%s\n' "$TOOLS_JSON"
  exit 1
fi

printf '\n🧰 MCP tools (%s)\n' "$(wc -l < "$TOOLS_FILE" | tr -d '[:space:]')"
awk '{ printf "%2d. %s\n", NR, $0 }' "$TOOLS_FILE"
printf '\n✅ Streamable HTTP handshake and tools/list completed successfully.\n'
