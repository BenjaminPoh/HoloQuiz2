package benloti.holoquiz2.data;

public class PlayerData {
    private int pekoCount;
    private String name;

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

    public void initialiseData() {
        this.pekoCount = 0;
        this.name = "TestingName peko";
    }

}
