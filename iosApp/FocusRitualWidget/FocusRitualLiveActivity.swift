import ActivityKit
import SwiftUI
import WidgetKit

/// The Live Activity widget for FocusRitual.
/// Dispatches to 3 distinct views based on `sessionType`.
struct FocusRitualLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: FocusRitualAttributes.self) { context in
            // MARK: - Lock Screen / Banner presentation
            lockScreenView(
                sessionType: context.attributes.sessionType,
                state: context.state
            )
            .activityBackgroundTint(RitualTokens.surface)

        } dynamicIsland: { context in
            DynamicIsland {
                // MARK: - Expanded Dynamic Island
                DynamicIslandExpandedRegion(.leading) {
                    expandedLeading(
                        sessionType: context.attributes.sessionType,
                        state: context.state
                    )
                }
                DynamicIslandExpandedRegion(.trailing) {
                    expandedTrailing(state: context.state)
                }
                DynamicIslandExpandedRegion(.center) {
                    expandedCenter(
                        sessionType: context.attributes.sessionType,
                        state: context.state
                    )
                }
                DynamicIslandExpandedRegion(.bottom) {
                    expandedBottom(state: context.state)
                }
            } compactLeading: {
                compactLeading(
                    sessionType: context.attributes.sessionType,
                    state: context.state
                )
            } compactTrailing: {
                compactTrailing(
                    sessionType: context.attributes.sessionType,
                    state: context.state
                )
            } minimal: {
                minimalView(
                    sessionType: context.attributes.sessionType,
                    state: context.state
                )
            }
            .contentMargins(.all, 0, for: .expanded)
        }
    }

    // MARK: - Lock Screen

    @ViewBuilder
    private func lockScreenView(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        switch sessionType {
        case "ambient":
            AmbientPlaybackView(state: state)
        case "focus":
            FocusSessionActiveView(state: state)
        case "sleep":
            SleepSessionActiveView(state: state)
        default:
            AmbientPlaybackView(state: state)
        }
    }

    // MARK: - Dynamic Island: Expanded

    @ViewBuilder
    private func expandedLeading(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        switch sessionType {
        case "focus":
            // Progress ring + timer
            ZStack {
                Circle()
                    .trim(from: 0, to: state.progress)
                    .stroke(
                        RitualTokens.primary.opacity(0.3),
                        style: StrokeStyle(lineWidth: 2, lineCap: .round)
                    )
                    .rotationEffect(.degrees(-90))
                    .frame(width: 44, height: 44)

                Text(state.remainingFormatted)
                    .font(.system(size: 12, weight: .light))
                    .foregroundStyle(RitualTokens.onSurface)
                    .monospacedDigit()
            }
        case "sleep":
            Text(state.remainingFormatted)
                .font(.system(size: 16, weight: .ultraLight))
                .foregroundStyle(RitualTokens.onSurface.opacity(0.85))
                .monospacedDigit()
        default:
            EmptyView()
        }
    }

    @ViewBuilder
    private func expandedTrailing(
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        LiveActivityButton(
            systemImage: state.isPaused ? "play.fill" : "pause.fill",
            intent: "togglePause"
        )
    }

    @ViewBuilder
    private func expandedCenter(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        VStack(spacing: 2) {
            switch sessionType {
            case "ambient":
                RitualTokens.label("CURRENT MIX")
                Text(state.mixSummary)
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(RitualTokens.onSurface)
            case "focus":
                RitualTokens.label("FOCUS SESSION")
                RitualTokens.caption("\(state.phase) · \(state.cycleLabel)")
            case "sleep":
                RitualTokens.label("SLEEP SESSION")
                RitualTokens.caption(state.fadeOutLabel)
            default:
                EmptyView()
            }
        }
    }

    @ViewBuilder
    private func expandedBottom(
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        if !state.mixSummary.isEmpty {
            RitualTokens.caption(state.mixSummary)
                .padding(.top, 4)
        }
    }

    // MARK: - Dynamic Island: Compact

    @ViewBuilder
    private func compactLeading(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        switch sessionType {
        case "focus":
            // Tiny progress arc
            ZStack {
                Circle()
                    .trim(from: 0, to: state.progress)
                    .stroke(RitualTokens.primary.opacity(0.4), lineWidth: 2)
                    .rotationEffect(.degrees(-90))
                    .frame(width: 16, height: 16)
            }
        case "sleep":
            Image(systemName: "moon.fill")
                .font(.system(size: 10))
                .foregroundStyle(RitualTokens.primaryDim.opacity(0.6))
        default:
            // Ambient: subtle wave icon
            Image(systemName: "waveform")
                .font(.system(size: 10))
                .foregroundStyle(RitualTokens.primary.opacity(0.5))
        }
    }

    @ViewBuilder
    private func compactTrailing(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        switch sessionType {
        case "focus", "sleep":
            Text(state.remainingFormatted)
                .font(.system(size: 12, weight: .light))
                .foregroundStyle(RitualTokens.onSurface.opacity(0.7))
                .monospacedDigit()
        default:
            // Ambient: pause indicator
            Image(systemName: state.isPaused ? "play.fill" : "pause.fill")
                .font(.system(size: 10))
                .foregroundStyle(RitualTokens.onSurfaceVariant.opacity(0.5))
        }
    }

    // MARK: - Dynamic Island: Minimal

    @ViewBuilder
    private func minimalView(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        switch sessionType {
        case "focus":
            ZStack {
                Circle()
                    .trim(from: 0, to: state.progress)
                    .stroke(RitualTokens.primary.opacity(0.4), lineWidth: 2)
                    .rotationEffect(.degrees(-90))
                    .frame(width: 22, height: 22)
            }
        case "sleep":
            Image(systemName: "moon.fill")
                .font(.system(size: 10))
                .foregroundStyle(RitualTokens.primaryDim.opacity(0.5))
        default:
            Image(systemName: "waveform")
                .font(.system(size: 10))
                .foregroundStyle(RitualTokens.primary.opacity(0.4))
        }
    }
}
