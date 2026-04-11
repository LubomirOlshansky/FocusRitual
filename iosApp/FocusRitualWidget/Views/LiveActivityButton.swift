import SwiftUI

/// Reusable action button for the Live Activity surface.
/// Matches FocusRitual's dark cinematic aesthetic — no bright accents,
/// tonal depth only.
struct LiveActivityButton: View {

    enum Style {
        case primary
        case secondary
    }

    let systemImage: String
    var style: Style = .primary
    let intent: String

    var body: some View {
        // In iOS 17.1+ these would be wrapped in a Button with an AppIntent.
        // For now, placeholder layout — intent wiring comes at integration time.
        ZStack {
            Circle()
                .fill(backgroundColor)
                .frame(width: 32, height: 32)

            Image(systemName: systemImage)
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(iconColor)
        }
    }

    private var backgroundColor: Color {
        switch style {
        case .primary:
            return RitualTokens.primaryContainer.opacity(0.4)
        case .secondary:
            return RitualTokens.surfaceContainerHigh.opacity(0.6)
        }
    }

    private var iconColor: Color {
        switch style {
        case .primary:
            return RitualTokens.primary
        case .secondary:
            return RitualTokens.onSurfaceVariant.opacity(0.6)
        }
    }
}
