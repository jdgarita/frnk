// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "FrnkKit",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "FrnkKit", targets: ["FrnkKit"])
    ],
    targets: [
        // After running `./gradlew :iosApp:assembleXCFramework`, point the
        // path below at `build/XCFrameworks/release/FrnkKit.xcframework`
        // and check it in (or host it externally and switch to .binaryTarget(url:checksum:)).
        .binaryTarget(
            name: "FrnkKit",
            path: "build/XCFrameworks/release/FrnkKit.xcframework"
        )
    ]
)
