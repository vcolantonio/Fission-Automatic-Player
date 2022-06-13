package utils.enums;

public enum GridContent {
    WHITE((byte) 0),
    BLACK((byte) 1),
    EMPTY((byte) -1);

    public byte content;

    GridContent(byte b) {
        this.content = b;
    }
}