package dev.codeman.smtc4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class SMTC4J {

    private static final Gson GSON = new GsonBuilder().create();

    private static boolean loaded = false;
    private static Thread updateThread = null;

    private static MediaInfo lastMediaInfo = null;
    private static PlaybackState lastPlaybackState = null;

    private static native String getPlaybackState();
    private static native String getMediaInfo();
    private static native void pressMediaKey(int keyCode);

    public static boolean load() {
        if (loaded) return true;

        String dllPath = "/native/SMTC4J.dll";
        try (InputStream in = SMTC4J.class.getResourceAsStream(dllPath)) {
            if (in == null) {
                throw new FileNotFoundException("DLL not found: " + dllPath);
            }

            File tempDll = File.createTempFile("SMTC4J", ".dll");
            tempDll.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempDll)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            System.load(tempDll.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DLL", e);
        }

        return loaded = true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static MediaInfo getCachedMediaInfo() {
        return lastMediaInfo;
    }

    public static PlaybackState getCachedPlaybackState() {
        return lastPlaybackState;
    }

    public static void updateCache() {
        lastMediaInfo = parsedMediaInfo();
        lastPlaybackState = parsedPlaybackState();
    }

    public static void startUpdateThread(long intervalMillis) {
        if (updateThread != null && updateThread.isAlive())
            return;

        updateThread = new Thread(() -> {
            while (true) {
                try {
                    updateCache();
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "SMTC4J-Update-Thread");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public static MediaInfo parsedMediaInfo() {
        checkIsLoaded();

        String info = getMediaInfo();

        if (info.contains("\"error\":")) {
            System.out.println("Error retrieving media info: " + info);
            return new MediaInfo("", "", "", 0, "", "");
        }

        MediaInfo parsed = GSON.fromJson(info, MediaInfo.class);
        if (parsed == null)
            return new MediaInfo("", "", "", 0, "", "");

        return parsed;
    }

    public static PlaybackState parsedPlaybackState() {
        checkIsLoaded();

        String state = getPlaybackState();

        if (state.contains("\"error\":")) {
            System.out.println("Error retrieving playback state: " + state);
            return new PlaybackState(-1, 0);
        }

        PlaybackState parsed = GSON.fromJson(state, PlaybackState.class);
        if (parsed == null)
            return new PlaybackState(-1, 0);

        return parsed;
    }

    public static void pressKey(MediaKey key) {
        checkIsLoaded();

        pressMediaKey(key.ordinal());
    }

    private static void checkIsLoaded() {
        if (!isLoaded())
            throw new IllegalStateException("SMTC4J native library is not loaded.");
    }
}