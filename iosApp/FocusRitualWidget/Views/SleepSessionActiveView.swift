import SwiftUI
import WidgetKit

/// State 3 — Sleep Session Active.
/// Softer and calmer than focus. No cycle info. Gentle, drifting, quiet.
struct SleepSessionActiveView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(spacing: 0) {
            // Left: content
            VStack(alignment: .leading, spacing: 5) {
                RitualTokens.label("SLEEP SESSION")

                Text(state.remainingFormatted)
                    .font(.system(size: 24, weight: .ultraLight, design: .default))
                    .kerning(-0.3)
                    .foregroundStyle(RitualTokens.onSurface.opacity(0.85))
                    .monospacedDigit()

                RitualTokens.caption(state.fadeOutLabel)

                RitualTokens.caption(state.mixSummary)
            }

            Spacer(minLength: 12)

            // Right: actions
            VStack(spacing: 10) {
                LiveActivityButton(
                    systemImage: state.isPaused ? "play.fill" : "pause.fill",
                    intent: "togglePause"
                )
                LiveActivityButton(
                    systemImage: "xmark",
                    style: .secondary,
                    intent: "end"
                )
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(sleepBackground)
    }

    private var sleepBackground: some View {
        ZStack {
            RitualTokens.surface

            // Softer, wider glow — gentle, drifting feel
            RadialGradient(
                colors: [
                    RitualTokens.primaryDim.opacity(0.03),
                    Color.clear,
                ],
                center: .center,
                startRadius: 0,
                endRadius: 250
            )
        }
    }
}
