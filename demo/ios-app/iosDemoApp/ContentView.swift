import DemoKit
import SwiftUI

struct ContentView: View {
    @State private var toastMessage: String?

    var body: some View {
        ZStack(alignment: .bottom) {
            ComposeViewController(onEffect: handleEffect)
                .ignoresSafeArea()

            if let message = toastMessage {
                ToastBanner(text: message)
                    .padding(.bottom, 32)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: toastMessage)
    }

    private func handleEffect(_ effect: DemoEffect) {
        // Kotlin nested data classes (DemoEffect.Toast / DemoEffect.Navigate) are bridged to Swift as
        // flattened top-level types. Navigation effects are consumed inside FrnkDemoApp (routed into the
        // toolkit's FrnkNavHost back stack), so the host only surfaces toasts.
        switch effect {
        case let toast as DemoEffectToast:
            show(toast.message)
        default:
            break
        }
    }

    private func show(_ message: String) {
        toastMessage = message
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            if toastMessage == message {
                toastMessage = nil
            }
        }
    }
}

private struct ToastBanner: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.callout)
            .foregroundColor(.white)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.black.opacity(0.82))
            .clipShape(Capsule())
            .shadow(radius: 8, y: 2)
    }
}
