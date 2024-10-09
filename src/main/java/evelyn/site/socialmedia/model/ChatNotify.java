package evelyn.site.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chatNotify")
@CompoundIndex(name = "chatRoom_receiver_idx", def = "{'chatRoomId': 1, 'receiver': 1}", unique = true)
public class ChatNotify {
    @Id
    private String id;
    private String chatRoomId;
    private String senderName;
    private String senderId;
    private String receiverId;  // UUID 格式的接收者, for websocket notify
    private String content; // 最新一筆訊息內文
    private boolean seen = false; // 是否已被點開
    private Instant createAt;
}
