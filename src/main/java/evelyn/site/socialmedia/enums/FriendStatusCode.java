package evelyn.site.socialmedia.enums;

public enum FriendStatusCode {
    PENDING(0),
    FRIEND(1),
    DECLINE_OR_STRANGER(2);
    private final int code;

    FriendStatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
