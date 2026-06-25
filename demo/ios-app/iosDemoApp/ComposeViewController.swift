import DemoKit
import SwiftUI
import UIKit

/// Wraps the Compose Multiplatform `MainViewController` so SwiftUI can host it. The shared `FrnkDemoApp`
/// owns the whole UI (theme + navigation) — there's nothing to route back to Swift.
struct ComposeViewController: UIViewControllerRepresentable {
    func makeUIViewController(context _: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_: UIViewController, context _: Context) {}
}
