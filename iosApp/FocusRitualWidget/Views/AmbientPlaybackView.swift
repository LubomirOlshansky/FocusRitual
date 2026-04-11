import SwiftUI
import WidgetKit

/// State 1 — Ambient Playback Only.
/// Lightest and calmest. No large timer. Subtle atmospheric presence.
struct AmbientPlaybackView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(spacing: 0) {
            // Left: content
            VStack(alignment: .leading, spacing: 6) {
                RitualTokens.label("CURRENT MIX")

                Text(state.mixSummary)
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(RitualTokens.onSurface)

                RitualTokens.caption("\(state.activeSoundCount) sound\(state.activeSoundCount == 1 ? "" : "s") active")
            }

            Spacer(minLength: 12)

            // Right: actions
            HStack(spacing: 10) {
                LiveActivityButton(
                    systemImage: state.isPaused ? "play.fill" : "pause.fill",
                    intent: "togglePause"
                )
                LiveActivityButton(
                    systemImage: "stop.fill",
                    style: .secondary,
                    intent: "stop"
                )
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(ambientBackground)
    }

    private var ambientBackground: some View {
        ZStack {
            RitualTokens.surface

            // Soft radial glow — ambient pulse feel
            RadialGradient(
                colors: [
                    RitualTokens.primary.opacity(0.04),
                    Color.clear,
                ],
                center: .leading,
                startRadius: 0,
                endRadius: 200
            )
        }
    }
}
