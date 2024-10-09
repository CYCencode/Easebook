package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.dto.FriendRequestDTO;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository {
    List<FriendRequestDTO> findFriendRequestsByUserId(String userId);

    String insertFriendRequest(FriendRequestDTO friendRequestDTO);

    void declineFriendRequest(String id);

    void confirmFriendRequest(String id);

    Optional<List<String>> updateFriendRequestUserName(String userId, String newName);
}
