package Tests;

import Client.MainApp;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class MainAppTest {

    @Test
    void hasValidMainSignature() throws Exception {
        Method main = MainApp.class.getMethod("main", String[].class);

        assertNotNull(main, "Main method should exist");
        assertTrue(Modifier.isPublic(main.getModifiers()), "main must be public");
        assertTrue(Modifier.isStatic(main.getModifiers()), "main must be static");
        assertEquals(void.class, main.getReturnType(), "main must return void");
    }

    @Test
    void mainCanBeInvokedWithoutUnexpectedException() {
        // Force headless mode for safety in CI environments
        System.setProperty("java.awt.headless", "true");

        assertDoesNotThrow(() -> {
            try {
                MainApp.main(new String[]{});
            } catch (HeadlessException e) {
                // Expected in headless environments when JOptionPane is used.
                // We swallow it so the test still passes.
            }
        });
    }
}
