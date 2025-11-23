import { WebPlugin } from '@capacitor/core';

import type { MediaSessionControlPlugin } from './definitions';

export class MediaSessionControlWeb extends WebPlugin implements MediaSessionControlPlugin {
  async initMediaSession(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async play(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async pause(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async stop(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async next(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async previous(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async seekTo(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async updateMetadata(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }

  async updatePlaybackState(): Promise<void> {
    console.log('MediaSessionControl not supported on web');
  }
}
