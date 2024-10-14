package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.dto.ProfileRequestDTO;
import evelyn.site.socialmedia.dto.UserDTO;
import evelyn.site.socialmedia.model.Users;
import evelyn.site.socialmedia.rowmapper.UserDTORowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Users save(Users user) {
        String sql = "INSERT INTO users (id, name, email, password, create_at) VALUES (?,?,?,?,?)";
        jdbcTemplate.update(sql, user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getCreateAt());
        return user;
    }

    @Override
    public String getUserNameByUserId(String userId) {
        String sql = "SELECT name FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, String.class);
    }

    @Override
    public void updateName(ProfileRequestDTO profileRequestDTO) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, profileRequestDTO.getUsername(), profileRequestDTO.getUserId());
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT count(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email); // 查詢 email 的數量
        return count != null && count > 0; // 如果 count 大於 0，表示該 email 已經存在
    }

    @Override
    public Optional<Users> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                Users user = new Users();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setCreateAt(rs.getTimestamp("create_at").toInstant());
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        }, email);
    }

    @Override
    public List<UserDTO> findUsersByUsername(String username, String currentUserId) {
        String sql = """
                SELECT u.id, u.name, 
                       fr.id AS friend_request_id,
                       CASE 
                           WHEN fr.status = 0 AND fr.sender_id = :currentUserId THEN 0  -- currentUser 是發送者，狀態為 SENDER_PENDING
                           WHEN fr.status = 0 AND fr.receiver_id = :currentUserId THEN 1  -- currentUser 是接收者，狀態為 RECEIVER_PENDING
                           WHEN fr.status = 1 THEN 2  -- 狀態為 FRIEND
                           ELSE 3  -- DECLINE 或沒有記錄
                           END AS status
                    FROM users u 
                    LEFT JOIN friend_request fr ON fr.id = (
                        SELECT id FROM friend_request 
                        WHERE (sender_id = :currentUserId AND receiver_id = u.id) 
                           OR (receiver_id = :currentUserId AND sender_id = u.id)
                        ORDER BY create_at DESC LIMIT 1  -- 只選取最新的好友請求
                    )
                    WHERE u.name LIKE :username AND u.id != :currentUserId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("currentUserId", currentUserId);
        params.addValue("username", "%" + username + "%");

        return namedParameterJdbcTemplate.query(sql, params, new UserDTORowMapper());
    }

    @Override
    public UserDTO findFriendshipById(String userId, String currentUserId) {
        String sql = """
                SELECT u.id, u.name, 
                       fr.id AS friend_request_id,
                       CASE 
                           WHEN fr.status = 0 AND fr.sender_id = :currentUserId THEN 0  -- currentUser 是發送者，狀態為 SENDER_PENDING
                           WHEN fr.status = 0 AND fr.receiver_id = :currentUserId THEN 1  -- currentUser 是接收者，狀態為 RECEIVER_PENDING
                           WHEN fr.status = 1 THEN 2  -- 狀態為 FRIEND
                           ELSE 3  -- DECLINE 或沒有記錄
                       END AS status
                FROM users u 
                LEFT JOIN friend_request fr ON fr.id = (
                    SELECT id FROM friend_request 
                    WHERE (sender_id = :currentUserId AND receiver_id = u.id) 
                       OR (receiver_id = :currentUserId AND sender_id = u.id)
                    ORDER BY create_at DESC LIMIT 1  -- 只選取最新的好友請求
                )
                WHERE u.id = :userId AND u.id != :currentUserId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("currentUserId", currentUserId);
        params.addValue("userId", userId);

        List<UserDTO> users = namedParameterJdbcTemplate.query(sql, params, new UserDTORowMapper());

        if (users != null && !users.isEmpty()) {
            return users.get(0);
        } else {
            return null;
        }
    }

}
