# capacitor-media-session-control

Capacitor plugin for controlling media playback via Android MediaSession API.
Provides advanced integration with lock screen controls, notification controls, and hardware media buttons.

## ✨ Features

* Play, pause, seek, stop, and skip tracks
* Show playback information (title, artist, album, cover, duration)
* Sync playback position with UI
* Integration with Android notifications (media style)
* Reacts to hardware media buttons (headphones, Bluetooth, etc.)

## Demo
* [Link to demo ](https://github.com/sarvarsangilov/mucisplayer)

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

Init Media Session API and notification

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#initoptions">InitOptions</a></code> |

--------------------


### play()

```typescript
play() => Promise<void>
```

Start playback

--------------------


### pause()

```typescript
pause() => Promise<void>
```

Pause playback

--------------------


### stop()

```typescript
stop() => Promise<void>
```

Stop playback and remove notification

--------------------


### next()

```typescript
next() => Promise<void>
```

Next track

--------------------


### previous()

```typescript
previous() => Promise<void>
```

Previous track

--------------------


### seekTo(...)

```typescript
seekTo(options: SeekToOptions) => Promise<void>
```

Seek to position

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#seektooptions">SeekToOptions</a></code> |

--------------------


### updateMetadata(...)

```typescript
updateMetadata(options: UpdateMetadataOptions) => Promise<void>
```

Update metadata (title, artist, album, cover, duration)

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#updatemetadataoptions">UpdateMetadataOptions</a></code> |

--------------------


### updatePlaybackState(...)

```typescript
updatePlaybackState(options: UpdatePlaybackStateOptions) => Promise<void>
```

Update playback state (state, position, playbackSpeed)

| Param         | Type                                                                              |
| ------------- | --------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#updateplaybackstateoptions">UpdatePlaybackStateOptions</a></code> |

--------------------


### addListener('mediaSessionEvent', ...)

```typescript
addListener(eventName: "mediaSessionEvent", listenerFunc: (event: MediaSessionEventPayload) => void) => Promise<{ remove: () => void; }>
```

Add listener for media session events

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
