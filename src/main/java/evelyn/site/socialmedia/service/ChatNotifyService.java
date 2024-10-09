package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.model.ChatNotify;

import java.util.List;

public interface ChatNotifyService {
    void upsertChatNotify(ChatMessage chatMessage);
    List<ChatNotify> getChatRequests(String userId);
    void updateIsReadStatus(String chatRoomId, String receiverId);
}
