#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8888}"

TMP_DIR="$(mktemp -d)"
SSE_OUT="$TMP_DIR/sse.out"
SSE_ERR="$TMP_DIR/sse.err"

cleanup() {
  [[ -n "${SSE_PID:-}" ]] && kill "$SSE_PID" >/dev/null 2>&1 || true
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

echo "Opening MCP SSE connection..."

curl -sS -N \
  -H "Accept: text/event-stream" \
  "$BASE_URL/sse" > "$SSE_OUT" 2> "$SSE_ERR" &

SSE_PID=$!

echo "Waiting for MCP session id..."

SESSION_PATH=""

for i in {1..40}; do
  SESSION_PATH="$(awk -F'data:' '/^data:/ {print $2; exit}' "$SSE_OUT" | tr -d '\r\n')"

  if [[ -n "$SESSION_PATH" ]]; then
    break
  fi

  sleep 0.25
done

if [[ -z "$SESSION_PATH" ]]; then
  echo "ERROR: Could not get MCP session path."
  cat "$SSE_OUT"
  cat "$SSE_ERR"
  exit 1
fi

SESSION_URL="${BASE_URL}${SESSION_PATH}"

echo "MCP session URL: $SESSION_URL"
echo

post_mcp() {
  local payload="$1"

  curl -sS --max-time 5 -X POST "$SESSION_URL" \
    -H "Content-Type: application/json" \
    -d "$payload" >/dev/null || true
}

echo "Initializing MCP session..."

post_mcp '{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "bash-mcp-test",
      "version": "1.0.0"
    }
  }
}'

sleep 1

echo "Sending initialized notification..."

post_mcp '{
  "jsonrpc": "2.0",
  "method": "notifications/initialized",
  "params": {}
}'

sleep 1

echo "Listing MCP tools..."

post_mcp '{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}'

sleep 2

echo
echo "SSE responses:"
echo "----------------------------------------"
cat "$SSE_OUT"
echo
echo "----------------------------------------"
echo "Done."