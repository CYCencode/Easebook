package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequestDTO {
    private String userId;
    private String username;
    private MultipartFile coverPhoto;  // 用於上傳的封面照片
    private MultipartFile photo;       // 用於上傳的個人照片
    private Instant birthday;
    private String location;
    private String phone;
    private String email;
    private String bio;
}