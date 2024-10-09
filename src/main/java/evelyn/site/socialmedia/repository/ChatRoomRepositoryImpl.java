package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.rowmapper.ChatRoomRowMapper;
import evelyn.site.socialmedia.model.ChatRoom;
import evelyn.site.socialmedia.util.DbUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
@Log4j2
@Repository
@Transactional
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {
    private final JdbcTemplate jdbcTemplate;

    // 查詢是否存在聊天室
    @Override
    public Optional<ChatRoom> findByChatRoomId(String chatRoomId) {
        log.info("finding chatRoomId {}",chatRoomId);
        String sql = "SELECT * FROM chat_room WHERE chat_room_id = ?";
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
        String sql = "INSERT INTO chat_room (chat_room_id, user_1, user_2,create_at) VALUES (?, ?, ?, ?)";
        log.info("chatRoom size : {}", chatRoom.getChatRoomId().length());
        int rowsAffected = jdbcTemplate.update(sql, chatRoom.getChatRoomId(), chatRoom.getUser1(), chatRoom.getUser2(), chatRoom.getCreateAt());
        // 檢查是否成功插入資料
        DbUtil.checkRowAffected(rowsAffected,
                "Successfully inserted ChatRoom with ID: " + chatRoom.getChatRoomId(),
                "Failed to insert ChatRoom");
    }
}
