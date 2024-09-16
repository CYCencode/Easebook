package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.repository.ChatRoomRepository;
import evelyn.site.socialmedia.model.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    public String getCreateChatRoomId(String user1, String user2) {
        // 將用戶按字母順序排序，保證 chatRoomId 唯一
        String chatRoomId = generateChatRoomId(user1, user2);

        // 查詢數據庫，檢查是否已經存在該聊天室
        if (!chatRoomExists(chatRoomId)){
            // 如果不存在，創建新聊天室並保存到數據庫
            ChatRoom newChatRoom = new ChatRoom();
            newChatRoom.setChatRoomId(chatRoomId);
            newChatRoom.setUser1(user1);
            newChatRoom.setUser2(user2);
            chatRoomRepository.save(newChatRoom);
            return chatRoomId;
        }
        return chatRoomId;
    }

    private String generateChatRoomId(String user1, String user2) {
        return Arrays.asList(user1, user2).stream().sorted().collect(Collectors.joining("-"));
    }

    public boolean chatRoomExists(String chatRoomId) {
        // 呼叫 repository 查詢是否存在聊天室
        return chatRoomRepository.findByChatRoomId(chatRoomId).isPresent();
    }
}

