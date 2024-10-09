package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 取最新 n 筆
    @Query(value = "{ 'chatRoomId': ?0 }", sort = "{ 'createAt': -1 }")
    List<ChatMessage> findByChatRoomIdOrderByCreateAtDesc(String chatRoomId, Pageable pageable);
}
