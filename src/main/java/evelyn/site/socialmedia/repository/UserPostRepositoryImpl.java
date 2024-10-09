package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RequiredArgsConstructor
public class UserPostRepositoryImpl implements UserPostRepository {
    private final JdbcTemplate jdbcTemplate;
    @Override
    public void save(Post post){
        String sql = "INSERT INTO user_post (user_id, post_id, create_at) VALUES(?,?,?)";
        jdbcTemplate.update(sql, post.getUserId(), post.getPostId(), post.getCreateAt());
    }

}
