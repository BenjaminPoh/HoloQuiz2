package benloti.holoquiz.games;

import java.util.List;

public class MinTimeCheatDetector {
    private final boolean isEnabled;
    private final int minTimeRequired;
    private final boolean countAsCorrect;
    private final List<String> cheatingCommands;

    public MinTimeCheatDetector(boolean isEnabled, int minTimeReq, boolean countAsCorrect, List<String>cmdsExecuted) {
        this.isEnabled = isEnabled;
        this.minTimeRequired = minTimeReq;
        this.countAsCorrect = countAsCorrect;
        this.cheatingCommands = cmdsExecuted;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public int getMinTimeRequired() {
        return minTimeRequired;
    }

    public boolean isCountAsCorrect() {
        return countAsCorrect;
    }

    public List<String> getCheatingCommands() {
        return cheatingCommands;
    }
}
