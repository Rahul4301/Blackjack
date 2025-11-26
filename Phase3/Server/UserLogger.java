package Server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class UserLogger {
    private static final String LOG_FILE = "user_log.txt";

    public static void log(String username, String action) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(LocalDateTime.now() + "," + username + "," + action);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
