import Foundation

@objc public class MediaSessionControl: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
