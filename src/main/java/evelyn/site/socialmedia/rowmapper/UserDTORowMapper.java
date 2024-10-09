package evelyn.site.socialmedia.rowmapper;

import evelyn.site.socialmedia.dto.UserDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDTORowMapper implements RowMapper<UserDTO> {
    @Override
    public UserDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserDTO.builder()
                .id(rs.getString("id"))
                .name(rs.getString("name"))
                .status(rs.getInt("status"))
                .friendRequestId(rs.getString("friend_request_id"))
                .build();
    }
}

