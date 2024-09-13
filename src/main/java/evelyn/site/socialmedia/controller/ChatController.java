package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.model.ChatMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
@Log4j2
@Controller
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
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

        // 將消息發送到接收者的專屬通道
        simpMessagingTemplate.convertAndSendToUser(receiverName, "/queue/reply", responseMessage);
        // 訊息也會更新到發送方
        simpMessagingTemplate.convertAndSendToUser(senderName, "/queue/reply", responseMessage);
        //simpMessagingTemplate.convertAndSend("/chat-room/"+chatRoomId, responseMessage);
    }

}





