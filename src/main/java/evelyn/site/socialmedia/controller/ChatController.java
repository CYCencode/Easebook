package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage chatMessage, Principal principal) {

        String senderName = principal.getName(); // 發送者的用戶名
        String receiverName = chatMessage.getReceiver(); // 接收者的用戶名

        ChatMessage responseMessage = new ChatMessage();
        responseMessage.setContent(chatMessage.getContent()); // 設置消息內容
        responseMessage.setSender(senderName); // 設置發送者名稱
        responseMessage.setReceiver(receiverName); // 設置接收者名稱
        System.out.println("Received message from: " + senderName + " to: " + receiverName);

        // 將消息發送到接收者的專屬通道
        simpMessagingTemplate.convertAndSendToUser(receiverName, "/queue/reply", responseMessage);
    }
}





