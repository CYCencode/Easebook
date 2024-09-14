package evelyn.site.socialmedia.dao;

import evelyn.site.socialmedia.model.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository {
    // 查詢聊天室是否存在
    Optional<ChatRoom> findByChatRoomId(String chatRoomId);
    // 保存新的聊天室
    void save(ChatRoom chatRoom);
}
