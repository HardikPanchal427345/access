#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${PLAY_SERVICE_ACCOUNT_JSON:-}" ]]; then
  echo "ERROR: PLAY_SERVICE_ACCOUNT_JSON env var is required."
  echo "Point it to your Google Play service account json file."
  exit 1
fi

if [[ ! -f "$PLAY_SERVICE_ACCOUNT_JSON" ]]; then
  echo "ERROR: service account file not found: $PLAY_SERVICE_ACCOUNT_JSON"
  exit 1
fi

if [[ ! -f "local.properties" && -n "${ANDROID_HOME:-}" ]]; then
  echo "sdk.dir=${ANDROID_HOME}" > local.properties
fi

if [[ ! -f "local.properties" ]]; then
  echo "ERROR: local.properties missing and ANDROID_HOME not set."
  exit 1
fi

TRACK="${PLAY_TRACK:-production}"

echo "Publishing release bundle to Play track: $TRACK"
./gradlew :app:publishReleaseBundle -PPLAY_SERVICE_ACCOUNT_JSON="$PLAY_SERVICE_ACCOUNT_JSON" -PPLAY_TRACK="$TRACK"
echo "Upload complete. Verify rollout in Play Console."
