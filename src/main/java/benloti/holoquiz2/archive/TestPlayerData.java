package benloti.holoquiz2.archive;

import benloti.holoquiz2.HoloQuiz2;
import org.bukkit.entity.Player;

public class TestPlayerData extends AbstractFile {

    public TestPlayerData(HoloQuiz2 main) {
        super(main,"allplayerdata.yml");
    }

    public void newPlayerData(Player player) {
        PlayerData playerData = new PlayerData();
        config.set(player.getUniqueId().toString(), playerData);
    }

    public void updatePlayerData(Player player, PlayerData data) {
        config.set(player.getUniqueId().toString(), data);
    }

}
