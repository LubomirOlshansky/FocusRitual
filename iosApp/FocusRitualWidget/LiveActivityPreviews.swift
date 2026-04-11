import SwiftUI

/// Preview harness for all 3 Live Activity states.
/// Use Xcode Previews to visually validate the design.
#Preview("Ambient Playback") {
    AmbientPlaybackView(
        state: .ambient(
            isPaused: false,
            mixSummary: "Rain • Wind",
            activeSoundCount: 3
        )
    )
    .previewDisplayName("Ambient — Playing")
}

#Preview("Ambient Paused") {
    AmbientPlaybackView(
        state: .ambient(
            isPaused: true,
            mixSummary: "Rain • Wind",
            activeSoundCount: 3
        )
    )
    .previewDisplayName("Ambient — Paused")
}

#Preview("Focus Session") {
    FocusSessionActiveView(
        state: .focus(
            isPaused: false,
            mixSummary: "Rain • Wind",
            remainingSeconds: 1500,
            totalSeconds: 1500,
            phase: "Focus",
            currentCycle: 1,
            totalCycles: 4
        )
    )
    .previewDisplayName("Focus — Full")
}

#Preview("Focus Mid-Session") {
    FocusSessionActiveView(
        state: .focus(
            isPaused: false,
            mixSummary: "Rain • Wind",
            remainingSeconds: 845,
            totalSeconds: 1500,
            phase: "Focus",
            currentCycle: 2,
            totalCycles: 4
        )
    )
    .previewDisplayName("Focus — Mid")
}

#Preview("Focus Break") {
    FocusSessionActiveView(
        state: .focus(
            isPaused: false,
            mixSummary: "Forest • Stream",
            remainingSeconds: 180,
            totalSeconds: 300,
            phase: "Break",
            currentCycle: 2,
            totalCycles: 4
        )
    )
    .previewDisplayName("Focus — Break")
}

#Preview("Sleep Session") {
    SleepSessionActiveView(
        state: .sleep(
            isPaused: false,
            mixSummary: "Rain • Brown Noise",
            remainingSeconds: 2300,
            totalSeconds: 2700,
            fadeOutMinutes: 10
        )
    )
    .previewDisplayName("Sleep — Active")
}

#Preview("Sleep Paused") {
    SleepSessionActiveView(
        state: .sleep(
            isPaused: true,
            mixSummary: "Rain • Brown Noise",
            remainingSeconds: 1200,
            totalSeconds: 2700,
            fadeOutMinutes: 10
        )
    )
    .previewDisplayName("Sleep — Paused")
}
