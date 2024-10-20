package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.AcceptorInfoDTO;
import evelyn.site.socialmedia.dto.FriendAcceptMessageDTO;
import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.dto.PostResponseDTO;
import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.service.ChatMessageService;
import evelyn.site.socialmedia.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Log4j2
@Controller
@RequiredArgsConstructor
public class NotifyController {
    private final NotifyService notifyService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 發送好友關係建立通知
    @MessageMapping("/notify/friend/accept")
    public void handleFriendAccept(FriendAcceptMessageDTO message) {
        String senderId = message.getSenderId();
        AcceptorInfoDTO acceptorInfo = message.getAcceptorInfo();
        // 發送通知給發送者
        messagingTemplate.convertAndSendToUser(senderId, "/queue/notify/friend/accept", acceptorInfo);
    }

    // 發文通知給所有好友
    @MessageMapping("/notify/post")
    public void notifyFriendsOfNewPost(@Payload PostResponseDTO postResponseDTO) {
        notifyService.notifyFriendsOfPost(postResponseDTO);
    }

    // 貼文更新
    @MessageMapping("/notify/post/update")
    public void notifyFriendsOfPostUpdate(@Payload PostResponseDTO postResponseDTO) {
        notifyService.notifyFriendsOfPostUpdate(postResponseDTO);
    }

    // 按讚更新
    @MessageMapping("/notify/thumb/update")
    public void notifyFriendsOfThumbUpdate(@Payload PostResponseDTO postResponseDTO) {
        notifyService.notifyFriendsOfThumbUpdate(postResponseDTO);
    }

    // 留言更新
    @MessageMapping("/notify/comment/update")
    public void notifyFriendsOfCommentUpdate(@Payload PostResponseDTO postResponseDTO) {
        notifyService.notifyFriendsOfCommentUpdate(postResponseDTO);
    }

    // 留言刪除
    @MessageMapping("/notify/comment/delete")
    public void notifyFriendsOfCommentDelete(@Payload PostResponseDTO postResponseDTO) {
        notifyService.notifyFriendsOfCommentDelete(postResponseDTO);
    }

    // 貼文刪除
    @MessageMapping("/notify/post/delete")
    public void notifyFriendsOfPostDelete(@Payload PostResponseDTO postResponseDTO) {
        notifyService.notifyFriendsOfPostDelete(postResponseDTO);
    }

    // 將訊息通知送給特定用戶
    @MessageMapping("/notify/message")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessageService.processChatMessage(chatMessage);
        // 通知接收者有新的訊息，並將訊息及時同步到接收者頁面
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverId(),
                "/queue/notify/message",
                chatMessage
        );
    }

    // 好友邀請
    @MessageMapping("/notify/friend")
    public void notifyFriend(@Payload FriendRequestDTO friendRequestDTO) {
        messagingTemplate.convertAndSendToUser(
                friendRequestDTO.getReceiverId(),
                "/queue/notify/friend",
                friendRequestDTO);
    }

}

