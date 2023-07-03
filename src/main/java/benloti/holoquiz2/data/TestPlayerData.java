package benloti.holoquiz2.data;

import benloti.holoquiz2.HoloQuiz2;
import benloti.holoquiz2.files.AbstractFile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TestPlayerData extends AbstractFile {

    public TestPlayerData(HoloQuiz2 main) {
        super(main,"allplayerdata.yml");
    }

    public void newPlayer(Player player) {
        List<String> testList = new ArrayList<>();
        testList.add("FirstItem");
        testList.add("pekopeko");
        config.set(player.getUniqueId().toString(), testList);
    }

}
