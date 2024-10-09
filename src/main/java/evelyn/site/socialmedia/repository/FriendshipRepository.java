package evelyn.site.socialmedia.repository;

public interface FriendshipRepository {
    void addFriendship(String senderId, String receiverId);

}
