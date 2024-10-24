//package evelyn.site.socialmedia.service;
//
//import evelyn.site.socialmedia.model.ChatMessage;
//import evelyn.site.socialmedia.model.ChatRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.security.Principal;
//
//@Service
//@Log4j2
//@RequiredArgsConstructor
//public class ChatService {
//    private final ChatMessageService chatMessageService;
//    private final SimpMessagingTemplate simpMessagingTemplate;
//
//    public void processChatRequest(ChatRequest chatRequest) {
//        // 處理聊天請求邏輯
//        simpMessagingTemplate.convertAndSend("/chat-room/public", chatRequest);
//    }
//
//    public void processChatMessage(ChatMessage chatMessage, Principal principal) {
//        String chatRoomId = chatMessage.getChatRoomId();
//        String receiverName = chatMessage.getReceiver();
//        String senderName = principal.getName();
//
//        ChatMessage responseMessage = new ChatMessage();
//        responseMessage.setContent(chatMessage.getContent());
//        responseMessage.setSender(senderName);
//        responseMessage.setReceiver(receiverName);
//        responseMessage.setChatRoomId(chatRoomId);
//
//        log.info("Received message from: " + senderName + " to: " + receiverName);
//
//        // 保存消息到 MongoDB
//        chatMessageService.saveMessage(chatMessage);
//
//        // 將消息發送到該私人聊天室
//        simpMessagingTemplate.convertAndSend("/chat-room/" + chatRoomId, responseMessage);
//    }
//}
