import SwiftUI
import WidgetKit

struct ExpandedSleepView: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        HStack(spacing: 0) {
            // Far left: Moon icon
            Image(systemName: "moon.fill")
                .font(.system(size: 14))
                .foregroundStyle(Color.white.opacity(0.45))
                .padding(.trailing, 12)

            // Timer
            Group {
                if state.isPaused {
                    Text(state.remainingFormatted)
                } else {
                    Text(timerInterval: Date()...state.timerEndDate, countsDown: true)
                }
            }
            .font(.system(size: 32, weight: .light))
            .foregroundStyle(Color.white.opacity(0.70))
            .monospacedDigit()
            .lineLimit(1)
            .minimumScaleFactor(0.7)

            // Vertical divider
            Rectangle()
                .fill(Color.white.opacity(0.08))
                .frame(width: 1)
                .padding(.horizontal, 14)

            // Right side: Sleep info
            VStack(alignment: .leading, spacing: 5) {
                Text("SLEEP")
                    .font(.system(size: 10, weight: .regular))
                    .tracking(1.0)
                    .foregroundStyle(Color.white.opacity(0.32))

                Text(state.fadeOutLabel)
                    .font(.system(size: 11, weight: .regular))
                    .foregroundStyle(Color.white.opacity(0.45))

                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(Color.white.opacity(0.10))
                            .frame(height: 2)
                        Capsule()
                            .fill(Color.white.opacity(0.30))
                            .frame(width: geo.size.width * state.progress, height: 2)
                    }
                }
                .frame(height: 2)

                Text(state.mixSummary)
                    .font(.system(size: 11, weight: .regular))
                    .foregroundStyle(Color.white.opacity(0.22))
                    .lineLimit(1)
            }

            Spacer(minLength: 0)
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
    }
}
