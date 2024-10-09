package evelyn.site.socialmedia.enums;

public enum InitCounts {
    ZERO(0);
    private final int code;

    InitCounts(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
