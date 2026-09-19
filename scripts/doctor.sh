#!/usr/bin/env bash
# Read-only preflight for a frnk checkout: verifies the machine-local files and tools the build,
# test and demo flows need. Run standalone (`scripts/doctor.sh`) or via `make doctor`. Exits
# non-zero listing everything required that is missing; optional items only print a note.
# Changes nothing.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_PROPERTIES="$REPO_ROOT/local.properties"

failures=0
check() {
    local label="$1"
    shift
    if "$@" >/dev/null 2>&1; then
        echo "ok    $label"
    else
        echo "MISSING $label" >&2
        failures=$((failures + 1))
    fi
}
note() {
    local label="$1"
    shift
    if "$@" >/dev/null 2>&1; then
        echo "ok    $label"
    else
        echo "note  $label (optional, not configured)"
    fi
}

has_property() {
    [[ -f "$LOCAL_PROPERTIES" ]] && grep -qE "^$1=.+" "$LOCAL_PROPERTIES"
}

sdk_dir() {
    [[ -f "$LOCAL_PROPERTIES" ]] && sed -nE 's/^sdk\.dir=(.+)$/\1/p' "$LOCAL_PROPERTIES" | head -1
}

# --- required -------------------------------------------------------------------------------------
check "java" java -version
check "local.properties (cp local.properties.example local.properties)" test -f "$LOCAL_PROPERTIES"

sdk="$(sdk_dir || true)"
if [[ -n "$sdk" ]]; then
    check "Android SDK at sdk.dir ($sdk)" test -d "$sdk"
else
    check "Android SDK via ANDROID_HOME (\${ANDROID_HOME:-unset}) — or set sdk.dir in local.properties" \
        test -d "${ANDROID_HOME:-/nonexistent}"
fi

check "git hooks (core.hooksPath=.githooks; fix: ./gradlew installGitHooks)" \
    bash -c "[[ \"\$(git -C '$REPO_ROOT' config core.hooksPath)\" == '.githooks' ]]"

# --- optional: demo harnesses and iOS --------------------------------------------------------------
note "xcodebuild (needed for iosSimulatorArm64Test, the DemoKit XCFramework and demo/ios-app)" xcodebuild -version
note "local.properties: REVENUECAT_ANDROID_API_KEY (demo-android real purchase path)" has_property REVENUECAT_ANDROID_API_KEY
note "demo/android-app/google-services.json (demo-android real Firebase path)" \
    test -f "$REPO_ROOT/demo/android-app/google-services.json"
note "demo/ios-app/iosDemoApp/GoogleService-Info.plist (required to build iosDemoApp)" \
    test -f "$REPO_ROOT/demo/ios-app/iosDemoApp/GoogleService-Info.plist"

if [[ $failures -gt 0 ]]; then
    echo >&2
    echo "error: $failures required check(s) failed — see README.md 'Setup'" >&2
    exit 1
fi
echo "all required checks passed"
