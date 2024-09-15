package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.ChatRoom;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
@Log4j2
@Repository
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 查詢是否存在聊天室
    @Override
    public Optional<ChatRoom> findByChatRoomId(String chatRoomId) {
        log.info("finding chatRoomId {}",chatRoomId);
        String sql = "SELECT * FROM chat_rooms WHERE chat_room_id = ?";
        try {
            ChatRoom chatRoom = jdbcTemplate.queryForObject(sql, new Object[]{chatRoomId}, new ChatRoomRowMapper());
            log.info("found chatRoom {}", chatRoom);
            return Optional.ofNullable(chatRoom);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // 保存新的聊天室
    @Override
    public void save(ChatRoom chatRoom) {
        log.info("chatRoom {}",chatRoom);
        String sql = "INSERT INTO chat_rooms (chat_room_id, user_1, user_2) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, chatRoom.getChatRoomId(), chatRoom.getUser1(), chatRoom.getUser2());
    }

    // RowMapper 用來將結果集轉換成 ChatRoom 實體
    private static class ChatRoomRowMapper implements RowMapper<ChatRoom> {
        @Override
        public ChatRoom mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setChatRoomId(rs.getString("chat_room_id"));
            chatRoom.setUser1(rs.getString("user_1"));
            chatRoom.setUser2(rs.getString("user_2"));
            return chatRoom;
        }
    }
}
