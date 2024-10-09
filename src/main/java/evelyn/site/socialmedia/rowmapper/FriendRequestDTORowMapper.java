package evelyn.site.socialmedia.rowmapper;

import evelyn.site.socialmedia.dto.FriendRequestDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendRequestDTORowMapper implements RowMapper<FriendRequestDTO> {
    @Override
    public FriendRequestDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return  FriendRequestDTO.builder()
                .id(rs.getString("id"))
                .senderId(rs.getString("sender_id"))
                .senderName(rs.getString("sender_name"))
                .receiverId(rs.getString("receiver_id"))
                .receiverName(rs.getString("receiver_name"))
                .status(rs.getInt("status"))
                .createAt(rs.getTimestamp("create_at").toInstant())
                .build();
    }
}

