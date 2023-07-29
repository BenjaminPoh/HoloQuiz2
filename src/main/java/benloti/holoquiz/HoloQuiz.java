package benloti.holoquiz;

import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.commands.PlayerCmds;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.leaderboard.Leaderboard;
import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.QuizAnswerHandler;
import org.bukkit.Bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz extends JavaPlugin {

    private ConfigFile configFile;
    private DependencyHandler dependencyHandler;
    private DatabaseManager database;
    private GameManager gameManager;
    private Leaderboard leaderboard;
    private UserInterface userInterface;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("[HoloQuiz] Starting Up HoloQuiz!");
        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.configFile = new ConfigFile(this);
        this.dependencyHandler = new DependencyHandler(this);
        this.database = new DatabaseManager(this);
        this.userInterface = new UserInterface(dependencyHandler.getCMIDep(), database.getUserPersonalisation());
        this.leaderboard = new Leaderboard(configFile, database);
        this.gameManager = new GameManager(this, configFile, userInterface, dependencyHandler);
        new QuizAnswerHandler(this, gameManager, database, leaderboard, userInterface, configFile);
        getCommand("HoloQuiz").setExecutor(new PlayerCmds(gameManager, database, leaderboard, configFile, userInterface));
        if(configFile.isEnableOnStart()) {
            gameManager.startGame();
        }
    }

    @Override
    public void onDisable() {
        database.getUserPersonalisation().savePlayerSettings();
        Bukkit.getLogger().info("[HoloQuiz] Shutting Down HoloQuiz!");
    }
}

// Let us pray
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//
