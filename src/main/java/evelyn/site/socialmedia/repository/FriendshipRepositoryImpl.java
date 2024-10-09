package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.util.DbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class FriendshipRepositoryImpl implements FriendshipRepository {

    private final JdbcTemplate jdbcTemplate;
    @Override
    public void addFriendship(String senderId, String receiverId) {
        // 使用 compareTo 比較字串大小，來確定 user_1 和 user_2 的順序
        String user1 = (senderId.compareTo(receiverId) < 0) ? senderId : receiverId;
        String user2 = (senderId.compareTo(receiverId) > 0) ? senderId : receiverId;
        String sql = "INSERT INTO friendship (user_1, user_2) VALUES (?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, user1, user2);
        DbUtil.checkRowAffected(rowsAffected,
                "Successfully add Friendship",
                "Failed to add Friendship: " + senderId + " - " + receiverId);
    }

}
