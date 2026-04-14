import SwiftUI
import WidgetKit

/// State 3 — Sleep Session Active (Lock Screen).
/// Moon + timer left | vertical divider | fade-out info right. Softer/dimmer than Focus.
struct SleepSessionActiveView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(alignment: .center, spacing: 0) {
            // Left block: moon icon + timer
            VStack(alignment: .leading, spacing: 6) {
                Image(systemName: "moon.fill")
                    .font(.system(size: 22))
                    .foregroundStyle(Color.white.opacity(0.44))

                Group {
                    if state.isPaused {
                        Text(state.remainingFormatted)
                    } else {
                        Text(timerInterval: Date()...state.timerEndDate, countsDown: true)
                    }
                }
                .font(.system(size: 32, weight: .ultraLight))
                .foregroundStyle(Color.white.opacity(0.62))
                .monospacedDigit()
            }
            .padding(.trailing, 13)

            // Vertical divider — dimmer than Focus
            Rectangle()
                .fill(Color.white.opacity(0.06))
                .frame(width: 0.5)
                .padding(.vertical, 2)

            // Right block: sleep meta
            VStack(alignment: .leading, spacing: 4) {
                Text("SLEEP")
                    .font(.system(size: 10, weight: .light))
                    .tracking(1.0)
                    .foregroundStyle(Color.white.opacity(0.20))

                Text(state.fadeOutLabel)
                    .font(.system(size: 13, weight: .light))
                    .foregroundStyle(Color.white.opacity(0.42))

                Spacer().frame(height: 3)

                // Progress bar — more washed out than Focus
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.white.opacity(0.07))
                            .frame(height: 1.5)
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.white.opacity(0.20))
                            .frame(width: geo.size.width * CGFloat(state.progress), height: 1.5)
                    }
                }
                .frame(height: 1.5)

                Spacer().frame(height: 2)

                Text(state.mixSummary)
                    .font(.system(size: 11, weight: .light))
                    .foregroundStyle(Color.white.opacity(0.17))
            }
            .padding(.leading, 13)

            Spacer(minLength: 8)

            // Action buttons
            VStack(spacing: 8) {
                LiveActivityButton(
                    systemImage: state.isPaused ? "play.fill" : "pause.fill",
                    intent: TogglePauseIntent()
                )
                LiveActivityButton(
                    systemImage: "xmark",
                    style: .secondary,
                    intent: EndSessionIntent()
                )
            }
        }
        .padding(.horizontal, 15)
        .padding(.vertical, 13)
    }
}
