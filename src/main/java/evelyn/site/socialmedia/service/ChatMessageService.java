package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.repository.ChatMessageRepository;
import evelyn.site.socialmedia.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    // 儲存新的聊天訊息到mongoDB
    public void saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(message);
    }

    // 根據 chatRoomId 查詢聊天紀錄
    public List<ChatMessage> getMessagesByChatRoomId(String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }
}


