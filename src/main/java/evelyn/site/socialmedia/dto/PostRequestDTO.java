package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDTO {
    private String userId;
    private String userName;
    private String userPhoto;
    private String content;
    private List<MultipartFile> images;
    private List<MultipartFile> videos;
    private Instant createAt;
}

