package benloti.holoquiz.dependencies;

import benloti.holoquiz.files.Logger;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIVanish;
import com.Zrips.CMI.Modules.Vanish.VanishAction;
import net.Zrips.CMILib.Colors.CMIChatColor;
import org.bukkit.entity.Player;

public class CMIDep {
    private boolean isEnabled = false;
    public CMIDep() {}

    public boolean isEnabled() {
        return isEnabled;
    }
    public void setEnabled() {
        isEnabled = true;
    }

    public String translateHexColors(String string) {
        return CMIChatColor.translate(string);
    }

    public boolean isPlayerVanished(Player player) {
        CMIVanish temp = CMI.getInstance().getVanishManager().getVanish(player.getUniqueId());
        if(temp == null) {
            Logger.getLogger().warn(String.format("Player %s is null in VanishManager.",player.getName()));
            return false;
        }
        return temp.is(VanishAction.isVanished);
    }

}
