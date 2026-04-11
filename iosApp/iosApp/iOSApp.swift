import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        ScreenTimeBridge.shared.handler = ScreenTimeManager()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}