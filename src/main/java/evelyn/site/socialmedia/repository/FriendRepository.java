package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.dto.FriendDTO;

import java.util.List;

public interface FriendRepository {
    List<FriendDTO> getFriendByUserId(String userId);

    List<FriendDTO> getFriendInfoByUserId(String username, String currentUserId);
}
