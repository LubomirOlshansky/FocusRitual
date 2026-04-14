import SwiftUI
import ComposeApp
import ActivityKit

final class AppDelegate: NSObject, UIApplicationDelegate {
    func applicationWillTerminate(_ application: UIApplication) {
        LiveActivityManager.endAllActivitiesSync()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    private let actionObserver = LiveActivityActionObserver()

    init() {
        #if !targetEnvironment(simulator)
        ScreenTimeBridge.shared.handler = ScreenTimeManager()
        #endif
        LiveActivityBridge.shared.handler = LiveActivityManager()
        actionObserver.startObserving()

        // Clean up any stale activities from a previous app session
        // (e.g., if the app was force-quit and activities survived)
        LiveActivityManager.endAllActivitiesSync()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}