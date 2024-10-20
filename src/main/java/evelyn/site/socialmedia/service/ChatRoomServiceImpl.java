package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.model.ChatRoom;
import evelyn.site.socialmedia.repository.ChatRoomRepository;
import evelyn.site.socialmedia.util.ChatRoomIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public String getCreateChatRoomId(String user1, String user2) {
        String chatRoomId = null;
        // 生成唯一的 chatRoomId
        try {
            chatRoomId = ChatRoomIdUtil.generateChatRoomId(user1, user2);
        } catch (NoSuchAlgorithmException e) {
            log.error("no such algorithm", e);
        }

        // 查詢資料庫，檢查是否已經存在該聊天室
        if (!chatRoomExists(chatRoomId) && chatRoomId != null) {
            // 如果不存在，創建新聊天室並保存到資料庫
            ChatRoom newChatRoom = new ChatRoom();
            newChatRoom.setChatRoomId(chatRoomId);
            newChatRoom.setUser1(user1);
            newChatRoom.setUser2(user2);
            newChatRoom.setCreateAt(Instant.now());
            chatRoomRepository.save(newChatRoom);
            return chatRoomId;
        }
        return chatRoomId;
    }

    @Override
    public boolean chatRoomExists(String chatRoomId) {
        // 呼叫 repository 查詢是否存在聊天室
        return chatRoomRepository.findByChatRoomId(chatRoomId).isPresent();
    }
}

