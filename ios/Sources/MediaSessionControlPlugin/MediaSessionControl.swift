import Foundation
import Capacitor
import MediaPlayer
import AVFoundation
import UIKit

@objc(MediaSessionControl)
public class MediaSessionControl: NSObject {
    
    private var nowPlayingInfo: [String: Any] = [:]
    private var playerRate: Float = 1.0
    private var playerPosition: TimeInterval = 0
    
    @objc public func initMedia(_ call: CAPPluginCall) {
        let title = call.getString("title") ?? ""
        let artist = call.getString("artist") ?? ""
        let album = call.getString("album") ?? ""
        let coverUrl = call.getString("cover") ?? ""
        let duration = call.getDouble("duration") ?? 0.0
        let position = call.getDouble("position") ?? 0.0
        let isPlaying = call.getBool("isPlaying") ?? false
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = title
        nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = album
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = position
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        
        // Загружаем обложку асинхронно
        if let url = URL(string: coverUrl) {
            URLSession.shared.dataTask(with: url) { data, _, _ in
                if let data = data, let image = UIImage(data: data) {
                    let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
                    self.nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
                    DispatchQueue.main.async {
                        MPNowPlayingInfoCenter.default().nowPlayingInfo = self.nowPlayingInfo
                    }
                }
            }.resume()
        } else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        }
        
        setupRemoteCommands()
        setupAudioSession()
        call.resolve()
    }
    
    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Audio session setup failed: \(error)")
        }
    }
    
    private func setupRemoteCommands() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        commandCenter.playCommand.addTarget { [weak self] event in
            self?.updatePlaybackRate(1.0)
            self?.sendEvent("play")
            return .success
        }
        commandCenter.pauseCommand.addTarget { [weak self] event in
            self?.updatePlaybackRate(0.0)
            self?.sendEvent("pause")
            return .success
        }
        commandCenter.nextTrackCommand.addTarget { [weak self] event in
            self?.sendEvent("next")
            return .success
        }
        commandCenter.previousTrackCommand.addTarget { [weak self] event in
            self?.sendEvent("previous")
            return .success
        }
        commandCenter.stopCommand.addTarget { [weak self] event in
            self?.updatePlaybackRate(0.0)
            self?.sendEvent("stop")
            return .success
        }
        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            if let e = event as? MPChangePlaybackPositionCommandEvent {
                self?.sendEvent("seekTo", data: ["position": e.positionTime])
            }
            return .success
        }
    }
    
    // КЛЮЧЕВОЙ МЕТОД — был пропущен!
    @objc public func updatePlaybackState(_ call: CAPPluginCall) {
        let positionMs = call.getDouble("position") ?? 0.0
        let isPlaying = call.getBool("isPlaying") ?? false
        
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = positionMs / 1000.0
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        call.resolve()
    }
    
    private func updatePlaybackRate(_ rate: Float) {
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = rate
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
    
    private func sendEvent(_ event: String, data: [String: Any]? = nil) {
        var payload: [String: Any] = ["event": event]
        if let d = data { payload["data"] = d }
        NotificationCenter.default.post(name: Notification.Name("mediaSessionEvent"), object: payload)
    }
    
    @objc public func play() {
        updatePlaybackRate(1.0)
        sendEvent("play")
    }
    
    @objc public func pause() {
        updatePlaybackRate(0.0)
        sendEvent("pause")
    }
    
    @objc public func stop() {
        updatePlaybackRate(0.0)
        sendEvent("stop")
    }
    
    @objc public func next() {
        sendEvent("next")
    }
    
    @objc public func previous() {
        sendEvent("previous")
    }
    
    @objc public func seekTo(_ call: CAPPluginCall) {
        let position = call.getDouble("position") ?? 0.0
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = position
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        sendEvent("seekTo", data: ["position": position])
        call.resolve()
    }
    
    @objc public func updateMetadata(_ call: CAPPluginCall) {
        let title = call.getString("title") ?? ""
        let artist = call.getString("artist") ?? ""
        let album = call.getString("album") ?? ""
        let coverUrl = call.getString("cover") ?? ""
        let duration = call.getDouble("duration") ?? 0.0
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = title
        nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = album
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration
        
        if let url = URL(string: coverUrl) {
            URLSession.shared.dataTask(with: url) { data, _, _ in
                if let data = data, let image = UIImage(data: data) {
                    let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
                    self.nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
                    DispatchQueue.main.async {
                        MPNowPlayingInfoCenter.default().nowPlayingInfo = self.nowPlayingInfo
                    }
                }
            }.resume()
        } else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        }
        call.resolve()
    }
}
