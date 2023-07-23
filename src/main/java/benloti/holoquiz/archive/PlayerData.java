package benloti.holoquiz.archive;

public class PlayerData {
    private int pekoCount;
    private String name;

    public PlayerData() {
        this.pekoCount = 0;
        this.name = "TestingName peko";
    }

    public int getPekoCount() {
        return this.pekoCount;
    }

    public void increasePekoCount() {
        this.pekoCount += 1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
