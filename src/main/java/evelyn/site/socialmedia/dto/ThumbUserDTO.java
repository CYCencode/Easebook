package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThumbUserDTO {
    private String userId;
    private String userName;
    private String avatarUrl;
}

