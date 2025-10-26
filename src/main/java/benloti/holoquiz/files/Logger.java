package benloti.holoquiz.files;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    private static final String INFO_FORMAT = ChatColor.WHITE + "[HoloQuiz] %s";
    private static final String WARNING_FORMAT = ChatColor.YELLOW + "[HoloQuiz] Warning: %s";
    private static final String ERROR_FORMAT = ChatColor.RED + "[HoloQuiz] ERROR: %s";
    private static final String STACK_TRACE_FORMAT =  ChatColor.RED + "[HoloQuiz] Stack Trace: %s";
    private static final String DEV_ERROR_FORMAT = ChatColor.DARK_RED + "[HoloQuiz] DEV ERROR: %s";
    private static final String DEBUG_FORMAT =  ChatColor.BLUE + "[HoloQuiz] Debug Log: %s"; //Used for me to Debug

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
        Logger.getLogger().info_low(String.format("Logging Level set to %d!" , level));
    }

    public void info_low(String msg) {
        if (currentLevel < 1) {
            Bukkit.getLogger().info("Skipped logging for " + msg + " Current lvl is: " + currentLevel);
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
        if (currentLevel <= 4) {
            return;
        }
        String message = String.format(DEBUG_FORMAT, msg);
        Bukkit.getLogger().info(message);
    }

}
