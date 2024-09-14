package evelyn.site.socialmedia.dao;

import evelyn.site.socialmedia.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 根據 chatRoomId 查詢該聊天室的所有訊息，並按時間順序排序
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
    // 查詢用戶作為發送者或接收者的所有聊天紀錄
    List<ChatMessage> findBySenderOrReceiver(String sender, String receiver);
}
