
import Foundation
import Capacitor

@objc(MediaSessionControlPlugin)
public class MediaSessionControlPlugin: CAPPlugin {
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initMediaSession", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "play", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pause", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stop", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "next", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "previous", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "seekTo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "updateMetadata", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "updatePlaybackState", returnType: CAPPluginReturnPromise)
    ]

    public let identifier = "MediaSessionControlPlugin"
    public let jsName = "MediaSessionControl"
    
    private let implementation = MediaSessionControl()
    
    @objc func initMediaSession(_ call: CAPPluginCall) {
        implementation.initMedia(call)
    }
    
    @objc func play(_ call: CAPPluginCall) {
        implementation.play()
        call.resolve()
    }
    
    @objc func pause(_ call: CAPPluginCall) {
        implementation.pause()
        call.resolve()
    }
    
    @objc func stop(_ call: CAPPluginCall) {
        implementation.stop()
        call.resolve()
    }
    
    @objc func next(_ call: CAPPluginCall) {
        implementation.next()
        call.resolve()
    }
    
    @objc func previous(_ call: CAPPluginCall) {
        implementation.previous()
        call.resolve()
    }
    
    @objc func seekTo(_ call: CAPPluginCall) {
        implementation.seekTo(call)
    }
    
    @objc func updateMetadata(_ call: CAPPluginCall) {
        implementation.updateMetadata(call)
    }

    // Новый метод
    @objc func updatePlaybackState(_ call: CAPPluginCall) {
        implementation.updatePlaybackState(call)
    }
}
