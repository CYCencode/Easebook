package evelyn.site.socialmedia.repository;

import evelyn.site.socialmedia.model.Post;

public interface UserPostRepository {
    void save(Post post);
}
