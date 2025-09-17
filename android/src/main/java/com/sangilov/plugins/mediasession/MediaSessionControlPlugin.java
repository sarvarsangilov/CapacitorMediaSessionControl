// android/src/main/java/com/sangilov/plugins/mediasession/MediaSessionControlPlugin.java

package com.sangilov.plugins.mediasession;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "MediaSessionControl")
public class MediaSessionControlPlugin extends Plugin {

    private static MediaSessionControlPlugin instance;

    @Override
    public void load() {
        super.load();
        instance = this;

        checkIntentExtras();
    }
    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);
        // Проверяем Intent при получении нового Intent
        checkIntentFromIntent(intent);
    }


    private void checkIntentFromIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("openedFromNotification", false)) {
            String targetPage = intent.getStringExtra("targetPage");
            
            JSObject ret = new JSObject();
            ret.put("targetPage", targetPage);
            
            sendEvent("openApp", ret);
            
            // Очищаем флаги
            intent.removeExtra("openedFromNotification");
            intent.removeExtra("targetPage");
        }
    }

    private void checkIntentExtras() {
        try {
            android.app.Activity activity = getActivity();
            if (activity != null) {
                checkIntentFromIntent(activity.getIntent());
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    public static void sendEvent(String event, Object data) {
        if (instance == null) return;
        JSObject ret = new JSObject();
        ret.put("event", event);
        if (data instanceof Long) {
            ret.put("position", (Long) data);
        } else if (data instanceof JSObject) {
            ret.put("data", (JSObject) data);
        }
        instance.notifyListeners("mediaSessionEvent", ret);
    }

    @PluginMethod
    public void init(PluginCall call) {
        String title = call.getString("title", "");
        String artist = call.getString("artist", "");
        String album = call.getString("album", "");
        String cover = call.getString("cover", "");
        Long duration = Math.round(call.getDouble("duration", 0.0));
        Long position = Math.round(call.getDouble("position", 0.0));
        Boolean isPlaying = call.getBoolean("isPlaying", false);
        String targetPage = call.getString("targetPage", "");
    
        Context ctx = getContext();
        Intent intent = new Intent(ctx, MediaSessionService.class);
        intent.setAction(MediaSessionService.ACTION_INIT);
        intent.putExtra("title", title);
        intent.putExtra("artist", artist);
        intent.putExtra("album", album);
        intent.putExtra("cover", cover);
        intent.putExtra("duration", duration != null ? duration : 0L);
        intent.putExtra("position", position != null ? position : 0L);
        intent.putExtra("isPlaying", isPlaying != null ? isPlaying : false);
        intent.putExtra("targetPage", targetPage);
        
        try {
            ctx.startForegroundService(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to start service: " + e.getMessage());
        }
    }

    @PluginMethod
    public void play(PluginCall call) {
        try {
            sendSimpleAction(MediaSessionService.ACTION_PLAY);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to send play action: " + e.getMessage());
        }
    }

    @PluginMethod
    public void pause(PluginCall call) {
        try {
            sendSimpleAction(MediaSessionService.ACTION_PAUSE);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to send pause action: " + e.getMessage());
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        try {
            sendSimpleAction(MediaSessionService.ACTION_STOP);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to send stop action: " + e.getMessage());
        }
    }

    @PluginMethod
    public void next(PluginCall call) {
        try {
            sendSimpleAction(MediaSessionService.ACTION_NEXT);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to send next action: " + e.getMessage());
        }
    }

    @PluginMethod
    public void previous(PluginCall call) {
        try {
            sendSimpleAction(MediaSessionService.ACTION_PREV);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to send previous action: " + e.getMessage());
        }
    }

    @PluginMethod
    public void seekTo(PluginCall call) {
        Long position = Math.round(call.getDouble("position", 0.0));
        
        try {
            Intent intent = new Intent(getContext(), MediaSessionService.class);
            intent.setAction(MediaSessionService.ACTION_SEEK_TO);
            intent.putExtra("position", position != null ? position : 0L);
            getContext().startForegroundService(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to send seekTo action: " + e.getMessage());
        }
    }

    @PluginMethod
    public void updateMetadata(PluginCall call) {
        String title = call.getString("title", "");
        String artist = call.getString("artist", "");
        String album = call.getString("album", "");
        String cover = call.getString("cover", "");
        Long duration = Math.round(call.getDouble("duration", 0.0));

        try {
            Intent intent = new Intent(getContext(), MediaSessionService.class);
            intent.setAction(MediaSessionService.ACTION_UPDATE_METADATA);
            intent.putExtra("title", title);
            intent.putExtra("artist", artist);
            intent.putExtra("album", album);
            intent.putExtra("cover", cover);
            intent.putExtra("duration", duration != null ? duration : 0L);
            getContext().startForegroundService(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to update metadata: " + e.getMessage());
        }
    }

    @PluginMethod
    public void updatePlaybackState(PluginCall call) {
        String state = call.getString("state", "paused");
        Long position = Math.round(call.getDouble("position", 0.0));
        Float playbackSpeed = call.getFloat("playbackSpeed", 1.0f);

        try {
            Intent intent = new Intent(getContext(), MediaSessionService.class);
            intent.setAction(MediaSessionService.ACTION_UPDATE_PLAYBACK_STATE);
            intent.putExtra("state", state);
            intent.putExtra("position", position != null ? position : 0L);
            intent.putExtra("playbackSpeed", playbackSpeed != null ? playbackSpeed : 1.0f);
            getContext().startForegroundService(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to update playback state: " + e.getMessage());
        }
    }

    private void sendSimpleAction(String action) {
        Intent intent = new Intent(getContext(), MediaSessionService.class);
        intent.setAction(action);
        getContext().startForegroundService(intent);
    }

    @Override
    protected void handleOnDestroy() {
        instance = null;
        super.handleOnDestroy();
    }
}