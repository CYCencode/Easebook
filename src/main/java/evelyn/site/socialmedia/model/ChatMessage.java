package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chatMessages")
public class ChatMessage {
    @Id
    private String id;
    private String chatRoomId;
    private String senderId;   // senderId
    private String senderName;  // 新增 senderName
    private String receiverId; // receiverId
    private String receiverName;  // 新增 receiverName
    private String content;
    private Instant createAt;
}




