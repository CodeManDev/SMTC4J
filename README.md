# SMTC4J
Simple Java Library for getting information about the current playback from the windows system media transport controls (SMTC).

## Requirements
- Java 21 or later
- Windows 10 or later
- GSON library for JSON parsing

## Usage
Load the native library
```java
SMTC4J.load();
```

## Start the automatic update scheduler

```java
long updateIntervalMillis = 500; // e.g., update every 500ms
SMTC4J.startUpdateScheduler(updateIntervalMillis);
```

## Access the cached information

Media info (title, artist, album, duration, cover, source app):
```java
MediaInfo mediaInfo = SMTC4J.getCachedMediaInfo();
System.out.println("Title: " + mediaInfo.getTitle());
```

Playback state (play/pause/stop code, position in seconds):

```java
PlaybackState playbackState = SMTC4J.getCachedPlaybackState();
System.out.println("State code: " + playbackState.getStateCode());
System.out.println("Position: " + playbackState.getPosition());
```

## Press media keys (PLAY, PAUSE, STOP, NEXT, PREVIOUS):

Using the scheduler:

```java
SMTC4J.scheduleKeyPress(MediaKey.PLAY);
```

Or directly:

```java
SMTC4J.pressKey(MediaKey.NEXT);
```


# Important Notes
This only works on Windows 10 and later versions, as it relies on the Windows SMTC API. <br>
The the jars in the releases are built for x64 architecture. <br>
If you you really need x86 support, you can build the native library from source using the provided C++ code. <br>
If you try to call the native methods on a thread on which COM is already initialized, it may lead to crashes. <br>
To avoid this, you should use the provided scheduler methods which handle threading and COM initialization internally.