package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dao.ChatMessageRepository;
import evelyn.site.socialmedia.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 儲存新的聊天訊息到mongoDB
    public void saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(message);
    }

    // 根據 chatRoomId 查詢聊天紀錄
    public List<ChatMessage> getMessagesByChatRoomId(String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    // 查詢某用戶參與的所有聊天室ID
    public List<String> getUserChatRooms(String username) {
        List<ChatMessage> messages = chatMessageRepository.findBySenderOrReceiver(username, username);
        // 從聊天紀錄中提取唯一的 chatRoomId
        Set<String> chatRoomIds = messages.stream()
                .map(ChatMessage::getChatRoomId)
                .collect(Collectors.toSet());

        return List.copyOf(chatRoomIds);
    }
}


