import Foundation
import ComposeApp

/// Observes Live Activity button actions via Darwin notifications.
/// The widget extension writes the action to App Group UserDefaults,
/// then posts a Darwin notification. This observer reads the action
/// and dispatches it to the Kotlin bridge.
class LiveActivityActionObserver {

    private static let appGroupId = "group.com.focusritual.app"
    private static let actionKey = "com.focusritual.liveactivity.action"
    private static let darwinNotificationName = "com.focusritual.liveactivity.action"

    func startObserving() {
        let center = CFNotificationCenterGetDarwinNotifyCenter()
        CFNotificationCenterAddObserver(
            center,
            Unmanaged.passUnretained(self).toOpaque(),
            { _, observer, _, _, _ in
                guard let observer = observer else { return }
                let myself = Unmanaged<LiveActivityActionObserver>.fromOpaque(observer).takeUnretainedValue()
                myself.handleNotification()
            },
            Self.darwinNotificationName as CFString,
            nil,
            .deliverImmediately
        )
    }

    func stopObserving() {
        let center = CFNotificationCenterGetDarwinNotifyCenter()
        CFNotificationCenterRemoveObserver(
            center,
            Unmanaged.passUnretained(self).toOpaque(),
            CFNotificationName(Self.darwinNotificationName as CFString),
            nil
        )
    }

    private func handleNotification() {
        let defaults = UserDefaults(suiteName: Self.appGroupId)
        guard let action = defaults?.string(forKey: Self.actionKey) else { return }

        // Clear the action immediately to avoid re-processing
        defaults?.removeObject(forKey: Self.actionKey)
        defaults?.synchronize()

        // Dispatch on main thread — KMP bridge calls must be on main
        DispatchQueue.main.async {
            LiveActivityBridge.shared.handleAction(action: action)
        }
    }

    deinit {
        stopObserving()
    }
}
