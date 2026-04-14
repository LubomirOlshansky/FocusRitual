import ActivityKit
import Foundation
import ComposeApp

/// Manages the Live Activity lifecycle from the main app.
/// Implements the Kotlin `LiveActivityHandler` protocol (via KMP bridge).
class LiveActivityManager: NSObject, LiveActivityHandler {

    private var currentActivity: Activity<FocusRitualAttributes>?

    // MARK: - LiveActivityHandler (called from Kotlin)

    func startActivity(
        sessionType: String,
        isPaused: Bool,
        mixSummary: String,
        activeSoundCount: Int32,
        remainingSeconds: Int32,
        totalSeconds: Int32,
        phase: String,
        currentCycle: Int32,
        totalCycles: Int32,
        fadeOutMinutes: Int32
    ) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }

        // End any stale activity first
        endActivity()

        let attributes = FocusRitualAttributes(sessionType: sessionType)
        let contentState = makeContentState(
            isPaused: isPaused,
            mixSummary: mixSummary,
            activeSoundCount: Int(activeSoundCount),
            remainingSeconds: Int(remainingSeconds),
            totalSeconds: Int(totalSeconds),
            phase: phase,
            currentCycle: Int(currentCycle),
            totalCycles: Int(totalCycles),
            fadeOutMinutes: Int(fadeOutMinutes)
        )

        do {
            // staleDate tells iOS to auto-dismiss if the app stops updating
            let staleDate = Date().addingTimeInterval(Double(max(Int(remainingSeconds), 60)))
            let content = ActivityContent(state: contentState, staleDate: staleDate)
            currentActivity = try Activity.request(
                attributes: attributes,
                content: content,
                pushType: nil
            )
        } catch {
            print("[FocusRitual] Failed to start Live Activity: \(error)")
        }
    }

    func updateActivity(
        isPaused: Bool,
        mixSummary: String,
        activeSoundCount: Int32,
        remainingSeconds: Int32,
        totalSeconds: Int32,
        phase: String,
        currentCycle: Int32,
        totalCycles: Int32,
        fadeOutMinutes: Int32
    ) {
        guard let activity = currentActivity else { return }

        let contentState = makeContentState(
            isPaused: isPaused,
            mixSummary: mixSummary,
            activeSoundCount: Int(activeSoundCount),
            remainingSeconds: Int(remainingSeconds),
            totalSeconds: Int(totalSeconds),
            phase: phase,
            currentCycle: Int(currentCycle),
            totalCycles: Int(totalCycles),
            fadeOutMinutes: Int(fadeOutMinutes)
        )

        // Refresh staleDate on each update — if updates stop (app killed), iOS will dismiss
        let staleDate = Date().addingTimeInterval(60)
        let content = ActivityContent(state: contentState, staleDate: staleDate)

        Task {
            await activity.update(content)
        }
    }

    func endActivity() {
        guard let activity = currentActivity else { return }
        let activityToEnd = activity
        currentActivity = nil

        Task {
            let finalContent = activity.content
            await activityToEnd.end(finalContent, dismissalPolicy: .immediate)
        }
    }

    /// Synchronously ends all Live Activities. Safe to call from applicationWillTerminate.
    static func endAllActivitiesSync() {
        let semaphore = DispatchSemaphore(value: 0)
        Task.detached {
            for activity in Activity<FocusRitualAttributes>.activities {
                await activity.end(nil, dismissalPolicy: .immediate)
            }
            semaphore.signal()
        }
        semaphore.wait()
    }

    // MARK: - Private

    private func makeContentState(
        isPaused: Bool,
        mixSummary: String,
        activeSoundCount: Int,
        remainingSeconds: Int,
        totalSeconds: Int,
        phase: String,
        currentCycle: Int,
        totalCycles: Int,
        fadeOutMinutes: Int
    ) -> FocusRitualAttributes.ContentState {
        let endTimestamp = Date().timeIntervalSince1970 + Double(remainingSeconds)
        return .init(
            isPaused: isPaused,
            mixSummary: mixSummary,
            activeSoundCount: activeSoundCount,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            phase: phase,
            currentCycle: currentCycle,
            totalCycles: totalCycles,
            fadeOutMinutes: fadeOutMinutes,
            endTimestamp: endTimestamp
        )
    }
}
