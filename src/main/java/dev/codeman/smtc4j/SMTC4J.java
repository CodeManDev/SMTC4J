package dev.codeman.smtc4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SMTC4J {

    private static final Gson GSON = new GsonBuilder().create();

    private static boolean loaded = false;

    private static ScheduledExecutorService scheduler = null;

    private static MediaInfo lastMediaInfo = null;
    private static PlaybackState lastPlaybackState = null;

    private static native String getPlaybackState();
    private static native String getMediaInfo();
    private static native void pressMediaKey(int keyCode);

    public static boolean load() {
        if (loaded) return true;

        if (!System.getProperty("os.name").toLowerCase().contains("win"))
            throw new UnsupportedOperationException("SMTC4J is Windows-only");

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

    private static synchronized ScheduledExecutorService getScheduler() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "SMTC4J-Executor");
                t.setDaemon(true);
                return t;
            });
        }
        return scheduler;
    }

    public static MediaInfo getCachedMediaInfo() {
        return lastMediaInfo;
    }

    public static PlaybackState getCachedPlaybackState() {
        return lastPlaybackState;
    }

    public static void updateCache() {
        checkIsLoaded();

        lastMediaInfo = parsedMediaInfo();
        lastPlaybackState = parsedPlaybackState();
    }

    public static synchronized void startUpdateScheduler(long intervalMillis) {
        checkIsLoaded();

        ScheduledExecutorService exec = getScheduler();
        exec.scheduleAtFixedRate(() -> {
            try {
                updateCache();
            } catch (Exception e) {
                System.err.println("Error updating SMTC4J cache: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);
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

    public static void scheduleKeyPress(MediaKey key) {
        checkIsLoaded();

        getScheduler().execute(() -> {
            try {
                pressKey(key);
            } catch (Exception e) {
                System.err.println("Error pressing media key: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        });
    }

    private static void checkIsLoaded() {
        if (!isLoaded())
            throw new IllegalStateException("SMTC4J native library is not loaded.");
    }
}