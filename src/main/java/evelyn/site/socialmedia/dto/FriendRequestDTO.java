package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestDTO {
    private String id;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private int status;
    private Instant createAt;
    private String senderAvatar;
}
