package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_profiles")
public class UserProfile {
    @Id
    private String id;
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

