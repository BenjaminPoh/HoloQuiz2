package benloti.holoquiz.structs;

public class MathOPNode {

    private final String value;
    private final boolean isOperation;

    public MathOPNode(String value, boolean isOperation) {
        this.value = value;
        this.isOperation = isOperation;
    }

    public boolean isOperation() {
        return isOperation;
    }

    public String getValue() {
        return value;
    }
}
