#!/usr/bin/env bash
# Idempotent Cloud Agent setup for the TWYN monorepo.
#   server/ — FastAPI brain (Python 3.12)
#   app/    — Android app (Kotlin/Compose, Gradle, JDK 17 + Android SDK)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
JDK17="/usr/lib/jvm/java-17-openjdk-amd64"

sudo_if_needed() { if [ "$(id -u)" -eq 0 ]; then "$@"; else sudo "$@"; fi; }

# --- 1. System packages (JDK 17 for Android, venv + unzip) ---------------------
if ! dpkg -s python3.12-venv openjdk-17-jdk-headless unzip curl >/dev/null 2>&1; then
  export DEBIAN_FRONTEND=noninteractive
  sudo_if_needed apt-get update -qq
  sudo_if_needed apt-get install -y -qq \
    python3.12-venv openjdk-17-jdk-headless unzip curl
fi

# Make JDK 17 the default java so Gradle/AGP builds are reproducible.
if [ -x "$JDK17/bin/java" ]; then
  sudo_if_needed update-alternatives --set java "$JDK17/bin/java" >/dev/null 2>&1 || true
fi

# --- 2. Python server dependencies ---------------------------------------------
cd "$REPO_ROOT/server"
if [ ! -x ".venv/bin/python" ]; then
  python3 -m venv .venv
fi
# shellcheck disable=SC1091
. .venv/bin/activate
python -m pip install --upgrade pip -q
pip install -e ".[dev]" -q
deactivate

# --- 3. Android SDK ------------------------------------------------------------
SDKMGR="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMGR" ]; then
  echo "Installing Android command-line tools..."
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -sSL -o "$tmp/cmdtools.zip" \
    https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q -o "$tmp/cmdtools.zip" -d "$ANDROID_HOME/cmdline-tools"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rm -rf "$tmp"
fi

export JAVA_HOME="$JDK17"
yes | "$SDKMGR" --licenses >/dev/null 2>&1 || true
"$SDKMGR" "platform-tools" "platforms;android-35" "build-tools;35.0.0" >/dev/null

# Point the Gradle build at the SDK (local.properties is git-ignored).
echo "sdk.dir=$ANDROID_HOME" > "$REPO_ROOT/app/local.properties"

echo "TWYN environment ready: server venv + Android SDK installed."
