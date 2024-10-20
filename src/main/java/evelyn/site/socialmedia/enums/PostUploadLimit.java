package evelyn.site.socialmedia.enums;

public enum PostUploadLimit {
    CONTENT_LENGTH(1000),
    TOTAL_AMOUNT(3),          // 最多上傳 3 個檔案
    FILE_SIZE(2 * 1024 * 1024); // 上傳檔案大小限制 2MB
    private final int value;

    PostUploadLimit(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
