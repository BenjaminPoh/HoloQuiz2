package benloti.holoquiz.dependencies;

import net.Zrips.CMILib.Colors.CMIChatColor;

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

}
