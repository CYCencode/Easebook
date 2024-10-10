package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.*;
import evelyn.site.socialmedia.model.Comment;
import evelyn.site.socialmedia.model.Post;
import evelyn.site.socialmedia.model.UserProfile;
import evelyn.site.socialmedia.repository.*;
import evelyn.site.socialmedia.util.UploadS3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatNotifyRepository chatNotifyRepository;
    @Value("${defaultUserPhoto}")
    private String defaultUserPhoto;
    @Value("${defaultCoverPhoto}")
    private String defaultCoverPhoto;

    private final UserProfileRepository userProfileRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UploadS3Util uploadS3Util;
    private final S3Service s3Service;
    private final NotifyService notifyService;

    @Override
    public ProfileResponseDTO updateProfile(ProfileRequestDTO profileRequestDTO) throws IOException {
        // 查找用戶的現有個人檔案
        Optional<UserProfile> existingProfileOptional = userProfileRepository.findByUserId(profileRequestDTO.getUserId());
        log.info("existingProfileOptional {}", existingProfileOptional);
        UserProfile userProfile;
        if (existingProfileOptional.isPresent()) {
            userProfile = existingProfileOptional.get();
        } else {
            log.info("userProfile is null");
            throw new RuntimeException("UserProfile not found");
        }

        // 如果有上傳封面照片
        if (profileRequestDTO.getCoverPhoto() != null && !profileRequestDTO.getCoverPhoto().isEmpty()) {
            String coverPhotoName = uploadS3Util.uploadFile(profileRequestDTO.getCoverPhoto());
            // 將檔案名轉換為 S3 URL 並儲存
            String coverPhotoUrl = s3Service.getFileUrl(coverPhotoName);
            userProfile.setCoverPhoto(coverPhotoUrl);
        }

        // 如果有上傳個人照片
        if (profileRequestDTO.getPhoto() != null && !profileRequestDTO.getPhoto().isEmpty()) {
            log.info("profileRequestDTO.getPhoto() {}", profileRequestDTO.getPhoto());
            String photoName = uploadS3Util.uploadFile(profileRequestDTO.getPhoto());
            // 將檔案名轉換為 S3 URL 並儲存
            String photoUrl = s3Service.getFileUrl(photoName);
            userProfile.setPhoto(photoUrl);
            // 查詢post 資訊
            List<Post> posts = postRepository.findByUserIdAndStatus(profileRequestDTO.getUserId(), "ACTIVE");
            // 更新每個 Post 的 userPhoto
            for (Post post : posts) {
                post.setUserPhoto(photoUrl);
                // 將post 轉 postResponseDTO，傳給前端更新貼文顯示
                notifyService.notifyFriendsOfPostUpdate(convertToResponseDTO(post));
            }
            // 查詢自己的按讚紀錄，更新大頭照
            List<Post> postsWithThumbs = postRepository.findPostsByThumberId(profileRequestDTO.getUserId());
            for (Post post : postsWithThumbs) {
                Set<ThumbUserDTO> thumbUsers = post.getThumbUsers();
                if (thumbUsers != null) {
                    for (ThumbUserDTO thumbUser : thumbUsers) {
                        if (thumbUser.getUserId().equals(profileRequestDTO.getUserId())) {
                            // 更新按讚者的名稱
                            thumbUser.setAvatarUrl(photoUrl);
                        }
                    }
                }
            }
            postRepository.saveAll(postsWithThumbs);
            // 查詢自己的評論紀錄，更新大頭照
            List<Post> postsWithComments = postRepository.findPostsByCommenterId(profileRequestDTO.getUserId());
            for (Post post : postsWithComments) {
                List<Comment> comments = post.getComments();
                for (Comment comment : comments) {
                    if (comment.getUserId().equals(profileRequestDTO.getUserId())) {
                        comment.setUserPhoto(photoUrl);
                    }
                }
            }
            postRepository.saveAll(postsWithComments);

            // 查詢自己的好友邀請紀錄，更新大頭照
            Optional<List<String>> receiverIds = friendRequestRepository.findFriendRequestReceiverIdBySenderId(profileRequestDTO.getUserId());
            log.info("update profile name : friend request receiverIds {}", receiverIds);
            // 通知所有對應的好友（使用 WebSocket）
            receiverIds.ifPresent(receivers -> receivers.forEach(receiverId -> {
                FriendRequestDTO friendRequestDTO = FriendRequestDTO.builder()
                        .senderId(profileRequestDTO.getUserId())
                        .senderName(profileRequestDTO.getUsername())
                        .senderAvatar(photoUrl)
                        .receiverId(receiverId)
                        .build();
                notifyService.notifyFriendRequestUpdate(receiverId, friendRequestDTO);
            }));

            // 即時通知 websocket 更新個人大頭照
            notifyService.notifyOfPhotoUpdate(profileRequestDTO.getUserId(), photoUrl);
            // 保存所有更新過的 Post
            postRepository.saveAll(posts);
        }
        // 如果使用者名稱變更，需要更新過去的貼文資訊
        log.info("profileRequestDTO getUsername {}", profileRequestDTO.getUsername());
        log.info("name equal : {}", profileRequestDTO.getUsername().equals(userProfile.getUsername()));
        if (!userProfile.getUsername().equals(profileRequestDTO.getUsername())) {
            String userId = profileRequestDTO.getUserId();
            String newUserName = profileRequestDTO.getUsername();
            // 更新 users 表
            userRepository.updateName(profileRequestDTO);
            // 查詢post 資訊
            List<Post> posts = postRepository.findByUserIdAndStatus(userId, "ACTIVE");
            // 更新每個 Post 的 userName
            for (Post post : posts) {
                post.setUserName(newUserName);
                notifyService.notifyFriendsOfPostUpdate(convertToResponseDTO(post));
            }
            // 保存所有更新過的 Post
            postRepository.saveAll(posts);
            // 查詢自己的評論紀錄，更新 userName
            List<Post> postsWithComments = postRepository.findPostsByCommenterId(userId);
            for (Post post : postsWithComments) {
                List<Comment> comments = post.getComments();
                for (Comment comment : comments) {
                    if (comment.getUserId().equals(userId)) {
                        // 更新評論名稱
                        comment.setUserName(newUserName);
                    }
                }
            }
            postRepository.saveAll(postsWithComments);
            // 查詢自己的按讚紀錄，更新 userName
            List<Post> postsWithThumbs = postRepository.findPostsByThumberId(userId);
            for (Post post : postsWithThumbs) {
                Set<ThumbUserDTO> thumbUsers = post.getThumbUsers();
                if (thumbUsers != null) {
                    for (ThumbUserDTO thumbUser : thumbUsers) {
                        if (thumbUser.getUserId().equals(userId)) {
                            // 更新按讚者的名稱
                            thumbUser.setUserName(newUserName);
                        }
                    }
                }
            }
            postRepository.saveAll(postsWithThumbs);

            // 查詢自己的好友邀請紀錄，更新 userName
            Optional<List<String>> receiverIds = friendRequestRepository.updateFriendRequestUserName(userId, newUserName);
            log.info("update profile name : friend request receiverIds {}", receiverIds);
            // 通知所有對應的好友（使用 WebSocket）
            receiverIds.ifPresent(receivers -> receivers.forEach(receiverId -> {
                FriendRequestDTO friendRequestDTO = FriendRequestDTO.builder()
                        .senderId(userId)
                        .senderName(newUserName)
                        .receiverId(receiverId)
                        .build();
                notifyService.notifyFriendRequestUpdate(receiverId, friendRequestDTO);
            }));
            // 查詢訊息紀錄，更新 senderName
            chatNotifyRepository.updateSenderNameBySenderId(userId, newUserName);
            // 通知所有對應的好友（使用 WebSocket）

            // 即時通知 websocket 更新個人名稱
            notifyService.notifyOfNameUpdate(userId, newUserName);

        }
        userProfile.setUsername(profileRequestDTO.getUsername());
        // 更新其他資料
        userProfile.setBirthday(profileRequestDTO.getBirthday());
        userProfile.setLocation(profileRequestDTO.getLocation());
        userProfile.setEmail(profileRequestDTO.getEmail());
        userProfile.setPhone(profileRequestDTO.getPhone());
        userProfile.setBio(profileRequestDTO.getBio());
        // 保存用戶個人檔案
        userProfileRepository.save(userProfile);
        // 返回 DTO
        return convertToResponseDTO(userProfile);
    }

    @Override
    public ProfileResponseDTO getProfileByUserId(String userId) {
        Optional<UserProfile> existingProfileOptional = userProfileRepository.findByUserId(userId);
        if (existingProfileOptional.isPresent()) {
            UserProfile userProfile = existingProfileOptional.get();
            return convertToResponseDTO(userProfile);
        } else {
            throw new RuntimeException("Profile not found for userId: " + userId);
        }
    }

    private ProfileResponseDTO convertToResponseDTO(UserProfile userProfile) {
        ProfileResponseDTO responseDTO = new ProfileResponseDTO();
        responseDTO.setUserId(userProfile.getUserId());
        responseDTO.setUsername(userProfile.getUsername());
        responseDTO.setBirthday(userProfile.getBirthday());
        responseDTO.setLocation(userProfile.getLocation());
        responseDTO.setEmail(userProfile.getEmail());
        responseDTO.setPhone(userProfile.getPhone());
        responseDTO.setBio(userProfile.getBio());

        // 將檔案名稱轉換為完整的 S3 URL
        String coverPhotoUrls = userProfile.getCoverPhoto() != null ? userProfile.getCoverPhoto() : defaultCoverPhoto;
        String photoUrls = userProfile.getPhoto() != null ? userProfile.getPhoto() : defaultUserPhoto;

        responseDTO.setCoverPhoto(coverPhotoUrls);
        responseDTO.setPhoto(photoUrls);
        log.info("responseDTO {}", responseDTO);
        return responseDTO;
    }

    private PostResponseDTO convertToResponseDTO(Post post) {
        PostResponseDTO responseDTO = new PostResponseDTO();
        responseDTO.setPostId(post.getPostId());
        responseDTO.setUserId(post.getUserId());
        responseDTO.setUserName(post.getUserName());
        responseDTO.setUserPhoto(post.getUserPhoto());
        responseDTO.setContent(post.getContent());
        responseDTO.setImages(post.getImages());
        responseDTO.setVideos(post.getVideos());
        responseDTO.setThumbUsers(post.getThumbUsers());
        responseDTO.setThumbsCount(post.getThumbsCount());
        responseDTO.setReplyCount(post.getReplyCount());
        responseDTO.setCreateAt(post.getCreateAt());

        if (post.getComments() != null && !post.getComments().isEmpty()) {
            responseDTO.setComments(new ArrayList<>(post.getComments()));
        } else {
            responseDTO.setComments(new ArrayList<>());
        }
        return responseDTO;
    }
}