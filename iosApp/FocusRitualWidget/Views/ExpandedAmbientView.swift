import SwiftUI
import WidgetKit

/// Waveform bars for the expanded DI `.leading` region.
struct ExpandedAmbientLeading: View {
    var body: some View {
        WaveformBars()
            .padding(.leading, 4)
    }
}

/// Text content for the expanded DI `.center` region.
struct ExpandedAmbientCenter: View {
    let state: FocusRitualAttributes.ContentState

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("CURRENT MIX")
                .font(.system(size: 10, weight: .regular))
                .tracking(1.0)
                .foregroundStyle(Color.white.opacity(0.32))

            Text(state.mixSummary)
                .font(.system(size: 15, weight: .regular))
                .foregroundStyle(Color.white.opacity(0.85))
                .lineLimit(1)

            Text("\(state.activeSoundCount) \(state.activeSoundCount == 1 ? "sound" : "sounds") playing")
                .font(.system(size: 11, weight: .regular))
                .foregroundStyle(Color.white.opacity(0.30))
        }
    }
}

// MARK: - Waveform Bars

struct WaveformBars: View {
    private let heights: [CGFloat] = [0.35, 0.75, 0.45, 0.9, 0.3, 0.7, 0.5]
    private let maxHeight: CGFloat = 28

    var body: some View {
        HStack(alignment: .center, spacing: 3) {
            ForEach(0..<heights.count, id: \.self) { i in
                RoundedRectangle(cornerRadius: 1.5)
                    .fill(Color.white.opacity(0.35))
                    .frame(width: 3.5, height: heights[i] * maxHeight)
            }
        }
        .frame(height: maxHeight)
        .contentTransition(.interpolate)
    }
}
