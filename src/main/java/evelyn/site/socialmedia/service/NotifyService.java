package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.dto.PostResponseDTO;


public interface NotifyService {
    void notifyFriendsOfPost(PostResponseDTO postResponseDTO);

    void notifyFriendsOfPostUpdate(PostResponseDTO postResponseDTO);

    void notifyFriendsOfThumbUpdate(PostResponseDTO postResponseDTO);

    void notifyFriendsOfCommentUpdate(PostResponseDTO postResponseDTO);

    void notifyFriendsOfPostDelete(PostResponseDTO postResponseDTO);

    void notifyFriendsOfCommentDelete(PostResponseDTO postResponseDTO);

    void notifyOfPhotoUpdate(String userId, String photoUrl);

    void notifyOfNameUpdate(String userId, String username);

    void notifyFriendRequestUpdate(String receiverId, FriendRequestDTO friendRequestDTO);
}
