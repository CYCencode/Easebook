package evelyn.site.socialmedia.util;

import evelyn.site.socialmedia.enums.PostUploadLimit;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class PostValidationUtil {
    // 檢查圖片和影片數量
    public static void validateMediaCount(List<MultipartFile> images, List<MultipartFile> videos, int existingCount) {
        int totalMediaCount = existingCount + images.size() + videos.size();
        if (totalMediaCount > PostUploadLimit.TOTAL_AMOUNT.getValue()) {
            throw new IllegalArgumentException("最多只能上傳" + PostUploadLimit.TOTAL_AMOUNT.getValue() + "張圖片或影片");
        }
    }

    // 檢查圖片和影片大小
    public static void validateMediaSize(List<MultipartFile> images, List<MultipartFile> videos) {
        for (MultipartFile image : images) {
            if (image.getSize() > PostUploadLimit.FILE_SIZE.getValue()) {
                throw new IllegalArgumentException("圖片大小不可超過" + PostUploadLimit.FILE_SIZE.getValue() / (1024 * 1024) + "MB");
            }
        }
        for (MultipartFile video : videos) {
            if (video.getSize() > PostUploadLimit.FILE_SIZE.getValue()) {
                throw new IllegalArgumentException("影片大小不可超過" + PostUploadLimit.FILE_SIZE.getValue() / (1024 * 1024) + "MB");
            }
        }
    }

    // 檢查文字長度
    public static void validateContentLength(String content) {
        if (content == null || content.length() > PostUploadLimit.CONTENT_LENGTH.getValue()) {
            throw new IllegalArgumentException("文字長度不可超過" + PostUploadLimit.CONTENT_LENGTH.getValue() + "字");
        }
    }
}
