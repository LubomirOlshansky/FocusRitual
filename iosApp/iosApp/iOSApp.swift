import SwiftUI
import UIKit
import ComposeApp

final class FocusRitualAppDelegate: NSObject, UIApplicationDelegate {

    private let liveActivityManager = LiveActivityManager()
    private let liveActivityActionObserver = LiveActivityActionObserver()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        #if !targetEnvironment(simulator)
        ScreenTimeBridge.shared.handler = ScreenTimeManager()
        #endif
        LiveActivityBridge.shared.handler = liveActivityManager
        liveActivityActionObserver.startObserving()
        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        LiveActivityManager.endAllActivitiesSync()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(FocusRitualAppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}