package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.FriendDTO;
import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.dto.PostResponseDTO;
import evelyn.site.socialmedia.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    private final FriendRepository friendRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 通知用戶的所有好友有新的發文
    @Override
    public void notifyFriendsOfPost(PostResponseDTO postResponseDTO) {
        // 個人頁上也需要更新
        messagingTemplate.convertAndSendToUser(
                postResponseDTO.getUserId(),
                "/queue/notify/post",
                postResponseDTO
        );
        // 從資料庫查詢用戶的所有好友
        List<FriendDTO> friends = friendRepository.getFriendByUserId(postResponseDTO.getUserId());
        for (FriendDTO friend : friends) {
            log.info("notify friend : {}", friend.getFriendName());
            // 發送通知到每個好友的專屬頻道
            messagingTemplate.convertAndSendToUser(
                    friend.getFriendId(),
                    "/queue/notify/post",
                    postResponseDTO
            );
        }
    }

    // 個人主頁用戶大頭照更新
    @Override
    public void notifyOfPhotoUpdate(String userId, String photoUrl) {
        // 統一構建 updateInfo Map
        Map<String, String> updateInfo = new HashMap<>();
        updateInfo.put("userId", userId);
        updateInfo.put("photoUrl", photoUrl);

        // 自己本人頁面上的資訊更新
        messagingTemplate.convertAndSendToUser(userId, "/queue/notify/user/photo/update", updateInfo);

        // 通知好友更新他們的好友圈
        List<FriendDTO> friends = friendRepository.getFriendByUserId(userId);
        for (FriendDTO friend : friends) {
            messagingTemplate.convertAndSendToUser(friend.getFriendId(), "/queue/notify/user/photo/update", updateInfo);
        }
    }

    // 個人主頁用戶名字更新
    @Override
    public void notifyOfNameUpdate(String userId, String username) {
        // 統一構建 updateInfo Map
        Map<String, String> updateInfo = new HashMap<>();
        updateInfo.put("userId", userId);
        updateInfo.put("username", username);

        // 自己本人頁面上的資訊更新
        messagingTemplate.convertAndSendToUser(userId, "/queue/notify/user/name/update", updateInfo);

        // 通知好友更新他們的好友圈
        List<FriendDTO> friends = friendRepository.getFriendByUserId(userId);
        for (FriendDTO friend : friends) {
            messagingTemplate.convertAndSendToUser(friend.getFriendId(), "/queue/notify/user/name/update", updateInfo);
        }
    }


    @Override
    public void notifyFriendsOfPostUpdate(PostResponseDTO postResponseDTO) {
        log.info("notify postResponseDTO.getUserId() {}", postResponseDTO.getUserId());
        List<FriendDTO> friends = friendRepository.getFriendByUserId(postResponseDTO.getUserId());
        // 按讚、留言狀態的更新除了好友頁面要及時調整，自己的頁面也是
        messagingTemplate.convertAndSendToUser(
                postResponseDTO.getUserId(),
                "/queue/notify/post/update",
                postResponseDTO);
        for (FriendDTO friend : friends) {
            messagingTemplate.convertAndSendToUser(
                    friend.getFriendId(),
                    "/queue/notify/post/update",
                    postResponseDTO
            );
        }
    }

    @Override
    public void notifyFriendsOfThumbUpdate(PostResponseDTO postResponseDTO) {
        log.info("notify thumb update for postResponseDTO.getUserId() {}", postResponseDTO.getUserId());
        List<FriendDTO> friends = friendRepository.getFriendByUserId(postResponseDTO.getUserId());
        messagingTemplate.convertAndSendToUser(
                postResponseDTO.getUserId(),
                "/queue/notify/thumb/update",
                postResponseDTO
        );
        for (FriendDTO friend : friends) {
            messagingTemplate.convertAndSendToUser(
                    friend.getFriendId(),
                    "/queue/notify/thumb/update",
                    postResponseDTO
            );
        }
    }

    @Override
    public void notifyFriendsOfCommentUpdate(PostResponseDTO postResponseDTO) {
        log.info("notify comment update for postResponseDTO.getUserId() {}", postResponseDTO.getUserId());
        List<FriendDTO> friends = friendRepository.getFriendByUserId(postResponseDTO.getUserId());
        messagingTemplate.convertAndSendToUser(
                postResponseDTO.getUserId(),
                "/queue/notify/comment/update",
                postResponseDTO
        );
        for (FriendDTO friend : friends) {
            messagingTemplate.convertAndSendToUser(
                    friend.getFriendId(),
                    "/queue/notify/comment/update",
                    postResponseDTO
            );
        }
    }

    @Override
    public void notifyFriendsOfCommentDelete(PostResponseDTO postResponseDTO) {
        List<FriendDTO> friends = friendRepository.getFriendByUserId(postResponseDTO.getUserId());
        messagingTemplate.convertAndSendToUser(
                postResponseDTO.getUserId(),
                "/queue/notify/comment/delete",
                postResponseDTO
        );
        for (FriendDTO friend : friends) {
            // 發送通知到每個好友的專屬頻道
            messagingTemplate.convertAndSendToUser(
                    friend.getFriendId(),
                    "/queue/notify/comment/delete",
                    postResponseDTO
            );
        }
    }


    @Override
    public void notifyFriendsOfPostDelete(PostResponseDTO postResponseDTO) {
        log.info("DELETE notify friend : {}", postResponseDTO.getUserId());
        // 自己的頁面也要更新
        messagingTemplate.convertAndSendToUser(
                postResponseDTO.getUserId(),
                "/queue/notify/post/delete",
                postResponseDTO
        );
        // 從資料庫查詢用戶的所有好友
        List<FriendDTO> friends = friendRepository.getFriendByUserId(postResponseDTO.getUserId());
        for (FriendDTO friend : friends) {
            // 發送通知到每個好友的專屬頻道
            messagingTemplate.convertAndSendToUser(
                    friend.getFriendId(),
                    "/queue/notify/post/delete",
                    postResponseDTO
            );
        }
    }

    // 新增一方法，將通知傳給receiver
    @Override
    public void notifyFriendRequestUpdate(String receiverId, FriendRequestDTO friendRequestDTO) {
        messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/notify/friend",
                friendRequestDTO);
    }

}

