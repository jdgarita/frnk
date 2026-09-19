# frnk — the agent-facing entry point for every common operation. `make help` lists the targets.
#
# frnk is a Kotlin Multiplatform toolkit consumed as source (git submodule + Gradle composite
# build); it has no store release. Every target here maps 1:1 to the Gradle task or script it
# names, so nothing new is defined — this file is the interface, README.md / AGENTS.md describe
# what runs underneath. Parallelism and the build cache are already on in gradle.properties.
#
# Variables (override on the command line, e.g. `make test GRADLE_ARGS=--info`):
#   GRADLE_ARGS   extra flags for every Gradle call            (default: none)
#   VERSION       for `make release-check`, e.g. VERSION=0.4.4

SHELL := /bin/bash
.DEFAULT_GOAL := help

GRADLEW       := ./gradlew
XCODE_DIR     := demo/ios-app
XCODE_PROJECT := iosDemoApp.xcodeproj
XCODE_SCHEME  := iosDemoApp
VERSION_FILE  := frnk/core/util/src/commonMain/kotlin/dev/jdgarita/frnk/utils/Frnk.kt

GRADLE_ARGS ?=
VERSION     ?=

.PHONY: help doctor build compile xcframework test test-ios test-all lint format \
        release-check release-beta release-prod clean hooks

##@ General

help: ## Print this help (the default target)
	@awk 'BEGIN { FS = ":.*## "; printf "\nUsage: make <target> [VAR=value]\n" } \
	      /^##@ / { printf "\n%s\n", substr($$0, 5) } \
	      /^[a-zA-Z0-9_-]+:.*## / { printf "  %-16s %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
	@printf "\nVariables: GRADLE_ARGS, VERSION (release-check)\n\n"

doctor: ## Read-only preflight of the machine-local files and tools the build and demos need (scripts/doctor.sh)
	scripts/doctor.sh

##@ Build

build: ## Standard Gradle build for both platforms: every module's Android + iOS artifacts, no tests (./gradlew assemble)
	$(GRADLEW) assemble $(GRADLE_ARGS)

compile: ## Fast compile gate: commonMain + androidMain of every module plus the Android demo (the pre-push check)
	$(GRADLEW) compileAndroidMain :demo-android:compileDebugKotlin $(GRADLE_ARGS)

xcframework: ## DemoKit.xcframework for demo/ios-app (:demo-shared:assembleDemoKitDebugXCFramework); needs macOS + Xcode
	$(GRADLEW) :demo-shared:assembleDemoKitDebugXCFramework $(GRADLE_ARGS)

##@ Test

test: ## All unit tests: testAndroidHostTest (commonTest + androidHostTest) across every KMP module + the Android demo's unit tests
	$(GRADLEW) testAndroidHostTest :demo-android:testDebugUnitTest $(GRADLE_ARGS)

test-ios: ## Kotlin/Native tests on the iOS simulator target (iosSimulatorArm64Test); needs macOS + Xcode
	$(GRADLEW) iosSimulatorArm64Test $(GRADLE_ARGS)

test-all: ## Every target's tests with the aggregated report (allTests); slow
	$(GRADLEW) allTests $(GRADLE_ARGS)

lint: ## ktlint check only (no changes)
	$(GRADLEW) ktlintCheck $(GRADLE_ARGS)

format: ## ktlint auto-fix (the pre-commit hook runs this too)
	$(GRADLEW) ktlintFormat $(GRADLE_ARGS)

##@ Release (frnk ships as source: a release is a git tag — see docs/RELEASING.md)

release-check: ## Read-only preflight before tagging VERSION=x.y.z: Frnk.VERSION matches, CHANGELOG has the section, tag is free
	@[[ -n "$(VERSION)" ]] || { echo "usage: make release-check VERSION=x.y.z" >&2; exit 2; }
	@actual="$$(sed -nE 's/.*VERSION = "([^"]+)".*/\1/p' $(VERSION_FILE))"; \
	if [[ "$$actual" == "$(VERSION)" ]]; then echo "ok    Frnk.VERSION = $$actual"; \
	else echo "FAIL  Frnk.VERSION is '$$actual', expected '$(VERSION)' ($(VERSION_FILE))" >&2; exit 1; fi
	@if grep -qF '## [$(VERSION)]' CHANGELOG.md; then echo "ok    CHANGELOG.md has a [$(VERSION)] section"; \
	else echo "FAIL  CHANGELOG.md has no '## [$(VERSION)]' section" >&2; exit 1; fi
	@if git rev-parse -q --verify "refs/tags/v$(VERSION)" >/dev/null; then echo "FAIL  tag v$(VERSION) already exists locally" >&2; exit 1; \
	elif [[ -n "$$(git ls-remote --tags origin "refs/tags/v$(VERSION)" 2>/dev/null)" ]]; then echo "FAIL  tag v$(VERSION) already exists on origin (run: git fetch --tags origin)" >&2; exit 1; \
	else echo "ok    tag v$(VERSION) is free (locally and on origin)"; fi
	@echo "next: docs/RELEASING.md — PR the bookkeeping commit, merge, then tag the merge commit on main and push the tag (only when asked)"

release-beta: ## Not applicable: frnk has no beta channel. Prints the release flow and exits non-zero
	@echo "frnk has no beta release: it ships as source and is pinned by hosts through a git tag." >&2
	@echo "Run 'make release-check VERSION=x.y.z' and follow docs/RELEASING.md." >&2
	@exit 2

release-prod: ## Not applicable: frnk releases are git tags cut per docs/RELEASING.md. Prints the flow and exits non-zero
	@echo "frnk releases are annotated git tags on main, created by hand after the release PR merges." >&2
	@echo "Run 'make release-check VERSION=x.y.z' and follow docs/RELEASING.md." >&2
	@exit 2

##@ Maintenance

clean: ## Gradle clean (all modules + build-logic) and THIS checkout's Xcode DerivedData for iosDemoApp
	$(GRADLEW) clean $(GRADLE_ARGS)
	$(GRADLEW) -p build-logic clean $(GRADLE_ARGS)
	@derived="$$(cd $(XCODE_DIR) && xcodebuild -project $(XCODE_PROJECT) -scheme $(XCODE_SCHEME) -showBuildSettings 2>/dev/null \
	    | sed -nE 's|^ *BUILD_DIR = (.*/DerivedData/[^/]+)/Build/Products$$|\1|p' | head -1)"; \
	if [[ -n "$$derived" && -d "$$derived" ]]; then \
	    echo "==> removing $$derived"; rm -rf "$$derived"; \
	else \
	    echo "==> no Xcode DerivedData found for $(XCODE_SCHEME); nothing to remove"; \
	fi

hooks: ## Point git at .githooks/ so the ktlint pre-commit hook runs (./gradlew installGitHooks)
	$(GRADLEW) installGitHooks $(GRADLE_ARGS)
