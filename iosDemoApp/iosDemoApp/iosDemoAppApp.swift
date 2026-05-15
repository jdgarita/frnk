import DemoKit
import SwiftUI

@main
struct iosDemoAppApp: App {
    init() {
        DemoBootstrapKt.bootstrapDemoKoin(extraConfig: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
