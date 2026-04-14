import ActivityKit
import AppIntents
import Foundation

/// Shared App Group for communication between widget extension and main app.
private let appGroupId = "group.com.focusritual.app"
private let actionKey = "com.focusritual.liveactivity.action"
private let darwinNotificationName = "com.focusritual.liveactivity.action"

/// Posts a Darwin notification to wake the main app.
private func postActionNotification(_ action: String) {
    let defaults = UserDefaults(suiteName: appGroupId)
    defaults?.set(action, forKey: actionKey)
    defaults?.synchronize()

    let center = CFNotificationCenterGetDarwinNotifyCenter()
    CFNotificationCenterPostNotification(
        center,
        CFNotificationName(darwinNotificationName as CFString),
        nil,
        nil,
        true
    )
}

/// Immediately updates the Live Activity UI by toggling isPaused in the content state.
/// This ensures the icon changes even when the main app's Compose runtime is paused.
private func updateLiveActivityPauseState() async {
    guard let activity = Activity<FocusRitualAttributes>.activities.first else { return }
    let current = activity.content.state
    let newIsPaused = !current.isPaused
    let updated = FocusRitualAttributes.ContentState(
        isPaused: newIsPaused,
        mixSummary: current.mixSummary,
        activeSoundCount: current.activeSoundCount,
        remainingSeconds: current.remainingSeconds,
        totalSeconds: current.totalSeconds,
        phase: current.phase,
        currentCycle: current.currentCycle,
        totalCycles: current.totalCycles,
        fadeOutMinutes: current.fadeOutMinutes,
        endTimestamp: newIsPaused
            ? current.endTimestamp
            : Date().timeIntervalSince1970 + Double(current.remainingSeconds)
    )
    let content = ActivityContent(state: updated, staleDate: nil)
    await activity.update(content)
}

// MARK: - Toggle Pause

struct TogglePauseIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "Toggle Pause"
    static var description: IntentDescription = "Pause or resume the current session"

    func perform() async throws -> some IntentResult {
        postActionNotification("togglePause")
        await updateLiveActivityPauseState()
        return .result()
    }
}

// MARK: - Stop Mix (ambient playback)

struct StopMixIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "Stop Mix"
    static var description: IntentDescription = "Stop the ambient mix playback"

    func perform() async throws -> some IntentResult {
        postActionNotification("stopMix")
        return .result()
    }
}

// MARK: - End Session (focus / sleep)

struct EndSessionIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "End Session"
    static var description: IntentDescription = "End the current focus or sleep session"

    func perform() async throws -> some IntentResult {
        postActionNotification("endSession")
        return .result()
    }
}

// MARK: - Skip Phase (focus session)

struct SkipPhaseIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "Skip Phase"
    static var description: IntentDescription = "Skip the current focus or break phase"

    func perform() async throws -> some IntentResult {
        postActionNotification("skipPhase")
        return .result()
    }
}
