#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8888}"

TMP_DIR="$(mktemp -d)"
SSE_OUT="$TMP_DIR/sse.out"
SSE_ERR="$TMP_DIR/sse.err"
TOOLS_FILE="$TMP_DIR/tools.txt"

SESSION_PATH=""
SESSION_URL=""
SSE_PID=""

TOOLS_FOUND=0
ENDPOINT_EVENTS=0
MESSAGE_EVENTS=0
JSON_RESPONSES=0

cleanup() {
  if [[ -n "${SSE_PID:-}" ]]; then
    kill "$SSE_PID" >/dev/null 2>&1 || true
    wait "$SSE_PID" >/dev/null 2>&1 || true
  fi

  rm -rf "$TMP_DIR"
}

trap cleanup EXIT

print_separator() {
  printf '%s\n' \
    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

post_mcp() {
  local payload="$1"

  curl -sS \
    --max-time 5 \
    -X POST "$SESSION_URL" \
    -H "Content-Type: application/json" \
    -d "$payload" >/dev/null || true
}

extract_report_data() {
  local line
  local payload

  : > "$TOOLS_FILE"

  while IFS= read -r line; do
    # Remove a possible carriage return from CRLF output.
    line="${line%$'\r'}"

    case "$line" in
      event:endpoint | event:\ endpoint)
        ENDPOINT_EVENTS=$((ENDPOINT_EVENTS + 1))
        ;;

      event:message | event:\ message)
        MESSAGE_EVENTS=$((MESSAGE_EVENTS + 1))
        ;;

      data:*)
        # Supports both:
        # data:{"jsonrpc":...}
        # data: {"jsonrpc":...}
        payload="${line#data:}"

        # Remove leading whitespace.
        payload="${payload#"${payload%%[![:space:]]*}"}"

        if [[ -z "$payload" ]]; then
          continue
        fi

        if jq -e . >/dev/null 2>&1 <<< "$payload"; then
          JSON_RESPONSES=$((JSON_RESPONSES + 1))

          jq -r '
            if (.result.tools? | type) == "array" then
              .result.tools[].name
            else
              empty
            end
          ' <<< "$payload" >> "$TOOLS_FILE"
        fi
        ;;
    esac
  done < "$SSE_OUT"

  if [[ -s "$TOOLS_FILE" ]]; then
    TOOLS_FOUND="$(wc -l < "$TOOLS_FILE" | tr -d '[:space:]')"
  else
    TOOLS_FOUND=0
  fi
}

print_tools() {
  printf '\n'
  print_separator
  printf '🧰 MCP Tools (%s)\n' "$TOOLS_FOUND"
  print_separator

  if [[ "$TOOLS_FOUND" -eq 0 ]]; then
    printf '⚠️  No MCP tools were returned.\n'
  else
    awk '
      {
        printf "%2d. %s\n", NR, $0
      }
    ' "$TOOLS_FILE"
  fi

  print_separator
}

print_report() {
  printf '\n'
  print_separator
  printf '✅ MCP test completed\n'
  print_separator
  printf '🚀 SSE connection opened\n'
  printf '🆔 Session established\n'
  printf '🔗 Session URL: %s\n' "$SESSION_URL"
  printf '📤 Initialize request sent\n'
  printf '📤 Initialized notification sent\n'
  printf '🔧 Tools list request sent\n'
  printf '📨 SSE responses received\n'
  printf '🧰 Tools found: %s\n' "$TOOLS_FOUND"
  printf '📬 Message events: %s\n' "$MESSAGE_EVENTS"
  printf '🔗 Endpoint events: %s\n' "$ENDPOINT_EVENTS"
  printf '📦 JSON responses: %s\n' "$JSON_RESPONSES"
  printf '🧹 Temporary files cleaned up on exit\n'
  print_separator
}

printf '🚀 Opening MCP SSE connection...\n'

curl -sS -N \
  -H "Accept: text/event-stream" \
  "$BASE_URL/sse" > "$SSE_OUT" 2> "$SSE_ERR" &

SSE_PID=$!

printf '⏳ Waiting for MCP session id...\n'

for i in {1..40}; do
  SESSION_PATH="$(
    awk '
      /^data:/ {
        value = substr($0, 6)
        sub(/^[[:space:]]+/, "", value)

        if (value ~ /^\//) {
          print value
          exit
        }
      }
    ' "$SSE_OUT" | tr -d '\r\n'
  )"

  if [[ -n "$SESSION_PATH" ]]; then
    break
  fi

  sleep 0.25
done

if [[ -z "$SESSION_PATH" ]]; then
  printf '❌ Could not get MCP session path.\n'

  if [[ -s "$SSE_OUT" ]]; then
    printf '\n📥 SSE output\n'
    print_separator
    cat "$SSE_OUT"
  fi

  if [[ -s "$SSE_ERR" ]]; then
    printf '\n⚠️  SSE errors\n'
    print_separator
    cat "$SSE_ERR"
  fi

  exit 1
fi

SESSION_URL="${BASE_URL}${SESSION_PATH}"

printf '✅ MCP session established\n'
printf '🔗 %s\n' "$SESSION_URL"

printf '📤 Initializing MCP session...\n'

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

printf '📤 Sending initialized notification...\n'

post_mcp '{
  "jsonrpc": "2.0",
  "method": "notifications/initialized",
  "params": {}
}'

sleep 1

printf '🔧 Listing MCP tools...\n'

post_mcp '{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}'

sleep 2

extract_report_data
print_tools
print_report