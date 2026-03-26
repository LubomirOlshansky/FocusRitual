import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        BackgroundAwareHostViewController(MainViewControllerKt.MainViewController())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// Wraps the Compose UIViewController and pauses its Metal rendering loop when
/// the app enters background, preventing "Insufficient Permission" GPU errors.
final class BackgroundAwareHostViewController: UIViewController {
    private let inner: UIViewController
    private var observers: [NSObjectProtocol] = []

    init(_ inner: UIViewController) {
        self.inner = inner
        super.init(nibName: nil, bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError() }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(inner)
        inner.view.frame = view.bounds
        inner.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(inner.view)
        inner.didMove(toParent: self)

        let nc = NotificationCenter.default
        observers.append(nc.addObserver(
            forName: UIApplication.willResignActiveNotification,
            object: nil, queue: .main
        ) { [weak self] _ in
            self?.inner.beginAppearanceTransition(false, animated: false)
            self?.inner.endAppearanceTransition()
        })
        observers.append(nc.addObserver(
            forName: UIApplication.didBecomeActiveNotification,
            object: nil, queue: .main
        ) { [weak self] _ in
            self?.inner.beginAppearanceTransition(true, animated: false)
            self?.inner.endAppearanceTransition()
        })
    }

    deinit {
        observers.forEach { NotificationCenter.default.removeObserver($0) }
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

