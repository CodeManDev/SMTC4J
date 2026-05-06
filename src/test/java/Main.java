import dev.codeman.smtc4j.MediaKey;
import dev.codeman.smtc4j.PlaybackState;
import dev.codeman.smtc4j.SMTC4J;

import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, World!");

        if (SMTC4J.load()) {
            System.out.println("SMTC4J loaded successfully.");
        } else {
            System.out.println("Failed to load SMTC4J.");
            return;
        }

        SMTC4J.startUpdateThread(1000);

        int i = 0;

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("State: " + SMTC4J.getCachedPlaybackState());
            System.out.println("State Code: " + SMTC4J.getCachedPlaybackState().getStateCode().name());
            System.out.println("Info: " + SMTC4J.getCachedMediaInfo());


            if (i % 5 == 0) {
                MediaKey keyToPress = MediaKey.values()[ThreadLocalRandom.current().nextInt(0, MediaKey.values().length)];
                SMTC4J.pressKey(keyToPress);
                System.out.println("Pressed key: " + keyToPress.name());
            }

            i++;
        }
    }

}
