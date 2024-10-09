package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseDTO {
    private String userId;
    private String username;
    private String coverPhoto;
    private String photo;
    private Instant birthday;
    private String location;
    private String phone;
    private String email;
    private String bio;
}
