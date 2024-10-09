package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.*;
import evelyn.site.socialmedia.enums.InitCounts;
import evelyn.site.socialmedia.model.Comment;
import evelyn.site.socialmedia.model.Post;
import evelyn.site.socialmedia.repository.FriendRepository;
import evelyn.site.socialmedia.repository.PostRepository;
import evelyn.site.socialmedia.repository.UserPostRepository;
import evelyn.site.socialmedia.util.UUIDGenerator;
import evelyn.site.socialmedia.util.UploadS3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserPostRepository userPostRepository;
    private final FriendRepository friendRepository;
    private final S3Service s3Service;
    private final UploadS3Util uploadS3Util;

    @Override
    public PostResponseDTO createPost(PostRequestDTO postRequestDTO) throws IOException {
        Post post = new Post();
        post.setPostId(UUIDGenerator.generateUUID());
        post.setUserId(postRequestDTO.getUserId());
        post.setUserName(postRequestDTO.getUserName());
        post.setContent(postRequestDTO.getContent());
        post.setUserPhoto(postRequestDTO.getUserPhoto());
        post.setThumbUsers(new HashSet<>());
        post.setThumbsCount(InitCounts.ZERO.getCode());
        post.setReplyCount(InitCounts.ZERO.getCode());
        post.setCreateAt(postRequestDTO.getCreateAt());
        post.setComments(new ArrayList<>());

        // 上傳圖片並取得 S3 URL
        List<String> imageNames = uploadS3Util.uploadFiles(postRequestDTO.getImages());
        List<String> imageUrls = imageNames.stream()
                .map(s3Service::getFileUrl)
                .collect(Collectors.toList());
        post.setImages(imageUrls);
        log.info("create new post, image url: {}", imageUrls);
        // 上傳影片並取得 S3 URL
        List<String> videoNames = uploadS3Util.uploadFiles(postRequestDTO.getVideos());
        List<String> videoUrls = videoNames.stream()
                .map(s3Service::getFileUrl)
                .collect(Collectors.toList());
        post.setVideos(videoUrls);


        Post savedPost = postRepository.save(post);
        userPostRepository.save(post);
        return convertToResponseDTO(savedPost);
    }

    @Override
    public PostResponseDTO updatePost(String postId, UpdatePostRequestDTO updatePostRequestDTO) {
        try {
            Optional<Post> optionalPost = postRepository.findByPostId(postId);
            // 使用 ifPresentOrElse 進行處理
            optionalPost.ifPresentOrElse(existingPost -> {
                existingPost.setContent(updatePostRequestDTO.getContent());
                // 保留用戶保留的舊圖片和影片
                List<String> finalImages = new ArrayList<>(updatePostRequestDTO.getExistingImages());
                List<String> finalVideos = new ArrayList<>(updatePostRequestDTO.getExistingVideos());
                log.info("existing images: " + finalImages);
                log.info("existing videos: " + finalVideos);
                // 上傳新圖片並取得 URL
                if (updatePostRequestDTO.getNewImages() != null && !updatePostRequestDTO.getNewImages().isEmpty()) {
                    try {
                        List<String> uploadedImageNames = uploadS3Util.uploadFiles(updatePostRequestDTO.getNewImages());
                        // 逐一轉換為 S3 URL 並加入 finalImages 列表
                        for (String imageName : uploadedImageNames) {
                            String imageUrl = s3Service.getFileUrl(imageName);
                            finalImages.add(imageUrl);  // 加入 S3 URL
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload images", e);
                    }
                }
                // 上傳新影片並取得 URL
                if (updatePostRequestDTO.getNewVideos() != null && !updatePostRequestDTO.getNewVideos().isEmpty()) {
                    try {
                        List<String> uploadedVideoNames = uploadS3Util.uploadFiles(updatePostRequestDTO.getNewVideos());
                        // 逐一轉換為 S3 URL 並加入 finalVideos 列表
                        for (String videoName : uploadedVideoNames) {
                            String videoUrl = s3Service.getFileUrl(videoName);
                            finalVideos.add(videoUrl);  // 加入 S3 URL
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload videos", e);
                    }
                }
                // 更新貼文的圖片和影片清單
                existingPost.setImages(finalImages);
                existingPost.setVideos(finalVideos);
                // 儲存更新後的貼文
                postRepository.save(existingPost);
                log.info("finish put request,  post save as : " + existingPost);
            }, () -> {
                // 處理找不到貼文的情況
                throw new RuntimeException("Post not found");
            });
            log.info("Post updated, optionalPost: {}", optionalPost.get());
            // 返回更新後的貼文資訊
            return convertToResponseDTO(optionalPost.get());
        } catch (RuntimeException e) {
            log.error("Failed to update post", e);
            throw e;
        }
    }


    @Override
    public PostResponseDTO getPost(String postId, String userId) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToResponseDTO(post, userId);
    }
   /*
   分頁加載貼文 ：
   else : 初始化，載入所有貼文中最新的 limit 篇文章
   findByCreateAtBeforeAndUserIdIn : 依據最後一篇文章的時間戳，往前推所有的文章
   findByUserIdIn ： 根據postId 找貼文
    */

    @Override
    public Map<String, Object> getPosts(String userId, String page, String limit) {
        // 1. 取得該用戶的好友列表
        List<FriendDTO> friends = friendRepository.getFriendByUserId(userId);
        List<String> friendIds = friends.stream()
                .map(FriendDTO::getFriendId)
                .collect(Collectors.toList());

        // 自己的貼文也需要顯示
        friendIds.add(userId);
        // 2. 準備分頁資訊，注意 page 是從 0 開始計數的
        int pageNumber = Integer.parseInt(page);
        int pageSize = Integer.parseInt(limit) + 1; //多取一筆判斷是否還有更多資料
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));

        // 3. 查詢符合條件的貼文
        Page<Post> postPage = postRepository.findByUserIdInAndStatus(friendIds, "ACTIVE", pageable);

        // 4. 轉換為 PostResponseDTO，paging 並根據 userId 判斷 liked 屬性
        List<PostResponseDTO> posts = postPage.getContent().stream()
                .map(post -> convertToResponseDTO(post, userId))
                .collect(Collectors.toList());

        boolean hasMore = posts.size() == pageSize;
        if (hasMore) {
            posts.remove(posts.size() - 1);
        }

        // 4. 轉換並返回 DTO
        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("hasMore", hasMore);
        return result;

    }

    @Override
    public Map<String, Object> getPostByUserId(String userId, String page, String limit) {
        // 準備分頁資訊
        int pageNumber = Integer.parseInt(page);
        int pageSize = Integer.parseInt(limit) + 1; //多取一筆判斷是否還有更多資料
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));

        Page<Post> postPage = postRepository.findByUserIdAndStatus(userId, "ACTIVE", pageable);

        // 轉換為 PostResponseDTO，paging 並根據 userId 判斷 liked 屬性
        List<PostResponseDTO> posts = postPage.getContent().stream()
                .map(post -> convertToResponseDTO(post, userId))
                .collect(Collectors.toList());

        boolean hasMore = posts.size() == pageSize;
        if (hasMore) {
            posts.remove(posts.size() - 1);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("hasMore", hasMore);
        return result;
    }


    @Override
    public void deletePost(String postId) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 設置 status 為 "DELETED" 而非直接刪除
        post.setStatus("DELETED");
        postRepository.save(post);
    }

    private PostResponseDTO convertToResponseDTO(Post post) {
        PostResponseDTO responseDTO = new PostResponseDTO();
        responseDTO.setPostId(post.getPostId());
        responseDTO.setUserId(post.getUserId());
        responseDTO.setUserName(post.getUserName());
        responseDTO.setUserPhoto(post.getUserPhoto());
        responseDTO.setContent(post.getContent());
        responseDTO.setImages(post.getImages());
        responseDTO.setVideos(post.getVideos());
        responseDTO.setThumbUsers(post.getThumbUsers());
        responseDTO.setThumbsCount(post.getThumbsCount());
        responseDTO.setReplyCount(post.getReplyCount());
        responseDTO.setCreateAt(post.getCreateAt());

        if (post.getComments() != null && !post.getComments().isEmpty()) {
            responseDTO.setComments(new ArrayList<>(post.getComments())); // 將 List<Comment> 傳給 DTO
        } else {
            responseDTO.setComments(new ArrayList<>()); // 如果 comments 為 null 或空，回傳空 list
        }
        return responseDTO;
    }

    // for get method , add liked statement
    private PostResponseDTO convertToResponseDTO(Post post, String currentUserId) {
        PostResponseDTO responseDTO = new PostResponseDTO();
        responseDTO.setPostId(post.getPostId());
        responseDTO.setUserId(post.getUserId());
        responseDTO.setUserName(post.getUserName());
        responseDTO.setUserPhoto(post.getUserPhoto());
        responseDTO.setContent(post.getContent());
        responseDTO.setImages(post.getImages());
        responseDTO.setVideos(post.getVideos());
        responseDTO.setThumbUsers(post.getThumbUsers());
        responseDTO.setThumbsCount(post.getThumbsCount());
        responseDTO.setReplyCount(post.getReplyCount());
        responseDTO.setCreateAt(post.getCreateAt());
        // 檢查當前使用者是否已經按讚
        boolean liked = post.getThumbUsers().stream()
                .anyMatch(thumb -> thumb.getUserId().equals(currentUserId));
        responseDTO.setLiked(liked); // 添加 liked 屬性

        if (post.getComments() != null && !post.getComments().isEmpty()) {
            responseDTO.setComments(new ArrayList<>(post.getComments()));
        } else {
            responseDTO.setComments(new ArrayList<>());
        }
        return responseDTO;
    }

    @Override
    public PostResponseDTO toggleThumb(String postId, ThumbRequestDTO thumbRequestDTO) {
        String userId = thumbRequestDTO.getUserId();
        String userName = thumbRequestDTO.getUserName();
        String avatarUrl = thumbRequestDTO.getAvatarUrl();
        log.info("toggle thumb postId: " + postId + " userId: " + userId + " avatarUrl: " + avatarUrl);
        Optional<Post> postOpt = postRepository.findByPostId(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            boolean alreadyLiked = post.getThumbUsers().stream()
                    .anyMatch(thumb -> thumb.getUserId().equals(userId));
            if (alreadyLiked) {
                // 如果用戶已經按讚，則取消按讚
                postRepository.removeThumb(postId, userId);
            } else {
                // 如果用戶尚未按讚，則添加按讚
                postRepository.addThumb(postId, userId, userName, avatarUrl);
            }
            // 查詢並返回更新後的貼文
            return convertToResponseDTO(postRepository.findByPostId(postId).orElseThrow(() -> new RuntimeException("Post not found after update")));

        } else {
            throw new RuntimeException("Post not found");
        }
    }

    @Override
    public List<ThumbUserDTO> getThumbUser(String postId) {

        // 從 PostRepository 查找貼文
        Optional<Post> postOpt = postRepository.findByPostId(postId);

        if (postOpt.isPresent()) {
            // 獲取該貼文中的 thumbUsers 並返回
            Post post = postOpt.get();
            log.info("getThumbUser : {}", post);
            return post.getThumbUsers().stream()
                    .map(thumbUser -> ThumbUserDTO.builder()
                            .userId(thumbUser.getUserId())
                            .userName(thumbUser.getUserName())
                            .avatarUrl(thumbUser.getAvatarUrl())
                            .build())
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Post not found");
        }
    }

    @Override
    public PostResponseDTO addComment(String postId, Comment comment) {
        // 根據 postId 查找貼文
        Optional<Post> postOpt = postRepository.findByPostId(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            // 獲取現有的 comments 列表，若不存在則初始化
            List<Comment> comments = post.getComments();
            if (comments == null) {
                comments = new ArrayList<>();
            }
            // 設定 comment id
            comment.setId(UUIDGenerator.generateUUID());
            // 添加新的留言到 comments 列表中
            comments.add(comment);
            post.setComments(comments);
            // 更新留言數量
            post.setReplyCount(post.getReplyCount() + 1);
            // 保存更新後的貼文
            Post updatedPost = postRepository.save(post);
            // 返回更新後的 PostResponseDTO
            return convertToResponseDTO(updatedPost);

        } else {
            throw new RuntimeException("Post not found");
        }
    }

    @Override
    public PostResponseDTO editComment(String postId, Comment comment) {
        Optional<Post> postOpt = postRepository.findByPostId(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            List<Comment> comments = post.getComments();

            if (comments != null) {
                for (Comment existingComment : comments) {
                    if (existingComment.getId().equals(comment.getId())) {
                        // 更新評論內容
                        existingComment.setContent(comment.getContent());
                        // 保存更新後的貼文
                        postRepository.save(post);
                        return convertToResponseDTO(post);
                    }
                }
            }
            throw new RuntimeException("Comment not found");
        } else {
            throw new RuntimeException("Post not found");
        }
    }

    @Override
    public PostResponseDTO deleteComment(String postId, String commentId) {
        Optional<Post> postOpt = postRepository.findByPostId(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            List<Comment> comments = post.getComments();

            if (comments != null) {
                boolean removed = comments.removeIf(existingComment -> existingComment.getId().equals(commentId));
                if (removed) {
                    post.setReplyCount(post.getReplyCount() - 1);
                    postRepository.save(post);
                    return convertToResponseDTO(post);
                }
            }
            throw new RuntimeException("Comment not found");
        } else {
            throw new RuntimeException("Post not found");
        }
    }

}