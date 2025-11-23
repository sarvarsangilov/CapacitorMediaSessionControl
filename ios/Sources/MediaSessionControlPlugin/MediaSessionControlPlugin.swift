// ios/Sources/MediaSessionControlPlugin/MediaSessionControlPlugin.swift

import Foundation
import Capacitor
import MediaPlayer
import UIKit
import AVFoundation

@objc(MediaSessionControlPlugin)
public class MediaSessionControlPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "MediaSessionControlPlugin"
    public let jsName = "MediaSessionControl"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "init", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "play", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pause", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stop", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "next", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "previous", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "seekTo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "updateMetadata", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "updatePlaybackState", returnType: CAPPluginReturnPromise)
    ]
    
    private var nowPlayingInfo: [String: Any] = [:]
    private var targetPage: String = ""
    private var isPlaying: Bool = false
    
    override public func load() {
        setupRemoteCommandCenter()
        setupAudioSession()
        
        // Проверяем launch options при загрузке
        checkLaunchOptions()
        
        // Подписываемся на уведомления о том, что приложение стало активным
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(applicationDidBecomeActive),
            name: UIApplication.didBecomeActiveNotification,
            object: nil
        )
    }
    
    @objc private func applicationDidBecomeActive() {
        checkLaunchOptions()
    }
    
    private func checkLaunchOptions() {
        guard let bridge = self.bridge else { return }
        
        if let launchOptions = bridge.viewController?.launchOptions,
           let notification = launchOptions[.remoteNotification] as? [String: Any],
           let openedFromNotification = notification["openedFromNotification"] as? Bool,
           openedFromNotification {
            
            let targetPage = notification["targetPage"] as? String ?? ""
            
            var ret = JSObject()
            ret["targetPage"] = targetPage
            
            notifyListeners("mediaSessionEvent", data: [
                "event": "openApp",
                "data": ret
            ])
        }
    }
    
    private func setupAudioSession() {
        do {
            let audioSession = AVAudioSession.sharedInstance()
            try audioSession.setCategory(.playback, mode: .default, options: [])
            try audioSession.setActive(true)
        } catch {
            print("Failed to setup audio session: \(error)")
        }
    }
    
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // Play
        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [weak self] event in
            self?.handlePlayCommand()
            return .success
        }
        
        // Pause
        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { [weak self] event in
            self?.handlePauseCommand()
            return .success
        }
        
        // Stop
        commandCenter.stopCommand.isEnabled = true
        commandCenter.stopCommand.addTarget { [weak self] event in
            self?.handleStopCommand()
            return .success
        }
        
        // Next
        commandCenter.nextTrackCommand.isEnabled = true
        commandCenter.nextTrackCommand.addTarget { [weak self] event in
            self?.sendEvent(event: "next", data: nil)
            return .success
        }
        
        // Previous
        commandCenter.previousTrackCommand.isEnabled = true
        commandCenter.previousTrackCommand.addTarget { [weak self] event in
            self?.sendEvent(event: "previous", data: nil)
            return .success
        }
        
        // Seek
        commandCenter.changePlaybackPositionCommand.isEnabled = true
        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            guard let event = event as? MPChangePlaybackPositionCommandEvent else {
                return .commandFailed
            }
            let position = Int64(event.positionTime * 1000)
            self?.sendEvent(event: "seekTo", data: position)
            return .success
        }
    }
    
    private func handlePlayCommand() {
        isPlaying = true
        updatePlaybackRate()
        
        let position = nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] as? Double ?? 0.0
        sendEvent(event: "play", data: Int64(position * 1000))
    }
    
    private func handlePauseCommand() {
        isPlaying = false
        updatePlaybackRate()
        
        let position = nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] as? Double ?? 0.0
        sendEvent(event: "pause", data: Int64(position * 1000))
    }
    
    private func handleStopCommand() {
        isPlaying = false
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = 0.0
        updatePlaybackRate()
        sendEvent(event: "stop", data: 0)
    }
    
    private func updatePlaybackRate() {
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
    
    private func sendEvent(event: String, data: Any?) {
        var ret = JSObject()
        ret["event"] = event
        
        if let position = data as? Int64 {
            ret["position"] = position
        } else if let jsData = data as? JSObject {
            ret["data"] = jsData
        }
        
        notifyListeners("mediaSessionEvent", data: ret)
    }
    
    @objc func `init`(_ call: CAPPluginCall) {
        let title = call.getString("title") ?? ""
        let artist = call.getString("artist") ?? ""
        let album = call.getString("album") ?? ""
        let cover = call.getString("cover") ?? ""
        let duration = call.getDouble("duration") ?? 0.0
        let position = call.getDouble("position") ?? 0.0
        isPlaying = call.getBool("isPlaying") ?? false
        targetPage = call.getString("targetPage") ?? ""
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = title
        nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = album
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration / 1000.0
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = position / 1000.0
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        
        // Загружаем обложку асинхронно
        if !cover.isEmpty {
            loadArtwork(from: cover) { [weak self] artwork in
                if let artwork = artwork {
                    self?.nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
                }
                MPNowPlayingInfoCenter.default().nowPlayingInfo = self?.nowPlayingInfo
            }
        } else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        }
        
        call.resolve()
    }
    
    @objc func play(_ call: CAPPluginCall) {
        handlePlayCommand()
        call.resolve()
    }
    
    @objc func pause(_ call: CAPPluginCall) {
        handlePauseCommand()
        call.resolve()
    }
    
    @objc func stop(_ call: CAPPluginCall) {
        handleStopCommand()
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
        call.resolve()
    }
    
    @objc func next(_ call: CAPPluginCall) {
        sendEvent(event: "next", data: nil)
        call.resolve()
    }
    
    @objc func previous(_ call: CAPPluginCall) {
        sendEvent(event: "previous", data: nil)
        call.resolve()
    }
    
    @objc func seekTo(_ call: CAPPluginCall) {
        let position = call.getDouble("position") ?? 0.0
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = position / 1000.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        sendEvent(event: "seekTo", data: Int64(position))
        call.resolve()
    }
    
    @objc func updateMetadata(_ call: CAPPluginCall) {
        let title = call.getString("title") ?? ""
        let artist = call.getString("artist") ?? ""
        let album = call.getString("album") ?? ""
        let cover = call.getString("cover") ?? ""
        let duration = call.getDouble("duration") ?? 0.0
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = title
        nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = album
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration / 1000.0
        
        if !cover.isEmpty {
            loadArtwork(from: cover) { [weak self] artwork in
                if let artwork = artwork {
                    self?.nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
                }
                MPNowPlayingInfoCenter.default().nowPlayingInfo = self?.nowPlayingInfo
            }
        } else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        }
        
        call.resolve()
    }
    
    @objc func updatePlaybackState(_ call: CAPPluginCall) {
        let state = call.getString("state") ?? "paused"
        let position = call.getDouble("position") ?? 0.0
        let playbackSpeed = call.getFloat("playbackSpeed") ?? 1.0
        
        isPlaying = (state == "playing")
        
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = position / 1000.0
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? Double(playbackSpeed) : 0.0
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        call.resolve()
    }
    
    private func loadArtwork(from urlString: String, completion: @escaping (MPMediaItemArtwork?) -> Void) {
        guard let url = URL(string: urlString) else {
            completion(nil)
            return
        }
        
        URLSession.shared.dataTask(with: url) { data, response, error in
            guard let data = data,
                  let image = UIImage(data: data) else {
                completion(nil)
                return
            }
            
            let artwork = MPMediaItemArtwork(boundsSize: image.size) { size in
                return image
            }
            
            completion(artwork)
        }.resume()
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
        
        let commandCenter = MPRemoteCommandCenter.shared()
        commandCenter.playCommand.removeTarget(nil)
        commandCenter.pauseCommand.removeTarget(nil)
        commandCenter.stopCommand.removeTarget(nil)
        commandCenter.nextTrackCommand.removeTarget(nil)
        commandCenter.previousTrackCommand.removeTarget(nil)
        commandCenter.changePlaybackPositionCommand.removeTarget(nil)
    }
}