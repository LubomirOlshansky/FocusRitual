import SwiftUI
import WidgetKit

/// State 2 — Focus Session Active.
/// Strongest hierarchy: large countdown, phase, cycle indicator, progress ring.
struct FocusSessionActiveView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(spacing: 0) {
            // Left: timer with progress halo
            timerBlock
                .frame(width: 90)

            Spacer(minLength: 8)

            // Center: labels
            VStack(alignment: .leading, spacing: 4) {
                RitualTokens.label("FOCUS SESSION")

                Text(state.phase)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(RitualTokens.onSurface.opacity(0.8))

                RitualTokens.caption(state.cycleLabel)

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
        .background(focusBackground)
    }

    private var timerBlock: some View {
        ZStack {
            // Subtle halo ring — thin circular progress
            Circle()
                .trim(from: 0, to: state.progress)
                .stroke(
                    RitualTokens.primary.opacity(0.25),
                    style: StrokeStyle(lineWidth: 2, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .frame(width: 72, height: 72)

            // Ghost ring track
            Circle()
                .stroke(
                    RitualTokens.outlineVariant.opacity(0.15),
                    lineWidth: 1
                )
                .frame(width: 72, height: 72)

            // Timer text
            Text(state.remainingFormatted)
                .font(.system(size: 20, weight: .ultraLight, design: .default))
                .kerning(-0.3)
                .foregroundStyle(RitualTokens.onSurface)
                .monospacedDigit()
        }
    }

    private var focusBackground: some View {
        ZStack {
            RitualTokens.surface

            // Structured radial glow — intentional, focused energy
            RadialGradient(
                colors: [
                    RitualTokens.primary.opacity(0.06),
                    RitualTokens.primary.opacity(0.02),
                    Color.clear,
                ],
                center: .leading,
                startRadius: 20,
                endRadius: 180
            )
        }
    }
}
