package benloti.holoquiz.files;

import org.bukkit.Bukkit;

public class Logger {

    private static final String RESET = "\u001B[0m";
    private static final String WHITE = "\u001B[37m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String DARK_RED = "\u001B[91m";
    private static final String BLUE = "\u001B[34m";

    // Message formats
    private static final String INFO_FORMAT = WHITE + "[HoloQuiz] %s" + RESET;
    private static final String WARNING_FORMAT = YELLOW + "[HoloQuiz] Warning: %s" + RESET;
    private static final String ERROR_FORMAT = RED + "[HoloQuiz] ERROR: %s" + RESET;
    private static final String STACK_TRACE_FORMAT = RED + "[HoloQuiz] Stack Trace: %s" + RESET;
    private static final String DEV_ERROR_FORMAT = DARK_RED + "[HoloQuiz] DEV ERROR: %s" + RESET;
    private static final String DEBUG_FORMAT = BLUE + "[HoloQuiz] Debug Log: %s" + RESET;
    private static final String LOG_SET_LOGGING_LEVEL = WHITE + "Logging Level set to %d!" + RESET;

    /*
    Logging Level: 1 (Low)
    - Warnings and Errors
    - Important Startup Info
    Logging Level: 2 (Medium)
    - Correct Answers Only
    - Extra Startup Info
    Logging Level: 3 (High)
    - All Trivia Info
    Logging Level: 4 (Debug)
    - For me to find errors
    - Do I really need this? Probably not!
    */

    private static Logger instance;
    private int currentLevel;

    public static void createLogger() {
        instance = new Logger();
        instance.currentLevel = 1;
    }

    public static Logger getLogger() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void setLogLevel(int level) {
        this.currentLevel = level;
        Logger.getLogger().info_low(String.format(LOG_SET_LOGGING_LEVEL, level));
    }

    public void info_low(String msg) {
        if (currentLevel < 1) {
            return;
        }
        String message = String.format(INFO_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

    public void info_med(String msg) {
        if (currentLevel < 2) {
            return;
        }
        String message = String.format(INFO_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

    public void info_high(String msg) {
        if (currentLevel < 3) {
            return;
        }
        String message = String.format(INFO_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

    public void warn(String msg) {
        String message = String.format(WARNING_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

    public void error(String msg) {
        String message = String.format(ERROR_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

    public void devError(String msg) {
        String message = String.format(DEV_ERROR_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

    public void dumpStackTrace(Exception e) {
        String message = String.format(STACK_TRACE_FORMAT, e.toString());
        Bukkit.getLogger().info(message);
    }

    public void debug(String msg) {
        if (currentLevel < 4) {
            return;
        }
        String message = String.format(DEBUG_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

}
