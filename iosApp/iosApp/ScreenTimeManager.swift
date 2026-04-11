import FamilyControls
import SwiftUI
import ComposeApp

class ScreenTimeManager: NSObject, ScreenTimeHandler {
    private weak var presentedController: UIViewController?

    func requestSetup(
        onSuccess: @escaping () -> Void,
        onCancelled: @escaping () -> Void,
        onDenied: @escaping () -> Void
    ) {
        Task { @MainActor in
            let center = AuthorizationCenter.shared

            switch center.authorizationStatus {
            case .notDetermined:
                do {
                    try await center.requestAuthorization(for: .individual)
                } catch {
                    onDenied()
                    return
                }
            case .denied:
                onDenied()
                return
            case .approved:
                break
            @unknown default:
                onDenied()
                return
            }

            self.showPicker(onSuccess: onSuccess, onCancelled: onCancelled)
        }
    }

    @MainActor
    private func showPicker(
        onSuccess: @escaping () -> Void,
        onCancelled: @escaping () -> Void
    ) {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else {
            onCancelled()
            return
        }
        let topVC = Self.topMost(from: rootVC)

        let pickerView = PickerSheet(
            onDone: { [weak self] in
                self?.presentedController?.dismiss(animated: true) { onSuccess() }
            },
            onCancel: { [weak self] in
                self?.presentedController?.dismiss(animated: true) { onCancelled() }
            }
        )
        let hosting = UIHostingController(rootView: pickerView)
        hosting.modalPresentationStyle = .pageSheet
        presentedController = hosting
        topVC.present(hosting, animated: true)
    }

    private static func topMost(from vc: UIViewController) -> UIViewController {
        if let presented = vc.presentedViewController {
            return topMost(from: presented)
        }
        return vc
    }
}

private struct PickerSheet: View {
    @State private var selection = FamilyActivitySelection()
    let onDone: () -> Void
    let onCancel: () -> Void

    var body: some View {
        NavigationView {
            FamilyActivityPicker(selection: $selection)
                .navigationTitle("Block Apps")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { onCancel() }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Done") { onDone() }
                    }
                }
        }
    }
}
