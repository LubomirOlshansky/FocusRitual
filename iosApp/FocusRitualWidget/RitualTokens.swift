import SwiftUI

/// Design tokens matching FocusRitual's dark cinematic design system.
/// Sourced from the Compose `Color.kt` palette.
enum RitualTokens {

    // MARK: - Surface palette

    static let surface              = Color(hex: 0x0C0E11)
    static let surfaceContainerLow  = Color(hex: 0x111418)
    static let surfaceContainer     = Color(hex: 0x161A1F)
    static let surfaceContainerHigh = Color(hex: 0x1B2027)
    static let surfaceBright        = Color(hex: 0x2A3240)

    /// Live Activity card background — near-black with slight warmth.
    static let liveActivityBackground = Color(red: 28/255, green: 28/255, blue: 30/255).opacity(0.97)

    // MARK: - Content

    static let onSurface            = Color(hex: 0xE0E6F1)
    static let onSurfaceVariant     = Color(hex: 0xA5ABB6)
    static let primary              = Color(hex: 0xB7C8DB)
    static let primaryDim           = Color(hex: 0xA9BBCD)
    static let primaryContainer     = Color(hex: 0x384858)
    static let outlineVariant       = Color(hex: 0x424851)

    // MARK: - Typography

    /// Quiet, understated label — matches the 11sp session labels in-app.
    static func label(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 10, weight: .medium))
            .kerning(1.8)
            .foregroundStyle(onSurfaceVariant.opacity(0.6))
    }

    /// Large countdown — matches the 64sp timer in ActiveSessionScreen.
    static func timer(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 32, weight: .ultraLight, design: .default))
            .kerning(-0.5)
            .foregroundStyle(onSurface)
            .monospacedDigit()
    }

    /// Primary line — mix summary or phase info.
    static func body(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 13, weight: .regular))
            .foregroundStyle(onSurfaceVariant.opacity(0.8))
    }

    /// Secondary / supporting text.
    static func caption(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 11, weight: .regular))
            .foregroundStyle(onSurfaceVariant.opacity(0.5))
    }
}

// MARK: - Color hex initializer

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
