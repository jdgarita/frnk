import DemoKit
import Foundation

/// Holds the active `BackendChoice` so SwiftUI can drive Koin re-bootstrapping.
/// `versionTag` is bumped on every swap; ContentView attaches it as a `.id(...)` on
/// the Compose UIViewController so SwiftUI re-creates the controller and the
/// `koinViewModel()` call inside Compose re-resolves against the new Koin container.
final class BackendStore: ObservableObject {
    @Published var current: BackendChoice = .supabase
    @Published var versionTag: Int = 0

    func select(_ backend: BackendChoice) {
        guard backend != current else { return }
        DemoBootstrapKt.swapBackend(newBackend: backend)
        current = backend
        versionTag += 1
    }
}
