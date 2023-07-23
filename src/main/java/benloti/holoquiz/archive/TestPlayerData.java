package benloti.holoquiz.archive;

import benloti.holoquiz.HoloQuiz;
import org.bukkit.entity.Player;

public class TestPlayerData extends AbstractFile {

    public TestPlayerData(HoloQuiz main) {
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
