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

        guard ActivityAuthorizationInfo().areActivitiesEnabled else {
            endActivity()
            return
        }

        Task {
            await endExistingActivities()

            let attributes = FocusRitualAttributes(sessionType: sessionType)
            let staleDate = Date().addingTimeInterval(Double(max(Int(remainingSeconds), 60)))
            let content = ActivityContent(state: contentState, staleDate: staleDate)

            do {
                currentActivity = try Activity.request(
                    attributes: attributes,
                    content: content,
                    pushType: nil
                )
            } catch {
                print("[FocusRitual] Failed to start Live Activity: \(error)")
            }
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
        guard let activity = recoverActivity() else { return }

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

        let staleDate = Date().addingTimeInterval(60)
        let content = ActivityContent(state: contentState, staleDate: staleDate)

        Task {
            await activity.update(content)
        }
    }

    func endActivity() {
        let activityToEnd = currentActivity
        currentActivity = nil

        Task {
            if let activityToEnd {
                await activityToEnd.end(nil, dismissalPolicy: .immediate)
            }
            await endExistingActivities(excluding: activityToEnd?.id)
        }
    }

    /// Synchronously ends all Live Activities as a bounded best-effort termination cleanup.
    static func endAllActivitiesSync(timeout: TimeInterval = 2.0) {
        let semaphore = DispatchSemaphore(value: 0)
        Task.detached {
            for activity in Activity<FocusRitualAttributes>.activities {
                await activity.end(nil, dismissalPolicy: .immediate)
            }
            semaphore.signal()
        }
        _ = semaphore.wait(timeout: .now() + .milliseconds(Int(timeout * 1000)))
    }

    // MARK: - Private

    private func recoverActivity() -> Activity<FocusRitualAttributes>? {
        let existingActivities = Activity<FocusRitualAttributes>.activities

        if let currentActivity,
           existingActivities.contains(where: { $0.id == currentActivity.id }) {
            return currentActivity
        }

        currentActivity = existingActivities.first
        return currentActivity
    }

    private func endExistingActivities(excluding activityIdToKeep: String? = nil) async {
        for activity in Activity<FocusRitualAttributes>.activities where activity.id != activityIdToKeep {
            await activity.end(nil, dismissalPolicy: .immediate)
        }
    }

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
