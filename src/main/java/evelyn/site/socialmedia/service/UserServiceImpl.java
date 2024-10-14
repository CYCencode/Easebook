package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.FriendDTO;
import evelyn.site.socialmedia.dto.UserDTO;
import evelyn.site.socialmedia.model.UserProfile;
import evelyn.site.socialmedia.repository.FriendRepository;
import evelyn.site.socialmedia.repository.UserProfileRepository;
import evelyn.site.socialmedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FriendRepository friendRepository;
    private final S3Service s3Service;

    @Override
    public List<UserDTO> findUsersByName(String username, String currentUserId) {
        List<UserDTO> users = userRepository.findUsersByUsername(username, currentUserId);
        log.info("users: {}", users);
        return users;
    }

    @Override
    public UserDTO findFriendshipById(String userId, String currentUserId) {
        UserDTO user = userRepository.findFriendshipById(userId, currentUserId);
        log.info("users: {}", user);
        return user;
    }

    @Override
    public List<FriendDTO> findFriendsByName(String username, String currentUserId) {
        List<FriendDTO> friends = friendRepository.getFriendInfoByUserId(username, currentUserId);
        log.info("friends: {}", friends);
        return friends;
    }

    @Override
    // 分成兩階段，找好友＆找profile
    public List<UserProfile> findFriendByUserId(String userId) {
        List<FriendDTO> friends = friendRepository.getFriendByUserId(userId);
        List<String> friendIds = friends.stream()
                .map(FriendDTO::getFriendId)
                .collect(Collectors.toList());
        log.info("find friends: {}", friendIds);
        return userProfileRepository.findByUserIdIn(friendIds);
    }
}



