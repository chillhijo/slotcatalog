package util;

import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppLogger {
    private static final Logger logger = Logger.getLogger(AppLogger.class.getName());

    static {
        logger.setLevel(Level.ALL);

        Handler consoleHandler = new java.util.logging.ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        try {
            Handler fileHandler = new FileHandler("src/resources/logs/app.log");
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing file handler", e);
        }
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message, NoSuchElementException e) {
        logger.warning(message);
    }

    public static void severe(String message, Exception e) {
        logger.severe(message);
    }
}
