// android/src/main/java/com/sangilov/plugins/mediasession/MediaSessionService.java

package com.sangilov.plugins.mediasession;
import com.getcapacitor.JSObject;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaSessionService extends Service {

    public static final String ACTION_INIT = "ACTION_INIT";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_SEEK_TO = "ACTION_SEEK_TO";
    public static final String ACTION_UPDATE_METADATA = "ACTION_UPDATE_METADATA";
    public static final String ACTION_UPDATE_PLAYBACK_STATE = "ACTION_UPDATE_PLAYBACK_STATE";

    // public static final String ACTION_OPEN_APP = "ACTION_OPEN_APP";
    public static final String ACTION_DISMISS = "ACTION_DISMISS";

    private static final String CHANNEL_ID = "media_session_channel";
    private static final int NOTIFICATION_ID = 1;
    private boolean isDismissing = false;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private NotificationManager notificationManager;
    private ExecutorService executor;

    private String title = "";
    private String artist = "";
    private String album = "";
    private String cover = "";
    private long duration = 0L;
    private long position = 0L;
    private boolean isPlaying = false;
    private boolean isInternalCall = false;
    private String targetPage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        
        executor = Executors.newSingleThreadExecutor();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        mediaSession = new MediaSessionCompat(this, "MediaSessionControl");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                              MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_STOP |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO
                );
        mediaSession.setPlaybackState(stateBuilder.build());

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                if (!isInternalCall) {
                    isPlaying = true;
                    updateState();
                    MediaSessionControlPlugin.sendEvent("play", position);
                    showNotification();
                    }
            }

            @Override
            public void onPause() {
                if (!isInternalCall && mediaSession != null && mediaSession.isActive()) {
                    isPlaying = false;
                    updateState();
                    MediaSessionControlPlugin.sendEvent("pause", position);
                    showNotification();
                } else if (!isInternalCall && mediaSession != null && !mediaSession.isActive()) {
                    // Если MediaSession неактивна, отправляем stop вместо pause
                    MediaSessionControlPlugin.sendEvent("stop", position);
                }
            }

            @Override
            public void onStop() {
                if (!isInternalCall) {
                    isPlaying = false;
                    position = 0L;
                    updateState();
                    // MediaSessionControlPlugin.sendEvent("stop", position);
                    // Не отправляем notificationDismissed здесь, это будет в ACTION_DISMISS
                }
}

            @Override
            public void onSkipToNext() {
                MediaSessionControlPlugin.sendEvent("next", null);
            }

            @Override
            public void onSkipToPrevious() {
                MediaSessionControlPlugin.sendEvent("previous", null);
            }

            @Override
            public void onSeekTo(long pos) {
                position = pos;
                MediaSessionControlPlugin.sendEvent("seekTo", pos);
                updateState();
            }
        });

        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_STICKY;

        switch (intent.getAction()) {
            case ACTION_INIT:
                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                album = intent.getStringExtra("album");
                cover = intent.getStringExtra("cover");
                duration = intent.getLongExtra("duration", 0L);
                position = intent.getLongExtra("position", 0L);
                isPlaying = intent.getBooleanExtra("isPlaying", false);
                targetPage = intent.getStringExtra("targetPage");
                updateMetadata();
                updateState();
                showNotification();
                break;
            case ACTION_PLAY: 
                isInternalCall = true;
                mediaSession.getController().getTransportControls().play();
                isInternalCall = false;
                break;
            case ACTION_PAUSE: 
                isInternalCall = true;
                mediaSession.getController().getTransportControls().pause();
                isInternalCall = false;
                break;
            case ACTION_STOP: 
                isInternalCall = true;
                stopSelf();
                // mediaSession.getController().getTransportControls().stop();
                isInternalCall = false;
                break;
            case ACTION_NEXT: 
                isInternalCall = true;
                mediaSession.getController().getTransportControls().skipToNext();
                isInternalCall = false;
                break;
            case ACTION_PREV: 
                isInternalCall = true;
                mediaSession.getController().getTransportControls().skipToPrevious();
                isInternalCall = false;
                break;
            case ACTION_SEEK_TO:
                isInternalCall = true;
                long pos = intent.getLongExtra("position", 0L);
                mediaSession.getController().getTransportControls().seekTo(pos);
                isInternalCall = false;
                break;
            case ACTION_UPDATE_METADATA:
                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                album = intent.getStringExtra("album");
                cover = intent.getStringExtra("cover");
                duration = intent.getLongExtra("duration", 0L);
                updateMetadata();
                showNotification();
                break;
            case ACTION_UPDATE_PLAYBACK_STATE:
                String state = intent.getStringExtra("state");
                position = intent.getLongExtra("position", 0L);
                float playbackSpeed = intent.getFloatExtra("playbackSpeed", 1.0f);
                if ("playing".equals(state)) {
                    isPlaying = true;
                } else if ("paused".equals(state)) {
                    isPlaying = false;
                } else if ("stopped".equals(state)) {
                    isPlaying = false;
                }
                updateState();
                showNotification();
                break;
            // case ACTION_OPEN_APP:

            //     JSObject eventData = new JSObject();
            //     eventData.put("targetPage", targetPage);
            //     MediaSessionControlPlugin.sendEvent("openApp", eventData);
            //     break;
            case ACTION_DISMISS:
                isDismissing = true;
                if (mediaSession != null) {
                    mediaSession.setActive(false);
                }
                MediaSessionControlPlugin.sendEvent("notificationDismissed", null);
                stopSelf();
                break;
        }

        return START_STICKY;
    }

    private void updateState() {
        if (stateBuilder != null && mediaSession != null) {
            int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            stateBuilder.setState(state, position, 1.0f);
            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    private void updateMetadata() {
        if (mediaSession == null) return;
        
        MediaMetadataCompat.Builder metaBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
                
        // Загружаем обложку асинхронно
        if (cover != null && !cover.isEmpty()) {
            executor.execute(() -> {
                Bitmap bitmap = getBitmapFromUrl(cover);
                if (bitmap != null) {
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                }
                mediaSession.setMetadata(metaBuilder.build());
            });
        } else {
            mediaSession.setMetadata(metaBuilder.build());
        }
    }

    private void showNotification() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent playIntent = PendingIntent.getService(this, 1,
                new Intent(this, MediaSessionService.class).setAction(ACTION_PLAY), flags);
        PendingIntent pauseIntent = PendingIntent.getService(this, 2,
                new Intent(this, MediaSessionService.class).setAction(ACTION_PAUSE), flags);
        PendingIntent prevIntent = PendingIntent.getService(this, 3,
                new Intent(this, MediaSessionService.class).setAction(ACTION_PREV), flags);
        PendingIntent nextIntent = PendingIntent.getService(this, 4,
                new Intent(this, MediaSessionService.class).setAction(ACTION_NEXT), flags);
        PendingIntent deleteIntent = PendingIntent.getService(this, 5,
            new Intent(this, MediaSessionService.class).setAction(ACTION_DISMISS), flags);

        // Создаем Intent для открытия MainActivity с флагом
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getPackageName());
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchIntent.putExtra("openedFromNotification", true);
            launchIntent.putExtra("targetPage", targetPage);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 6, launchIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(isPlaying)
                .setDeleteIntent(deleteIntent)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_media_previous, "Previous", prevIntent))
                .addAction(isPlaying ?
                        new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pauseIntent) :
                        new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", playIntent))
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_media_next, "Next", nextIntent))
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Media Playback", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Media playback controls");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        MediaSessionControlPlugin.sendEvent("appClosed", null);
        stopSelf();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    private Bitmap getBitmapFromUrl(String src) {
        if (src == null || src.isEmpty()) return null;
        
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.connect();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (Exception e) {
            return null;
        }
    }
}