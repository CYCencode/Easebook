package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.dto.ProfileResponseDTO;

import java.util.List;

public interface FriendRequestService {
    List<FriendRequestDTO> getFriendRequests(String userId);

    FriendRequestDTO sendFriendRequest(FriendRequestDTO friendRequestDTO);

    ProfileResponseDTO replyToFriendRequest(String id, String senderId, String receiverId, boolean accept);
}

