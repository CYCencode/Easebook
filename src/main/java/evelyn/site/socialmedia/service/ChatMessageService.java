package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.model.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    void saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesByChatRoomId(String chatRoomId);
    void processChatMessage(ChatMessage chatMessage);
}
