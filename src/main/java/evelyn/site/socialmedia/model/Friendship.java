package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    private String user1;
    private String user2;
    private Instant createAt;
}
