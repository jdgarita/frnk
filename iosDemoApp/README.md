# iosDemoApp

Open this folder in Xcode after running:

```bash
./gradlew :iosApp:assembleXCFramework
```

Then add `iosApp/build/XCFrameworks/release/FrnkKit.xcframework`
to the Xcode project's Frameworks, Libraries, and Embedded Content.

Drop your real `GoogleService-Info.plist` into `iosDemoApp/iosDemoApp/`
(gitignored).
