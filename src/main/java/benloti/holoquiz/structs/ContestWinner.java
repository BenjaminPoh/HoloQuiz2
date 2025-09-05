package benloti.holoquiz.structs;

import benloti.holoquiz.files.UserInterface;

public class ContestWinner {

    private final RewardTier contestPrize;
    private final PlayerContestStats contestWinnerData;
    private final int position;

    //Used to create the template
    public ContestWinner(RewardTier prizesTemplate, PlayerContestStats winner, int position, ContestInfo contestInfo, UserInterface userInterface) {
        this.contestWinnerData = winner;
        this.position = position;
        this.contestPrize = new RewardTier(prizesTemplate, this, contestInfo, userInterface);
    }

    public RewardTier getContestWinnerPrize() {
        return contestPrize;
    }

    public PlayerContestStats getContestWinnerData() {
        return contestWinnerData;
    }

    public int getPosition() {
        return position;
    }
}
