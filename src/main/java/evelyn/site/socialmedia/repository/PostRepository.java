package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    // 使用 Pageable 進行分頁查詢
    Page<Post> findByUserIdInAndStatus(List<String> userIds, int status, Pageable pageable);

    Page<Post> findByUserIdAndStatus(String userId, int status, Pageable pageable);

    List<Post> findByUserIdAndStatus(String userId, int status);

    @Query("{ 'postId': ?0, 'status': 0 }")
    Optional<Post> findByPostId(String postId);

    @Query("{ 'userId': { $in: ?0 }, 'status': 0 }")
    List<Post> findByUserIdIn(List<String> userIds, Sort sort);


    @Query("{ 'postId': ?0, 'thumbUsers.userId': { $ne: ?1 } , 'status': 0 }")
    @Update("{ $addToSet: { 'thumbUsers': { 'userId': ?1, 'userName': ?2,'avatarUrl': ?3 } }, $inc: { 'thumbsCount': 1 } }")
    void addThumb(String postId, String userId, String userName, String avatarUrl);

    @Query("{ 'postId': ?0 , 'status': 0 }")
    @Update("{ $pull: { 'thumbUsers': { 'userId': ?1 } }, $inc: { 'thumbsCount': -1 } }")
    void removeThumb(String postId, String userId);

    // 查詢包含特定留言者 userId 的所有貼文
    @Query("{ 'comments.userId': ?0, 'status': 0 }")
    List<Post> findPostsByCommenterId(String commenterId);

    // 查詢包含特定按讚者 userId 的所有貼文
    @Query("{ 'thumbUsers.userId': ?0, 'status': 0 }")
    List<Post> findPostsByThumberId(String userId);
}
