declare module "@capacitor/core" {
  interface PluginRegistry {
    MediaSessionControl: MediaSessionControlPlugin;
  }
}

export interface InitOptions {
  title?: string;
  artist?: string;
  album?: string;
  cover?: string;       // URL обложки
  duration?: number;    // Длительность трека (мс)
  position?: number;    // Начальная позиция (мс)
  isPlaying?: boolean;  // Состояние воспроизведения
  targetPage?: string;  // Страница для открытия при клике на уведомление
}

export interface UpdatePlaybackStateOptions {
  state: "playing" | "paused" | "stopped";
  position?: number;
  playbackSpeed?: number;
}

export interface UpdateMetadataOptions {
  title?: string;
  artist?: string;
  album?: string;
  cover?: string;
  duration?: number;
}

export interface SeekToOptions {
  position: number; // Новая позиция в мс
}

export type MediaSessionEvent =
  | "play"
  | "pause"
  | "stop"
  | "next"
  | "previous"
  | "seekTo"
  | "openApp"
  | "notificationDismissed"
  | "appClosed";

export interface MediaSessionEventPayload {
  event: MediaSessionEvent;
  position?: number;
  data?: {
    targetPage?: string;
  };
}

export interface MediaSessionControlPlugin {
  /**
   * Init Media Session API and notification
   */
  initMediaSession(options: InitOptions): Promise<void>;

  /**
   * Start playback
   */
  play(): Promise<void>;

  /**
   * Pause playback
   */
  pause(): Promise<void>;

  /**
   * Stop playback and remove notification
   */
  stop(): Promise<void>;

  /**
   * Next track
   */
  next(): Promise<void>;

  /**
   * Previous track
   */
  previous(): Promise<void>;

  /**
   * Seek to position
   */
  seekTo(options: SeekToOptions): Promise<void>;

  /**
   * Update metadata (title, artist, album, cover, duration)
   */
  updateMetadata(options: UpdateMetadataOptions): Promise<void>;

  /**
   * Update playback state (state, position, playbackSpeed)
   */
  updatePlaybackState(options: UpdatePlaybackStateOptions): Promise<void>;

  /**
   * Add listener for media session events
   */
  addListener(
    eventName: "mediaSessionEvent",
    listenerFunc: (event: MediaSessionEventPayload) => void
  ): Promise<{ remove: () => void }>;
}
