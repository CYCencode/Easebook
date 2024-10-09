package evelyn.site.socialmedia.service;

import java.security.NoSuchAlgorithmException;

public interface ChatRoomService {
    String getCreateChatRoomId(String user1, String user2);
    boolean chatRoomExists(String chatRoomId);
}
