package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);

    List<UserProfile> findByUserIdIn(List<String> userIds);
}
