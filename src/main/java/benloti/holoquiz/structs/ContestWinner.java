package benloti.holoquiz.structs;

public class ContestWinner {

    private final ContestRewardTier contestPrize;
    private final PlayerData contestWinnerData;
    private final int position;

    public ContestWinner(ContestRewardTier prizes, PlayerData winner, int position) {
        this.contestPrize = prizes;
        this.contestWinnerData = winner;
        this.position = position;
    }

    public ContestRewardTier getContestWinnerPrize() {
        return contestPrize;
    }

    public PlayerData getContestWinnerData() {
        return contestWinnerData;
    }

    public int getPosition() {
        return position;
    }
}
