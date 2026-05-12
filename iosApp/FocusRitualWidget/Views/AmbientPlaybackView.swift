import SwiftUI
import WidgetKit

/// State 1 — Ambient Playback Only (Lock Screen).
/// Orb identity + text stack + live dot. No action buttons.
struct AmbientPlaybackView: View {
    let state: FocusRitualAttributes.ContentState
    @State private var isBreathing = false
    @State private var isLivePulsing = false

    var body: some View {
        HStack(spacing: 12) {
            OrbView(isBreathing: isBreathing)
            TextStack(name: state.displayName, subtitle: state.displaySubtitle)
            LiveDot(isPulsing: isLivePulsing)
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 16)
        .onAppear {
            isBreathing = true
            isLivePulsing = true
        }
    }
}
