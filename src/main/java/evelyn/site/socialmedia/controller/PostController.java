package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.*;
import evelyn.site.socialmedia.model.Comment;
import evelyn.site.socialmedia.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@ModelAttribute PostRequestDTO postRequestDTO) {
        try {
            // 1. 檢查上傳的檔案大小、貼文文字長度是否符合
            postService.validatePost(postRequestDTO);
            log.info("get PostRequestDTO {}", postRequestDTO);
            // 2. 把貼文存進資料庫
            PostResponseDTO createdPost = postService.createPost(postRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("資料上傳失敗，請稍後再試");
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable String postId,
                                                   @RequestParam String userId) {
        PostResponseDTO post = postService.getPost(postId, userId);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPostByUserId(@PathVariable String userId,
                                                               @RequestParam String currentUserId,
                                                               @RequestParam String page,
                                                               @RequestParam String limit) {
        Map<String, Object> posts = postService.getPostByUserId(userId, currentUserId, page, limit);
        return ResponseEntity.ok(posts);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPosts(
            @RequestParam String userId,
            @RequestParam String page,
            @RequestParam String limit) {
        Map<String, Object> posts = postService.getPosts(userId, page, limit);
        return ResponseEntity.ok(posts);
    }


    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable String postId,
            @ModelAttribute UpdatePostRequestDTO updatePostRequestDTO) {
        try {
            // 1. check if content, file are valid
            postService.validateUpdatePost(updatePostRequestDTO);
            // 2. update post since content is valid
            PostResponseDTO updatedPost = postService.updatePost(postId, updatePostRequestDTO);
            log.info("put PostRequestDTO {}", updatePostRequestDTO);
            log.info("put request done, response updatedPost: {}", updatedPost);
            return ResponseEntity.ok(updatedPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("貼文更新失敗，請稍後再試");
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build(); // return 204 No Content
    }

    @PostMapping("/{postId}/thumb")
    public ResponseEntity<PostResponseDTO> thumbPost(@PathVariable String postId, @RequestBody ThumbRequestDTO thumbRequestDTO) {
        PostResponseDTO updatedPost = postService.toggleThumb(postId, thumbRequestDTO);
        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/{postId}/thumb")
    public ResponseEntity<List<ThumbUserDTO>> getThumbUser(@PathVariable String postId) {
        List<ThumbUserDTO> users = postService.getThumbUser(postId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<PostResponseDTO> commentPost(@PathVariable String postId, @RequestBody Comment comment) {
        PostResponseDTO updatedPost = postService.addComment(postId, comment);
        return ResponseEntity.ok(updatedPost);
    }

    @PutMapping("/{postId}/comments")
    public ResponseEntity<PostResponseDTO> editComment(@PathVariable String postId, @RequestBody Comment comment) {
        PostResponseDTO updatedPost = postService.editComment(postId, comment);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<PostResponseDTO> deleteComment(@PathVariable String postId, @PathVariable String commentId) {
        PostResponseDTO updatePost = postService.deleteComment(postId, commentId);
        return ResponseEntity.ok(updatePost);
    }
}
