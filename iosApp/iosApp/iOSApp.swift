import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        #if !targetEnvironment(simulator)
        ScreenTimeBridge.shared.handler = ScreenTimeManager()
        #endif
        LiveActivityBridge.shared.handler = LiveActivityManager()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}