package benloti.holoquiz;

import benloti.holoquiz.commands.CmdAutoComplete;
import benloti.holoquiz.dependencies.DependencyHandler;
import benloti.holoquiz.commands.PlayerCmds;
import benloti.holoquiz.files.ConfigFile;
import benloti.holoquiz.files.ContestManager;
import benloti.holoquiz.files.ExternalFiles;
import benloti.holoquiz.files.UserInterface;
import benloti.holoquiz.games.GameManager;
import benloti.holoquiz.database.DatabaseManager;
import benloti.holoquiz.games.QuizAnswerHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class HoloQuiz extends JavaPlugin {

    private ExternalFiles externalFiles;
    private ConfigFile configFile;
    private DependencyHandler dependencyHandler;
    private DatabaseManager database;
    private GameManager gameManager;
    private UserInterface userInterface;
    private ContestManager contestManager;
    private QuizAnswerHandler quizAnswerHandler;
    private PlayerCmds playerCmds;
    private CmdAutoComplete cmdAutoComplete;

    @Override
    public void onEnable() {
        this.externalFiles = new ExternalFiles(this);
        this.configFile = externalFiles.getConfigFile();
        this.dependencyHandler = new DependencyHandler(this);
        this.database = new DatabaseManager(this);
        this.userInterface = new UserInterface(dependencyHandler.getCMIDep(), database.getUserPersonalisation(), configFile.getPluginPrefix());
        this.gameManager = new GameManager(this, configFile, userInterface, dependencyHandler, externalFiles, database);
        this.contestManager = new ContestManager(database, configFile, externalFiles, gameManager);
        this.quizAnswerHandler = new QuizAnswerHandler(this, gameManager, database, userInterface, configFile, contestManager);
        this.playerCmds = new PlayerCmds(gameManager, database, externalFiles, userInterface, this);
        this.cmdAutoComplete = new CmdAutoComplete(externalFiles, this);
        getCommand("HoloQuiz").setExecutor(playerCmds);
        getCommand("HoloQuiz").setTabCompleter(cmdAutoComplete);
        if(configFile.isEnableOnStart()) {
            gameManager.startGame();
        }
    }

    @Override
    public void onDisable() {
        database.getUserPersonalisation().savePlayerSettings();
        //Bukkit.getLogger().info("[HoloQuiz] Shutting Down HoloQuiz!");
    }

    public boolean reloadHoloQuiz() {
        if(!externalFiles.reloadAll()) {
            return false;
        }
        this.configFile = externalFiles.getConfigFile();
        this.dependencyHandler = new DependencyHandler(this);
        this.userInterface = new UserInterface(dependencyHandler.getCMIDep(), database.getUserPersonalisation(), configFile.getPluginPrefix());
        this.gameManager = new GameManager(this, configFile, userInterface, dependencyHandler, externalFiles, database);
        //this.contestManager = new ContestManager(database, configFile, externalFiles, gameManager);
        quizAnswerHandler.reload(gameManager, userInterface, configFile);
        playerCmds.reload(gameManager, externalFiles, userInterface);
        cmdAutoComplete.reload(externalFiles);
        if(configFile.isEnableOnStart()) {
            gameManager.startGame();
        }
        return true;
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
