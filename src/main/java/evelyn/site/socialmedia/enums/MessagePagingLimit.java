package evelyn.site.socialmedia.enums;

public enum MessagePagingLimit {
    LIMIT(30);
    private final int limit;

    MessagePagingLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}

