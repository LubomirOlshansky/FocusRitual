import ActivityKit
import WidgetKit

/// ActivityAttributes for the FocusRitual Live Activity.
/// Static `sessionType` determines which visual layout is used.
/// ContentState carries all dynamic fields — optional where state-specific.
struct FocusRitualAttributes: ActivityAttributes {

    // MARK: - Static (set once at activity start)

    /// "ambient", "focus", or "sleep"
    let sessionType: String

    // MARK: - Dynamic content

    struct ContentState: Codable, Hashable {
        // Common to all states
        let isPaused: Bool
        let mixSummary: String

        // Ambient-only
        let activeSoundCount: Int

        // Focus + Sleep
        let remainingSeconds: Int
        let totalSeconds: Int

        // Focus-only
        let phase: String       // "Focus" / "Break"
        let currentCycle: Int
        let totalCycles: Int

        // Sleep-only
        let fadeOutMinutes: Int

        // Timer endpoint (seconds since epoch) for native countdown
        let endTimestamp: TimeInterval

        var timerEndDate: Date {
            Date(timeIntervalSince1970: endTimestamp)
        }

        // MARK: Derived

        var progress: Double {
            guard totalSeconds > 0 else { return 0 }
            return 1.0 - (Double(remainingSeconds) / Double(totalSeconds))
        }

        var remainingFormatted: String {
            let m = remainingSeconds / 60
            let s = remainingSeconds % 60
            return String(format: "%d:%02d", m, s)
        }

        var cycleLabel: String {
            "Cycle \(currentCycle) of \(totalCycles)"
        }

        var fadeOutLabel: String {
            "Fade out in \(fadeOutMinutes) min"
        }
    }
}

// MARK: - Convenience initializers for each state

extension FocusRitualAttributes.ContentState {

    static func ambient(
        isPaused: Bool,
        mixSummary: String,
        activeSoundCount: Int
    ) -> Self {
        .init(
            isPaused: isPaused,
            mixSummary: mixSummary,
            activeSoundCount: activeSoundCount,
            remainingSeconds: 0,
            totalSeconds: 0,
            phase: "",
            currentCycle: 0,
            totalCycles: 0,
            fadeOutMinutes: 0,
            endTimestamp: 0
        )
    }

    static func focus(
        isPaused: Bool,
        mixSummary: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        phase: String,
        currentCycle: Int,
        totalCycles: Int,
        endTimestamp: TimeInterval
    ) -> Self {
        .init(
            isPaused: isPaused,
            mixSummary: mixSummary,
            activeSoundCount: 0,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            phase: phase,
            currentCycle: currentCycle,
            totalCycles: totalCycles,
            fadeOutMinutes: 0,
            endTimestamp: endTimestamp
        )
    }

    static func sleep(
        isPaused: Bool,
        mixSummary: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        fadeOutMinutes: Int,
        endTimestamp: TimeInterval
    ) -> Self {
        .init(
            isPaused: isPaused,
            mixSummary: mixSummary,
            activeSoundCount: 0,
            remainingSeconds: remainingSeconds,
            totalSeconds: totalSeconds,
            phase: "",
            currentCycle: 0,
            totalCycles: 0,
            fadeOutMinutes: fadeOutMinutes,
            endTimestamp: endTimestamp
        )
    }
}
