package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequestDTO {
    private String userId;
    private String content;
    private List<String> existingImages = new ArrayList<>();  // 貼文中已上傳圖片、且編輯後被保留、預設為空
    private List<String> existingVideos = new ArrayList<>();  // 貼文中已上傳影片、且編輯後被保留、預設為空
    private List<MultipartFile> newImages;  // 新上傳的圖片
    private List<MultipartFile> newVideos;  // 新上傳的影片
}

