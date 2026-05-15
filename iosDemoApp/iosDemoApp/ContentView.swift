import DemoKit
import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var backendStore: BackendStore
    @State private var toastMessage: String?

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                BackendPicker(current: $backendStore.current) { choice in
                    backendStore.select(choice)
                }
                .padding(.horizontal)
                .padding(.top, 8)

                ComposeViewController(onEffect: handleEffect)
                    .id(backendStore.versionTag)
                    .ignoresSafeArea(edges: .bottom)
            }

            if let message = toastMessage {
                ToastBanner(text: message)
                    .padding(.bottom, 32)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: toastMessage)
    }

    private func handleEffect(_ effect: DemoEffect) {
        // Kotlin nested data classes (DemoEffect.Toast / DemoEffect.Navigate) are
        // bridged to Swift as flattened top-level types `DemoEffectToast` /
        // `DemoEffectNavigate` that conform to the DemoEffect protocol.
        switch effect {
        case let toast as DemoEffectToast:
            show(toast.message)
        case let nav as DemoEffectNavigate:
            show("Navigate → \(nav.routeKey)")
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

private struct BackendPicker: View {
    @Binding var current: BackendChoice
    let onSelect: (BackendChoice) -> Void

    var body: some View {
        Picker("Backend", selection: Binding(
            get: { current },
            set: { onSelect($0) }
        )) {
            Text("Supabase").tag(BackendChoice.supabase)
            Text("Firebase").tag(BackendChoice.firebase)
        }
        .pickerStyle(.segmented)
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
