package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.ChatNotify;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface ChatNotifyRepository extends MongoRepository<ChatNotify, String> {
    Optional<ChatNotify> findByChatRoomIdAndReceiverId(String chatRoomId, String receiver);

    // 查找所有未讀且接收者為指定ID的通知
    List<ChatNotify> findBySeenFalseAndReceiverIdOrderByCreateAtDesc(String receiverId);

    // 當profile 更新名字，對於訊息通知也需要修正
    @Query("{ 'senderId': ?0 }")
    @Update("{ '$set': { 'senderName': ?1 } }")
    void updateSenderNameBySenderId(String senderId, String newSenderName);
}


