package evelyn.site.socialmedia.rowmapper;

import evelyn.site.socialmedia.model.ChatRoom;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

// RowMapper 用來將結果集轉換成 ChatRoom 實體
public class ChatRoomRowMapper implements RowMapper<ChatRoom> {
    @Override
    public ChatRoom mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(rs.getString("chat_room_id"));
        chatRoom.setUser1(rs.getString("user_1"));
        chatRoom.setUser2(rs.getString("user_2"));
        chatRoom.setCreateAt(rs.getTimestamp("create_at").toInstant());
        return chatRoom;
    }
}

