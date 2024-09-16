package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 取最新 n 筆
    @Query(value = "{ 'chatRoomId': ?0 }", sort = "{ 'timestamp': -1 }")
    List<ChatMessage> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);
}
//
//    // 根據 chatRoomId 查詢該聊天室的所有訊息，並按時間順序排序
//    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);

