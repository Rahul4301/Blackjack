package Tests;

import org.junit.jupiter.api.*;
import Server.UserLogger;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserLoggerTest {

    private static final String TEST_LOG_FILE = "test_user_log.txt";
    private static Path testLogPath;

    @BeforeEach
    void setup() throws IOException {
        // Create a fresh test log file for each test
        testLogPath = Paths.get(TEST_LOG_FILE);
        Files.deleteIfExists(testLogPath);
    }

    @AfterEach
    void cleanup() throws IOException {
        // Clean up test log file after each test
        Files.deleteIfExists(testLogPath);
    }

    @Test
    @DisplayName("Should write log entry to file with correct format")
    void testLogWritesToFile() throws IOException {
        String username = "testUser";
        String action = "LOGIN";

        UserLogger.log(username, action);

        assertTrue(Files.exists(Paths.get("user_log.txt")), "Log file should exist");

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        assertFalse(lines.isEmpty(), "Log file should not be empty");

        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.contains(username), "Log should contain username");
        assertTrue(lastLine.contains(action), "Log should contain action");
        assertTrue(lastLine.matches(".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*"), "Log should contain timestamp");
    }

    @Test
    @DisplayName("Should append multiple log entries")
    void testMultipleLogEntries() throws IOException {
        UserLogger.log("user1", "LOGIN");
        UserLogger.log("user2", "LOGOUT");
        UserLogger.log("user3", "CREATE_TABLE");

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        long newEntries = lines.stream()
                .filter(line -> line.contains("user1") || line.contains("user2") || line.contains("user3"))
                .count();

        assertTrue(newEntries >= 3, "Should have at least 3 new log entries");
    }

    @Test
    @DisplayName("Should handle empty username")
    void testEmptyUsername() throws IOException {
        UserLogger.log("", "SOME_ACTION");

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        String lastLine = lines.get(lines.size() - 1);

        assertTrue(lastLine.contains("SOME_ACTION"), "Log should contain action even with empty username");
    }

    @Test
    @DisplayName("Should handle empty action")
    void testEmptyAction() throws IOException {
        UserLogger.log("testUser", "");

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        String lastLine = lines.get(lines.size() - 1);

        assertTrue(lastLine.contains("testUser"), "Log should contain username even with empty action");
    }

    @Test
    @DisplayName("Should handle special characters in username and action")
    void testSpecialCharacters() throws IOException {
        String username = "user@test.com";
        String action = "DEPOSIT_$100.50";

        UserLogger.log(username, action);

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        String lastLine = lines.get(lines.size() - 1);

        assertTrue(lastLine.contains(username), "Log should handle special characters in username");
        assertTrue(lastLine.contains(action), "Log should handle special characters in action");
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void testNullUsername() {
        assertDoesNotThrow(() -> UserLogger.log(null, "ACTION"),
                "Should not throw exception with null username");
    }

    @Test
    @DisplayName("Should handle null action gracefully")
    void testNullAction() {
        assertDoesNotThrow(() -> UserLogger.log("testUser", null),
                "Should not throw exception with null action");
    }

    @Test
    @DisplayName("Should create log file if it doesn't exist")
    void testLogFileCreation() throws IOException {
        Path logPath = Paths.get("user_log.txt");
        Files.deleteIfExists(logPath);

        UserLogger.log("newUser", "FIRST_LOG");

        assertTrue(Files.exists(logPath), "Log file should be created if it doesn't exist");
    }

    @Test
    @DisplayName("Should log various common actions")
    void testCommonActions() throws IOException {
        String[] actions = {"LOGIN", "LOGOUT", "CREATE_TABLE", "JOIN_TABLE", 
                          "LEAVE_TABLE", "PLACE_BET", "HIT", "STAND", "DEPOSIT", "WITHDRAW"};

        for (String action : actions) {
            UserLogger.log("testUser", action);
        }

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        long matchCount = lines.stream()
                .filter(line -> line.contains("testUser"))
                .count();

        assertTrue(matchCount >= actions.length, 
                "Should have logged all common actions");
    }

    @Test
    @DisplayName("Should handle concurrent logging")
    void testConcurrentLogging() throws InterruptedException, IOException {
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    UserLogger.log("user" + threadNum, "ACTION_" + j);
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        List<String> lines = Files.readAllLines(Paths.get("user_log.txt"));
        long newEntries = lines.stream()
                .filter(line -> line.contains("user") && line.contains("ACTION_"))
                .count();

        assertTrue(newEntries >= 100, 
                "Should have logged all 100 concurrent entries");
    }
}
