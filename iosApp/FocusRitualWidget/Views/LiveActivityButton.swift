import AppIntents
import SwiftUI

/// Reusable action button for the Live Activity surface.
/// Matches FocusRitual's dark cinematic aesthetic — no bright accents,
/// tonal depth only. Circular, quiet, minimal.
/// Wraps in Button(intent:) so taps execute the intent without opening the app.
struct LiveActivityButton<I: LiveActivityIntent>: View {

    enum Style {
        case primary   // Tonal fill — default action
        case secondary // Lighter tonal fill — secondary action
        case ghost     // Near-invisible — quietest possible
    }

    let systemImage: String
    var style: Style = .primary
    let intent: I

    var body: some View {
        Button(intent: intent) {
            ZStack {
                Circle()
                    .fill(backgroundColor)
                    .frame(width: 30, height: 30)

                Image(systemName: systemImage)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(iconColor)
            }
        }
        .buttonStyle(.plain)
    }

    private var backgroundColor: Color {
        switch style {
        case .primary:
            return RitualTokens.primaryContainer.opacity(0.35)
        case .secondary:
            return RitualTokens.surfaceContainerHigh.opacity(0.5)
        case .ghost:
            return RitualTokens.surfaceContainerHigh.opacity(0.3)
        }
    }

    private var iconColor: Color {
        switch style {
        case .primary:
            return RitualTokens.primary.opacity(0.9)
        case .secondary:
            return RitualTokens.onSurfaceVariant.opacity(0.55)
        case .ghost:
            return RitualTokens.onSurfaceVariant.opacity(0.4)
        }
    }
}
