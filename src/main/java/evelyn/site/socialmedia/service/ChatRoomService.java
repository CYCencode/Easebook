package evelyn.site.socialmedia.service;


public interface ChatRoomService {
    String getCreateChatRoomId(String user1, String user2);

    boolean chatRoomExists(String chatRoomId);
}
