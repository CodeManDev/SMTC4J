# SMTC4J
Simple Java Library for getting information about the current playback from the windows system media transport controls (SMTC).

## Usage
Load the native library
```java
SMTC4J.load();
```

Start the automatic update thread

```java
long updateIntervalMillis = 500; // e.g., update every 500ms
SMTC4J.startUpdateThread(updateIntervalMillis);
```

Access the cached information

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