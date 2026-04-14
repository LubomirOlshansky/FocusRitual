import SwiftUI
import WidgetKit

/// State 2 — Focus Session Active (Lock Screen).
/// Timer left | vertical divider | cycle dots + progress right.
struct FocusSessionActiveView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(alignment: .center, spacing: 0) {
            // Left block: timer + phase
            VStack(alignment: .leading, spacing: 4) {
                Group {
                    if state.isPaused {
                        Text(state.remainingFormatted)
                    } else {
                        Text(timerInterval: Date()...state.timerEndDate, countsDown: true)
                    }
                }
                .font(.system(size: 36, weight: .ultraLight))
                .foregroundStyle(Color.white.opacity(0.88))
                .monospacedDigit()

                Text(state.phase.uppercased())
                    .font(.system(size: 10, weight: .light))
                    .tracking(1.2)
                    .foregroundStyle(Color.white.opacity(0.28))
            }
            .padding(.trailing, 13)

            // Vertical divider
            Rectangle()
                .fill(Color.white.opacity(0.08))
                .frame(width: 0.5)
                .padding(.vertical, 2)

            // Right block: cycle info + progress
            VStack(alignment: .leading, spacing: 0) {
                Text("CYCLE \(state.currentCycle) OF \(state.totalCycles)")
                    .font(.system(size: 10, weight: .light))
                    .tracking(1.0)
                    .foregroundStyle(Color.white.opacity(0.26))
                    .monospacedDigit()

                Spacer().frame(height: 5)

                // Cycle dots
                HStack(spacing: 5) {
                    ForEach(0..<state.totalCycles, id: \.self) { i in
                        Circle()
                            .fill(Color.white.opacity(i < state.currentCycle ? 0.7 : 0.14))
                            .frame(width: 5, height: 5)
                    }
                }

                Spacer().frame(height: 9)

                // Progress bar
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.white.opacity(0.08))
                            .frame(height: 1.5)
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color.white.opacity(0.35))
                            .frame(width: geo.size.width * CGFloat(state.progress), height: 1.5)
                    }
                }
                .frame(height: 1.5)

                Spacer().frame(height: 6)

                Text(state.mixSummary)
                    .font(.system(size: 11, weight: .light))
                    .foregroundStyle(Color.white.opacity(0.2))
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
