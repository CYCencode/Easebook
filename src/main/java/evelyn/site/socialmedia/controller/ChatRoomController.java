package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.ChatNotifyUpdateRequest;
import evelyn.site.socialmedia.dto.ChatRoomDTO;
import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.model.ChatNotify;
import evelyn.site.socialmedia.service.ChatMessageService;
import evelyn.site.socialmedia.service.ChatNotifyService;
import evelyn.site.socialmedia.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping(("/api/chat"))
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatNotifyService chatNotifyService;
    // 檢查或創建聊天室，並返回聊天室 ID
    @GetMapping("/chatroom")
    public ResponseEntity<ChatRoomDTO> checkOrCreateChatRoom(@RequestParam String user1, @RequestParam String user2) {
        String chatRoomId = chatRoomService.getCreateChatRoomId(user1, user2);
        return ResponseEntity.ok(new ChatRoomDTO(chatRoomId));
    }
    @GetMapping("/chatroom/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String chatRoomId) {
        log.info("get chat history {}", chatRoomId);
        List<ChatMessage> chatMessages = chatMessageService.getMessagesByChatRoomId(chatRoomId);
        return ResponseEntity.ok(chatMessages);
    }
    @GetMapping("/chat-request")
    public ResponseEntity<List<ChatNotify>> getChatRequests(@RequestParam("userId") String userId) {
        List<ChatNotify> requests = chatNotifyService.getChatRequests(userId);
        return ResponseEntity.ok(requests);
    }
    @PostMapping("/chat-request/")
    public ResponseEntity<Void> updateChatRequestsStatus(@RequestBody ChatNotifyUpdateRequest request) {
        chatNotifyService.updateIsReadStatus(request.getChatRoomId(), request.getReceiverId());
        return ResponseEntity.ok().build();
    }



}
