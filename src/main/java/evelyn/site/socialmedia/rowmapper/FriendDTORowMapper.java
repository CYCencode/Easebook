package evelyn.site.socialmedia.rowmapper;

import evelyn.site.socialmedia.dto.FriendDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendDTORowMapper implements RowMapper<FriendDTO> {
    @Override
    public FriendDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FriendDTO.builder()
                .friendId(rs.getString("friend_id"))
                .friendName(rs.getString("friend_name"))
                .build();
    }
}
