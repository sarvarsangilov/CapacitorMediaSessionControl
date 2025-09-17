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
   * Инициализация MediaSession и уведомления
   */
  init(options: InitOptions): Promise<void>;

  /**
   * Запуск воспроизведения
   */
  play(): Promise<void>;

  /**
   * Пауза воспроизведения
   */
  pause(): Promise<void>;

  /**
   * Остановка воспроизведения
   */
  stop(): Promise<void>;

  /**
   * Следующий трек
   */
  next(): Promise<void>;

  /**
   * Предыдущий трек
   */
  previous(): Promise<void>;

  /**
   * Перемотка на указанную позицию
   */
  seekTo(options: SeekToOptions): Promise<void>;

  /**
   * Обновление метаданных трека
   */
  updateMetadata(options: UpdateMetadataOptions): Promise<void>;

  /**
   * Обновление состояния воспроизведения
   */
  updatePlaybackState(options: UpdatePlaybackStateOptions): Promise<void>;

  /**
   * Подписка на события из MediaSession
   */
  addListener(
    eventName: "mediaSessionEvent",
    listenerFunc: (event: MediaSessionEventPayload) => void
  ): Promise<{ remove: () => void }>;
}
