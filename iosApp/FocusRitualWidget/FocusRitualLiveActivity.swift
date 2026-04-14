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
            .activityBackgroundTint(RitualTokens.liveActivityBackground)

        } dynamicIsland: { context in
            DynamicIsland {
                // MARK: - Expanded Dynamic Island
                DynamicIslandExpandedRegion(.leading) {
                    if context.attributes.sessionType == "ambient" {
                        ExpandedAmbientLeading()
                    }
                }
                DynamicIslandExpandedRegion(.trailing) { EmptyView() }
                DynamicIslandExpandedRegion(.center) {
                    if context.attributes.sessionType == "ambient" {
                        ExpandedAmbientCenter(state: context.state)
                    }
                }
                DynamicIslandExpandedRegion(.bottom) {
                    if context.attributes.sessionType != "ambient" {
                        expandedContent(
                            sessionType: context.attributes.sessionType,
                            state: context.state
                        )
                    }
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
    private func expandedContent(
        sessionType: String,
        state: FocusRitualAttributes.ContentState
    ) -> some View {
        switch sessionType {
        case "focus":
            ExpandedFocusView(state: state)
        case "sleep":
            ExpandedSleepView(state: state)
        default:
            EmptyView()
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
            Image(systemName: "stop.fill")
                .font(.system(size: 10, weight: .medium))
                .foregroundStyle(RitualTokens.onSurfaceVariant.opacity(0.5))
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
