# capacitor-media-session-control

Capacitor plugin for controlling media playback via Android MediaSession API.
Provides advanced integration with lock screen controls, notification controls, and hardware media buttons.

## ✨ Features

* Play, pause, seek, stop, and skip tracks
* Show playback information (title, artist, album, cover, duration)
* Sync playback position with UI
* Integration with Android notifications (media style)
* Reacts to hardware media buttons (headphones, Bluetooth, etc.)

## Install

```bash
npm install capacitor-media-session-control
npx cap sync
```

## API

<docgen-index>

* [`init(...)`](#init)
* [`play()`](#play)
* [`pause()`](#pause)
* [`stop()`](#stop)
* [`next()`](#next)
* [`previous()`](#previous)
* [`seekTo(...)`](#seekto)
* [`updateMetadata(...)`](#updatemetadata)
* [`updatePlaybackState(...)`](#updateplaybackstate)
* [`addListener('mediaSessionEvent', ...)`](#addlistenermediasessionevent-)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### init(...)

```typescript
init(options: InitOptions) => Promise<void>
```

Инициализация MediaSession и уведомления

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#initoptions">InitOptions</a></code> |

--------------------


### play()

```typescript
play() => Promise<void>
```

Запуск воспроизведения

--------------------


### pause()

```typescript
pause() => Promise<void>
```

Пауза воспроизведения

--------------------


### stop()

```typescript
stop() => Promise<void>
```

Остановка воспроизведения

--------------------


### next()

```typescript
next() => Promise<void>
```

Следующий трек

--------------------


### previous()

```typescript
previous() => Promise<void>
```

Предыдущий трек

--------------------


### seekTo(...)

```typescript
seekTo(options: SeekToOptions) => Promise<void>
```

Перемотка на указанную позицию

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#seektooptions">SeekToOptions</a></code> |

--------------------


### updateMetadata(...)

```typescript
updateMetadata(options: UpdateMetadataOptions) => Promise<void>
```

Обновление метаданных трека

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#updatemetadataoptions">UpdateMetadataOptions</a></code> |

--------------------


### updatePlaybackState(...)

```typescript
updatePlaybackState(options: UpdatePlaybackStateOptions) => Promise<void>
```

Обновление состояния воспроизведения

| Param         | Type                                                                              |
| ------------- | --------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#updateplaybackstateoptions">UpdatePlaybackStateOptions</a></code> |

--------------------


### addListener('mediaSessionEvent', ...)

```typescript
addListener(eventName: "mediaSessionEvent", listenerFunc: (event: MediaSessionEventPayload) => void) => Promise<{ remove: () => void; }>
```

Подписка на события из MediaSession

| Param              | Type                                                                                              |
| ------------------ | ------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'mediaSessionEvent'</code>                                                                  |
| **`listenerFunc`** | <code>(event: <a href="#mediasessioneventpayload">MediaSessionEventPayload</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;{ remove: () =&gt; void; }&gt;</code>

--------------------


### Interfaces


#### InitOptions

| Prop             | Type                 |
| ---------------- | -------------------- |
| **`title`**      | <code>string</code>  |
| **`artist`**     | <code>string</code>  |
| **`album`**      | <code>string</code>  |
| **`cover`**      | <code>string</code>  |
| **`duration`**   | <code>number</code>  |
| **`position`**   | <code>number</code>  |
| **`isPlaying`**  | <code>boolean</code> |
| **`targetPage`** | <code>string</code>  |


#### SeekToOptions

| Prop           | Type                |
| -------------- | ------------------- |
| **`position`** | <code>number</code> |


#### UpdateMetadataOptions

| Prop           | Type                |
| -------------- | ------------------- |
| **`title`**    | <code>string</code> |
| **`artist`**   | <code>string</code> |
| **`album`**    | <code>string</code> |
| **`cover`**    | <code>string</code> |
| **`duration`** | <code>number</code> |


#### UpdatePlaybackStateOptions

| Prop                | Type                                            |
| ------------------- | ----------------------------------------------- |
| **`state`**         | <code>'playing' \| 'paused' \| 'stopped'</code> |
| **`position`**      | <code>number</code>                             |
| **`playbackSpeed`** | <code>number</code>                             |


#### MediaSessionEventPayload

| Prop           | Type                                                            |
| -------------- | --------------------------------------------------------------- |
| **`event`**    | <code><a href="#mediasessionevent">MediaSessionEvent</a></code> |
| **`position`** | <code>number</code>                                             |
| **`data`**     | <code>{ targetPage?: string; }</code>                           |


### Type Aliases


#### MediaSessionEvent

<code>"play" | "pause" | "stop" | "next" | "previous" | "seekTo" | "openApp" | "notificationDismissed" | "appClosed"</code>

</docgen-api>
