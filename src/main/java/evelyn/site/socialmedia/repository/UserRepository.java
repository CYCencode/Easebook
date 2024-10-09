package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.dto.ProfileRequestDTO;
import evelyn.site.socialmedia.dto.UserDTO;
import evelyn.site.socialmedia.model.Users;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<UserDTO> findUsersByUsername(String username, String currentUserId);

    boolean existsByEmail(String email);

    Users save(Users user);

    Optional<Users> findByEmail(String email);

    void updateName(ProfileRequestDTO profileRequestDTO);

    String getUserNameByUserId(String userId);
}
