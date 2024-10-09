package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.dto.ProfileResponseDTO;
import evelyn.site.socialmedia.repository.FriendRequestRepository;
import evelyn.site.socialmedia.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FriendRequestServiceImpl implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProfileService profileService;

    @Override
    public List<FriendRequestDTO> getFriendRequests(String userId) {
        return friendRequestRepository.findFriendRequestsByUserId(userId);
    }

    @Override
    public FriendRequestDTO sendFriendRequest(FriendRequestDTO friendRequestDTO) {
        String id = friendRequestRepository.insertFriendRequest(friendRequestDTO);
        friendRequestDTO.setId(id);
        log.info("sendFriendRequest friendRequestDTO : {}", friendRequestDTO);
        return friendRequestDTO;
    }

    @Override
    public ProfileResponseDTO replyToFriendRequest(String id, String senderId, String receiverId, boolean accept) {
        if (accept) {
            friendshipRepository.addFriendship(senderId, receiverId);
            friendRequestRepository.confirmFriendRequest(id);
            // 獲取發送者的個人資料，以更新接受邀請的使用者個人主頁朋友圈
            return profileService.getProfileByUserId(senderId);
        } else {
            // 拒絕好友邀請
            friendRequestRepository.declineFriendRequest(id);
            return null; // 不返回資料
        }
    }


}
