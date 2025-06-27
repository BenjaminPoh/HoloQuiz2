package benloti.holoquiz.structs;

import benloti.holoquiz.files.UserInterface;

public class ContestWinner {

    private final ContestRewardTier contestPrize;
    private final PlayerContestStats contestWinnerData;
    private final int position;

    public ContestWinner(ContestRewardTier prizes, PlayerContestStats winner, int position) {
        this.contestPrize = prizes;
        this.contestWinnerData = winner;
        this.position = position;
    }

    public ContestWinner(ContestWinner template, ContestInfo contestInfo, UserInterface userInterface) {
        this.contestPrize = new ContestRewardTier(template, contestInfo, userInterface);
        this.contestWinnerData = template.getContestWinnerData();
        this.position = template.getPosition();
    }

    public ContestRewardTier getContestWinnerPrize() {
        return contestPrize;
    }

    public PlayerContestStats getContestWinnerData() {
        return contestWinnerData;
    }

    public int getPosition() {
        return position;
    }
}
