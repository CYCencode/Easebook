package evelyn.site.socialmedia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatNotifyUpdateRequest {
    private String chatRoomId;
    private String receiverId;
}
