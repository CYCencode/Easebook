package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.*;
import evelyn.site.socialmedia.model.Comment;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PostService {
    void validatePost(PostRequestDTO postRequestDTO);

    void validateUpdatePost(UpdatePostRequestDTO updatePostRequestDTO);

    PostResponseDTO createPost(PostRequestDTO postRequestDTO) throws IOException;

    PostResponseDTO updatePost(String postId, UpdatePostRequestDTO updatePostRequestDTO) throws IOException;

    PostResponseDTO getPost(String postId, String userId);

    Map<String, Object> getPosts(String userId, String page, String limit);

    Map<String, Object> getPostByUserId(String userId, String currentUserId, String page, String limit);

    PostResponseDTO toggleThumb(String postId, ThumbRequestDTO thumbRequestDTO);

    PostResponseDTO addComment(String postId, Comment comment);

    PostResponseDTO editComment(String postId, Comment comment);

    PostResponseDTO deleteComment(String postId, String commentId);

    List<ThumbUserDTO> getThumbUser(String postId);

    void deletePost(String postId);
}

