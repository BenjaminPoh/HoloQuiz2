package benloti.holoquiz.dependencies;

import net.Zrips.CMILib.Colors.CMIChatColor;

public class CMIDep {
    public CMIDep() {
    }

    public String translateHexColors(String string) {
        return CMIChatColor.translate(string);
    }

}
