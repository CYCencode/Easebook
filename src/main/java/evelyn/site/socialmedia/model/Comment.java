package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    // 透過 id 管理留言
    private String id;
    private String userId;
    private String userName;
    private String userPhoto;
    private String content;
    private Instant createAt;
}

