package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.dto.FriendDTO;
import evelyn.site.socialmedia.rowmapper.FriendDTORowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Log4j2
@Repository
@RequiredArgsConstructor
public class FriendRepositoryImpl implements  FriendRepository{
    private final NamedParameterJdbcTemplate template;
    @Override
    public List<FriendDTO> getFriendByUserId (String userId){
        log.info("getFriendByUserId: {}",userId);
        String sql = """
                SELECT u.name AS friend_name,
                CASE
                         WHEN f.user_1 =:userId THEN f.user_2
                         ELSE f.user_1
                       END AS friend_id
                FROM friendship f
                JOIN users u
                ON u.id = CASE
                WHEN f.user_1 =:userId THEN f.user_2
                ELSE f.user_1
                END
                WHERE (f.user_1 = :userId OR f.user_2 = :userId);
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        return template.query(sql, params, new FriendDTORowMapper());
    }

}
