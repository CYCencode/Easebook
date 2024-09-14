package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    private Long id;
    private String chatRoomId;
    private String user1;
    private String user2;
    private LocalDateTime createdAt;
}

