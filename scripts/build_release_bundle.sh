#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f "local.properties" && -n "${ANDROID_HOME:-}" ]]; then
  echo "sdk.dir=${ANDROID_HOME}" > local.properties
fi

if [[ ! -f "local.properties" ]]; then
  echo "ERROR: local.properties missing and ANDROID_HOME not set."
  echo "Set ANDROID_HOME to your Android SDK path and retry."
  exit 1
fi

echo "Building release AAB..."
./gradlew clean :app:bundleRelease

echo "Release build completed."
echo "Bundle path: app/build/outputs/bundle/release/app-release.aab"
