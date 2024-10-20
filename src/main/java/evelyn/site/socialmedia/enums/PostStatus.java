package evelyn.site.socialmedia.enums;

public enum PostStatus {
    ACTIVE(0),
    DELETED(1);
    private final int value;

    PostStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
