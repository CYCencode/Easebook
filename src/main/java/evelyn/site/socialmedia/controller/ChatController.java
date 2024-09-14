package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dao.ChatRoomRepository;
import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.model.ChatRequest;
import evelyn.site.socialmedia.model.ChatRoom;
import evelyn.site.socialmedia.service.ChatMessageService;
import evelyn.site.socialmedia.service.ChatRoomService;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Log4j2
@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomRepository chatRoomRepository;
    public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatMessageService chatMessageService, ChatRoomService chatRoomService, ChatRoomRepository chatRoomRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.chatMessageService = chatMessageService;
        this.chatRoomRepository = chatRoomRepository;
    }

    @MessageMapping("/chat.sendRequest")
    public void sendRequest(@Payload ChatRequest chatRequest, Principal principal) {
        //String sender = principal.getName();
        //chatRequest.setSender(sender);
        // 通知公眾頻道：某人想發起與另一人的聊天，傳遞receiver, chatRoomId資訊
        simpMessagingTemplate.convertAndSend("/chat-room/public", chatRequest);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        // 使用chatRoomId
        String chatRoomId = chatMessage.getChatRoomId();
        // 接收方
        String receiverName = chatMessage.getReceiver();
        // 發送方
        String senderName = principal.getName();

        ChatMessage responseMessage = new ChatMessage();
        responseMessage.setContent(chatMessage.getContent());
        responseMessage.setSender(senderName);
        responseMessage.setReceiver(receiverName);
        responseMessage.setChatRoomId(chatRoomId);
        log.info("Received message from: " + senderName + " to: " + receiverName);
        // 保存消息到 MongoDB
        chatMessageService.saveMessage(chatMessage);
        // 將消息發送到該私人聊天室
        simpMessagingTemplate.convertAndSend("/chat-room/" + chatRoomId, responseMessage);
    }

}





