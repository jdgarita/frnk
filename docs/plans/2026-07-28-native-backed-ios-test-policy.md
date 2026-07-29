# Native-backed iOS Test Policy Implementation Plan

> **For agentic workers:** Use `mobiai-mobile-executing-plans-with-subagents` (recommended) or `mobiai-mobile-executing-plans` to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Keep `frnk`'s aggregate Gradle tests green without pretending standalone Kotlin/Native test executables can link consumer-owned Firebase and RevenueCat Apple SDKs.

**Architecture:** Disable only `iosSimulatorArm64Test` in `analytics-impl` and `monetization-impl`, where native Apple frameworks are intentionally supplied by a consuming Xcode target. Continue running their common tests through Android host tests, and make the demo Xcode application build the explicit iOS integration gate.

**Tech Stack:** Kotlin Multiplatform, Gradle Kotlin DSL, Swift Package Manager, Xcode

**Platform:** KMP / Android / iOS

---

### Task 1: Exclude unsupported standalone native test binaries

**Files:**
- Modify: `frnk/capabilities/analytics-impl/build.gradle.kts`
- Modify: `frnk/capabilities/monetization-impl/build.gradle.kts`

- [x] **Step 1: Verify the existing aggregate test fails**

Run: `./gradlew allTests`

Expected: FAIL while linking `analytics-impl` without `FirebaseCore` and `monetization-impl` without RevenueCat Swift compatibility symbols.

- [x] **Step 2: Disable only the unsupported simulator test tasks**

Configure each module's `linkDebugTestIosSimulatorArm64` and `iosSimulatorArm64Test` tasks as disabled, with a comment explaining that common tests remain covered by `testAndroidHostTest` and native linkage belongs to the consumer Xcode integration build.

- [x] **Step 3: Verify the aggregate test passes**

Run: `./gradlew allTests`

Expected: PASS, with the two host-dependent simulator tests skipped.

### Task 2: Document the platform verification gates

**Files:**
- Modify: `README.md`
- Modify: `docs/HOST_INTEGRATION.md`
- Modify: `demo/ios-app/iosDemoApp.xcodeproj/project.pbxproj`

- [x] **Step 1: Document the reason for the exclusions**

State that `analytics-impl` and `monetization-impl` common tests run through `testAndroidHostTest`, while their native Apple linkage must be verified by an Xcode host that supplies Firebase and RevenueCat via SwiftPM or CocoaPods.

- [x] **Step 2: Document the exact Xcode integration command**

Build `DemoKit.xcframework` first, then use `xcodebuild build -project demo/ios-app/iosDemoApp.xcodeproj -scheme iosDemoApp -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO EXCLUDED_SOURCE_FILE_NAMES=GoogleService-Info.plist` as the credential-free native integration gate. Ensure the Crashlytics upload phase skips cleanly when the excluded plist is absent.

- [x] **Step 3: Run formatting and lint checks**

Run: `./gradlew ktlintFormat ktlintCheck`

Expected: PASS.

### Task 3: Final verification

**Files:**
- Verify only

- [x] **Step 1: Run Android host tests**

Run: `./gradlew testAndroidHostTest`

Expected: PASS.

- [x] **Step 2: Build the native dependency integration host**

Run: `./gradlew :demo-shared:assembleDemoKitDebugXCFramework`, then `xcodebuild build -project demo/ios-app/iosDemoApp.xcodeproj -scheme iosDemoApp -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO EXCLUDED_SOURCE_FILE_NAMES=GoogleService-Info.plist`

Expected: BUILD SUCCEEDED with Firebase and RevenueCat resolved by the Xcode target.

- [x] **Step 3: Check the final diff**

Run: `git diff --check`

Expected: no output and exit code 0.
