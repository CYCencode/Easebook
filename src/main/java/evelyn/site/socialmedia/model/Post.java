package evelyn.site.socialmedia.model;

import evelyn.site.socialmedia.dto.ThumbUserDTO;
import evelyn.site.socialmedia.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "posts")
@CompoundIndex(name = "status_index", def = "{'status': 1}")
public class Post {
    @Id
    private String id;
    private String postId;
    private String userId;
    private String userName;
    private String userPhoto;
    private String content;
    private List<String> images;
    private List<String> videos;
    private Set<ThumbUserDTO> thumbUsers;
    private int thumbsCount;
    private int replyCount;
    private Instant createAt;
    private List<Comment> comments = new ArrayList<>();
    private int status = PostStatus.ACTIVE.getValue();
}
