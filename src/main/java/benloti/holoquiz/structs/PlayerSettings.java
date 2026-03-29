package benloti.holoquiz.structs;

public class PlayerSettings {
    private String suffix;
    private boolean notificationEnabled;
    private boolean alertEnabled;

    public PlayerSettings(String suffix, boolean notificationEnabled, boolean alertEnabled) {
        this.suffix = suffix;
        this.notificationEnabled = notificationEnabled;
        this.alertEnabled = alertEnabled;
    }

    public boolean sendAlert() {
        return notificationEnabled && alertEnabled;
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

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertSetting(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

}
