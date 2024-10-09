package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostNotify {
    private String id;
    private String poster;
    private String receiver;
    private String postId;
    private Instant createAt;
}
