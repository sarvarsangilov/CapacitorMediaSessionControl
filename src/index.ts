import { registerPlugin } from '@capacitor/core';

import type { MediaSessionControlPlugin } from './definitions';

const MediaSessionControl = registerPlugin<MediaSessionControlPlugin>('MediaSessionControl', {
  web: () => import('./web').then((m) => new m.MediaSessionControlWeb()),
});

export * from './definitions';
export { MediaSessionControl };
