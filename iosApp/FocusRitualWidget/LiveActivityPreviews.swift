import SwiftUI

// MARK: - Card Preview Helper

/// Wraps lock screen views in the Live Activity card background for visual validation.
private struct CardPreview<Content: View>: View {
    let content: Content
    init(@ViewBuilder content: () -> Content) { self.content = content() }
    var body: some View {
        content
            .background(RitualTokens.liveActivityBackground)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color.white.opacity(0.06), lineWidth: 0.5))
            .padding(20)
            .background(Color(red: 0.067, green: 0.067, blue: 0.067))
    }
}

// MARK: - Ambient Previews

#Preview("Ambient Playback") {
    CardPreview {
        AmbientPlaybackView(
            state: .ambient(
                isPaused: false,
                mixSummary: "Rain • Wind",
                activeSoundCount: 3
            )
        )
    }
    .previewDisplayName("Ambient — Playing")
}

#Preview("Ambient Paused") {
    CardPreview {
        AmbientPlaybackView(
            state: .ambient(
                isPaused: true,
                mixSummary: "Rain • Wind",
                activeSoundCount: 3
            )
        )
    }
    .previewDisplayName("Ambient — Paused")
}

// MARK: - Focus Previews

#Preview("Focus Session") {
    CardPreview {
        FocusSessionActiveView(
            state: .focus(
                isPaused: false,
                mixSummary: "Rain • Wind",
                remainingSeconds: 1500,
                totalSeconds: 1500,
                phase: "Focus",
                currentCycle: 1,
                totalCycles: 4,
                endTimestamp: Date().timeIntervalSince1970 + 1500
            )
        )
    }
    .previewDisplayName("Focus — Full")
}

#Preview("Focus Mid-Session") {
    CardPreview {
        FocusSessionActiveView(
            state: .focus(
                isPaused: false,
                mixSummary: "Rain • Wind",
                remainingSeconds: 845,
                totalSeconds: 1500,
                phase: "Focus",
                currentCycle: 2,
                totalCycles: 4,
                endTimestamp: Date().timeIntervalSince1970 + 845
            )
        )
    }
    .previewDisplayName("Focus — Mid")
}

#Preview("Focus Break") {
    CardPreview {
        FocusSessionActiveView(
            state: .focus(
                isPaused: false,
                mixSummary: "Forest • Stream",
                remainingSeconds: 180,
                totalSeconds: 300,
                phase: "Break",
                currentCycle: 2,
                totalCycles: 4,
                endTimestamp: Date().timeIntervalSince1970 + 180
            )
        )
    }
    .previewDisplayName("Focus — Break")
}

// MARK: - Sleep Previews

#Preview("Sleep Session") {
    CardPreview {
        SleepSessionActiveView(
            state: .sleep(
                isPaused: false,
                mixSummary: "Rain • Brown Noise",
                remainingSeconds: 2300,
                totalSeconds: 2700,
                fadeOutMinutes: 10,
                endTimestamp: Date().timeIntervalSince1970 + 2300
            )
        )
    }
    .previewDisplayName("Sleep — Active")
}

#Preview("Sleep Paused") {
    CardPreview {
        SleepSessionActiveView(
            state: .sleep(
                isPaused: true,
                mixSummary: "Rain • Brown Noise",
                remainingSeconds: 1200,
                totalSeconds: 2700,
                fadeOutMinutes: 10,
                endTimestamp: Date().timeIntervalSince1970 + 1200
            )
        )
    }
    .previewDisplayName("Sleep — Paused")
}

// MARK: - Expanded Dynamic Island Previews

#Preview("Expanded — Ambient") {
    HStack(spacing: 14) {
        ExpandedAmbientLeading()
        ExpandedAmbientCenter(
            state: .ambient(
                isPaused: false,
                mixSummary: "Rain · Wind · Forest",
                activeSoundCount: 3
            )
        )
        Spacer()
    }
    .padding(.horizontal, 20)
    .padding(.vertical, 16)
    .frame(height: 84)
    .background(Color.black)
    .clipShape(RoundedRectangle(cornerRadius: 26))
    .previewDisplayName("Expanded — Ambient")
}

#Preview("Expanded — Focus") {
    ExpandedFocusView(
        state: .focus(
            isPaused: false,
            mixSummary: "Rain · Wind",
            remainingSeconds: 1499,
            totalSeconds: 1500,
            phase: "Focus",
            currentCycle: 1,
            totalCycles: 4,
            endTimestamp: Date().timeIntervalSince1970 + 1499
        )
    )
    .frame(height: 84)
    .background(Color.black)
    .clipShape(RoundedRectangle(cornerRadius: 26))
    .previewDisplayName("Expanded — Focus")
}

#Preview("Expanded — Focus Mid") {
    ExpandedFocusView(
        state: .focus(
            isPaused: false,
            mixSummary: "Rain · Wind",
            remainingSeconds: 845,
            totalSeconds: 1500,
            phase: "Focus",
            currentCycle: 2,
            totalCycles: 4,
            endTimestamp: Date().timeIntervalSince1970 + 845
        )
    )
    .frame(height: 84)
    .background(Color.black)
    .clipShape(RoundedRectangle(cornerRadius: 26))
    .previewDisplayName("Expanded — Focus Mid")
}

#Preview("Expanded — Sleep") {
    ExpandedSleepView(
        state: .sleep(
            isPaused: false,
            mixSummary: "Rain · Wind",
            remainingSeconds: 2697,
            totalSeconds: 2700,
            fadeOutMinutes: 10,
            endTimestamp: Date().timeIntervalSince1970 + 2697
        )
    )
    .frame(height: 84)
    .background(Color.black)
    .clipShape(RoundedRectangle(cornerRadius: 26))
    .previewDisplayName("Expanded — Sleep")
}
