package evelyn.site.socialmedia.dto;

import evelyn.site.socialmedia.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDTO {
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
    private List<Comment> comments;
    private boolean liked = false;
}




