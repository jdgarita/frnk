import DemoKit
import SwiftUI

#if canImport(FirebaseCore)
    import FirebaseCore
#endif

@main
struct iosDemoAppApp: App {
    @StateObject private var backendStore = BackendStore()

    init() {
        configureFirebaseIfAvailable()
        DemoBootstrapKt.bootstrapDemoKoin(backend: .supabase, extraConfig: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView().environmentObject(backendStore)
        }
    }

    private func configureFirebaseIfAvailable() {
        // FirebaseCore is only available once `pod install` has run; the demo still
        // builds and the Supabase backend still works without it. The Firebase
        // backend choice will crash if invoked without both the pod and a real
        // GoogleService-Info.plist.
        #if canImport(FirebaseCore)
            guard
                let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
                FileManager.default.fileExists(atPath: path)
            else { return }
            FirebaseApp.configure()
        #endif
    }
}
