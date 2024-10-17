package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.ChatMessageDTO;
import evelyn.site.socialmedia.model.ChatMessage;

import java.time.Instant;
import java.util.List;

public interface ChatMessageService {
    void saveMessage(ChatMessage message);

    List<ChatMessage> getMessagesByChatRoomId(String chatRoomId, Instant lastCreateAt, int messageLimit);

    ChatMessageDTO getPagingMessagesByChatRoomId(String chatRoomId, Instant lastCreateAt);

    void processChatMessage(ChatMessage chatMessage);
}
