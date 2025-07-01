package benloti.holoquiz.games;

import java.util.List;

public class MinSDCheatDetector {
    private final boolean isEnabled;
    private final int minAnsUsed;
    private final double minSDReq;
    private final boolean countAsCorrect;
    private final List<String> cheatingCommands;

    public MinSDCheatDetector(boolean isEnabled, int minAnsUsed, double minReq, boolean countAsCorrect, List<String>cmdsExecuted) {
        this.isEnabled = isEnabled;
        this.minAnsUsed = minAnsUsed;
        this.minSDReq = minReq;
        this.countAsCorrect = countAsCorrect;
        this.cheatingCommands = cmdsExecuted;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isCountAsCorrect() {
        return countAsCorrect;
    }

    public List<String> getCheatingCommands() {
        return cheatingCommands;
    }

    public int getMinAnsUsed() {
        return minAnsUsed;
    }

    public double getMinSDReq() {
        return minSDReq;
    }
}