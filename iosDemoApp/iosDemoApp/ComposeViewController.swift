import DemoKit
import SwiftUI
import UIKit

/// Wraps the Compose Multiplatform `MainViewController` so SwiftUI can host it.
/// MVI effects emitted by `DemoViewModel` are routed back through `onEffect`.
struct ComposeViewController: UIViewControllerRepresentable {
    let onEffect: (DemoEffect) -> Void

    func makeUIViewController(context _: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(onEffect: { effect in
            onEffect(effect)
        })
    }

    func updateUIViewController(_: UIViewController, context _: Context) {}
}
