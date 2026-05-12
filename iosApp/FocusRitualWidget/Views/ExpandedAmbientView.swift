import SwiftUI
import WidgetKit

// MARK: - Orb

struct OrbView: View {
    var isBreathing: Bool
    private let primary = Color(red: 0.72, green: 0.78, blue: 0.86)
    var body: some View {
        ZStack {
            Circle().strokeBorder(primary.opacity(0.07), lineWidth: 0.5)
                .frame(width: 62, height: 62)
                .scaleEffect(isBreathing ? 1.06 : 1.0)
                .opacity(isBreathing ? 0.65 : 0.28)
                .animation(.easeInOut(duration: 4.5).repeatForever(autoreverses: true), value: isBreathing)
            Circle().strokeBorder(primary.opacity(0.07), lineWidth: 0.5)
                .frame(width: 55, height: 55)
                .scaleEffect(isBreathing ? 1.06 : 1.0)
                .opacity(isBreathing ? 0.65 : 0.28)
                .animation(.easeInOut(duration: 4.5).delay(0.8).repeatForever(autoreverses: true), value: isBreathing)
            Circle().fill(RadialGradient(colors: [
                Color(red: 0.23, green: 0.31, blue: 0.41).opacity(0.85),
                Color(red: 0.07, green: 0.10, blue: 0.15).opacity(0.95)
            ], center: UnitPoint(x: 0.35, y: 0.30), startRadius: 0, endRadius: 24))
                .frame(width: 48, height: 48)
                .overlay(Circle().strokeBorder(primary.opacity(0.16), lineWidth: 0.5))
            ZStack {
                Circle().strokeBorder(primary.opacity(0.35), lineWidth: 0.7).frame(width: 9, height: 9)
                Circle().fill(primary.opacity(0.18)).frame(width: 4, height: 4)
            }
        }.frame(width: 48, height: 48)
    }
}

// MARK: - Text Stack

struct TextStack: View {
    let name: String
    let subtitle: String
    private let primary = Color(red: 0.72, green: 0.78, blue: 0.86)
    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("NOW PLAYING").font(.system(size: 9, weight: .light)).kerning(1.0)
                .foregroundColor(primary.opacity(0.35))
            Text(name).font(.system(size: 17, weight: .light)).tracking(-0.4)
                .foregroundColor(.white.opacity(0.88)).lineLimit(1).truncationMode(.tail)
            Text(subtitle).font(.system(size: 11, weight: .light))
                .foregroundColor(.white.opacity(0.30)).lineLimit(1).truncationMode(.tail)
        }.frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Live Dot

struct LiveDot: View {
    var isPulsing: Bool
    private let primary = Color(red: 0.72, green: 0.78, blue: 0.86)
    var body: some View {
        Circle().fill(primary.opacity(isPulsing ? 0.90 : 0.35))
            .frame(width: 6, height: 6)
            .scaleEffect(isPulsing ? 1.25 : 1.0)
            .animation(.easeInOut(duration: 2.5).repeatForever(autoreverses: true), value: isPulsing)
    }
}

// MARK: - Expanded Dynamic Island Center (Ambient)

struct ExpandedAmbientCenter: View {
    let state: FocusRitualAttributes.ContentState
    @State private var isBreathing = false
    @State private var isLivePulsing = false

    var body: some View {
        HStack(spacing: 12) {
            OrbView(isBreathing: isBreathing)
            TextStack(name: state.displayName, subtitle: state.displaySubtitle)
            LiveDot(isPulsing: isLivePulsing)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .onAppear {
            isBreathing = true
            isLivePulsing = true
        }
    }
}
