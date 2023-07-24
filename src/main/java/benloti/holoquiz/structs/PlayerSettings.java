package benloti.holoquiz.structs;

public class PlayerSettings {
    private String suffix;
    private boolean notificationEnabled;

    public PlayerSettings(String suffix, boolean notificationEnabled) {
        this.suffix = suffix;
        this.notificationEnabled = notificationEnabled;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationSetting(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}
