package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.enums.FriendStatusCode;
import evelyn.site.socialmedia.rowmapper.FriendRequestDTORowMapper;
import evelyn.site.socialmedia.util.DbUtil;
import evelyn.site.socialmedia.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
@Log4j2
public class FriendRequestRepositoryImpl implements FriendRequestRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<FriendRequestDTO> findFriendRequestsByUserId(String userId) {
        String sql = "SELECT * FROM friend_request WHERE receiver_id = ? AND status =?";
        return jdbcTemplate.query(sql, new Object[]{userId, FriendStatusCode.PENDING.getCode()}, new FriendRequestDTORowMapper());
    }

    @Override
    public String insertFriendRequest(FriendRequestDTO friendRequestDTO) {
        String sql = "INSERT INTO friend_request (id, sender_id, sender_name, receiver_id, receiver_name, status, create_at) " +
                "VALUES (:id, :sender_id, :sender_name, :receiver_id, :receiver_name,:status,:create_at)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        String id = UUIDGenerator.generateUUID();
        params.addValue("id", id);
        params.addValue("sender_id", friendRequestDTO.getSenderId());
        params.addValue("sender_name", friendRequestDTO.getSenderName());
        params.addValue("receiver_id", friendRequestDTO.getReceiverId());
        params.addValue("receiver_name", friendRequestDTO.getReceiverName());
        params.addValue("status", FriendStatusCode.PENDING.getCode());
        params.addValue("create_at", friendRequestDTO.getCreateAt());

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        DbUtil.checkRowAffected(rowsAffected,
                "Successfully inserted Friend Request for sender: " + friendRequestDTO.getSenderId(),
                "Failed to insert Friend Request");
        return id;
    }

    @Override
    public void confirmFriendRequest(String id) {
        String sql = "UPDATE friend_request SET status = ? WHERE id = ? ";
        int rowsAffected = jdbcTemplate.update(sql, FriendStatusCode.FRIEND.getCode(), id);
        DbUtil.checkRowAffected(rowsAffected,
                "Successfully confirm Friend Request",
                "Failed to confirm Friend Request");
    }

    @Override
    public void declineFriendRequest(String id) {
        String sql = "UPDATE friend_request SET status = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, FriendStatusCode.DECLINE_OR_STRANGER.getCode(), id);
        DbUtil.checkRowAffected(rowsAffected,
                "Successfully decline Friend Request",
                "Failed to decline Friend Request");
    }

    @Override
    public Optional<List<String>> findFriendRequestReceiverIdBySenderId(String userId) {
        // 查詢所有與 userId 是 sender 的記錄，返回 receiver_id
        String selectSql = "SELECT receiver_id FROM friend_request " +
                "WHERE sender_id = :userId AND status = :status";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("status", FriendStatusCode.PENDING.getCode());

        // 查詢對應的 receiver_id
        List<String> receiverIds = namedParameterJdbcTemplate.queryForList(selectSql, params, String.class);
        // 如果有符合條件的 receiver_id，則返回
        return receiverIds.isEmpty() ? Optional.empty() : Optional.of(receiverIds);
    }

    @Override
    public Optional<List<String>> updateFriendRequestUserName(String userId, String newName) {
        // 查詢所有與 userId 是 sender 的記錄，返回 receiver_id
        String selectSql = "SELECT receiver_id FROM friend_request " +
                "WHERE sender_id = :userId AND status = :status";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("newName", newName);
        params.addValue("status", FriendStatusCode.PENDING.getCode());

        // 查詢對應的 receiver_id
        List<String> receiverIds = namedParameterJdbcTemplate.queryForList(selectSql, params, String.class);

        // 更新發送者和接收者的名稱
        String updateSql = "UPDATE friend_request SET " +
                "receiver_name = CASE WHEN receiver_id = :userId THEN :newName ELSE receiver_name END, " +
                "sender_name = CASE WHEN sender_id = :userId THEN :newName ELSE sender_name END " +
                "WHERE (sender_id = :userId OR receiver_id = :userId) AND status = :status";

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(updateSql, params);
            if (rowsAffected > 0) {
                log.info("Successfully updated {} records of friend request", rowsAffected);
            } else {
                log.warn("No friend request records updated for userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error occurred while updating friend request names for userId: {}", userId, e);
        }

        // 如果有符合條件的 receiver_id，則返回
        return receiverIds.isEmpty() ? Optional.empty() : Optional.of(receiverIds);
    }
}