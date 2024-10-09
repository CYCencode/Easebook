package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private String id;
    private String email;
    private String password;
    private String name;
    private String photo;
    private Instant createAt;
}
