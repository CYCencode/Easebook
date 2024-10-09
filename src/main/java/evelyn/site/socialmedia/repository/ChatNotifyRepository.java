package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.ChatNotify;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatNotifyRepository extends MongoRepository<ChatNotify, String> {
    Optional<ChatNotify> findByChatRoomIdAndReceiverId(String chatRoomId, String receiver);
    // 查找所有未讀且接收者為指定ID的通知
    List<ChatNotify> findBySeenFalseAndReceiverIdOrderByCreateAtDesc(String receiverId);
}


