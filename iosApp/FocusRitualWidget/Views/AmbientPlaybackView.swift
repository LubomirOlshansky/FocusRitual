import SwiftUI
import WidgetKit

/// State 1 — Ambient Playback Only (Lock Screen).
/// Waveform bars left, text stack right. Calm, informational.
struct AmbientPlaybackView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(spacing: 12) {
            // Waveform bars
            HStack(alignment: .center, spacing: 2) {
                ForEach(Array([5.0, 13.0, 9.0, 16.0, 7.0].enumerated()), id: \.offset) { _, height in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(Color.white.opacity(0.4))
                        .frame(width: 2.5, height: height)
                }
            }
            .frame(width: 28)

            // Text stack
            VStack(alignment: .leading, spacing: 2) {
                Text("CURRENT MIX")
                    .font(.system(size: 10, weight: .light))
                    .tracking(1.0)
                    .foregroundStyle(Color.white.opacity(0.28))

                Text(state.mixSummary)
                    .font(.system(size: 16, weight: .light))
                    .foregroundStyle(Color.white.opacity(0.85))

                Text("\(state.activeSoundCount) \(state.activeSoundCount == 1 ? "sound" : "sounds") playing")
                    .font(.system(size: 11, weight: .light))
                    .foregroundStyle(Color.white.opacity(0.22))
            }

            Spacer(minLength: 8)

            // Action buttons
            HStack(spacing: 8) {
                LiveActivityButton(
                    systemImage: state.isPaused ? "play.fill" : "pause.fill",
                    intent: TogglePauseIntent()
                )
                LiveActivityButton(
                    systemImage: "stop.fill",
                    style: .secondary,
                    intent: StopMixIntent()
                )
            }
        }
        .padding(.horizontal, 15)
        .padding(.vertical, 13)
    }
}
