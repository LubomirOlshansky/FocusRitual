import SwiftUI
import WidgetKit

struct ExpandedFocusView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(spacing: 0) {
            // Left: Timer block
            VStack(alignment: .leading, spacing: 4) {
                Group {
                    if state.isPaused {
                        Text(state.remainingFormatted)
                    } else {
                        Text(timerInterval: Date()...state.timerEndDate, countsDown: true)
                    }
                }
                .font(.system(size: 36, weight: .light))
                .foregroundStyle(Color.white.opacity(0.90))
                .monospacedDigit()
                .lineLimit(1)
                .minimumScaleFactor(0.7)

                Text("FOCUS")
                    .font(.system(size: 10, weight: .regular))
                    .tracking(1.0)
                    .foregroundStyle(Color.white.opacity(0.32))
            }

            // Vertical divider
            Rectangle()
                .fill(Color.white.opacity(0.10))
                .frame(width: 1)
                .padding(.horizontal, 16)

            // Center: Cycle info
            VStack(alignment: .leading, spacing: 6) {
                Text(state.cycleLabel.uppercased())
                    .font(.system(size: 10, weight: .regular))
                    .tracking(1.0)
                    .foregroundStyle(Color.white.opacity(0.50))

                HStack(spacing: 4) {
                    ForEach(0..<state.totalCycles, id: \.self) { i in
                        if i < state.currentCycle {
                            Circle()
                                .fill(Color.white.opacity(0.6))
                                .frame(width: 5, height: 5)
                        } else {
                            Circle()
                                .stroke(Color.white.opacity(0.2), lineWidth: 1)
                                .frame(width: 5, height: 5)
                        }
                    }
                }

                Text(state.mixSummary)
                    .font(.system(size: 11, weight: .regular))
                    .foregroundStyle(Color.white.opacity(0.22))
                    .lineLimit(1)
            }

            Spacer(minLength: 8)

            // Far right: Progress arc ring
            arcRing
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
    }

    private var arcRing: some View {
        ZStack {
            Circle()
                .stroke(Color.white.opacity(0.06), lineWidth: 2)
                .frame(width: 36, height: 36)
            Circle()
                .trim(from: 0, to: state.progress)
                .stroke(Color.white.opacity(0.20), style: StrokeStyle(lineWidth: 2, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .frame(width: 36, height: 36)
        }
    }
}
