package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.ChatRoomCreateResponse;
import evelyn.site.socialmedia.dto.ChatRoomExistResponse;
import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.service.ChatMessageService;
import evelyn.site.socialmedia.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Log4j2
@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/chat/chatRoomExist")
    public ResponseEntity<ChatRoomExistResponse> checkChatRoomExist(@RequestParam String chatRoomId) {
        boolean exists = chatRoomService.chatRoomExists(chatRoomId);
        ChatRoomExistResponse response = new ChatRoomExistResponse(exists);
        log.info("chat room exist check response: {}", response);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/chat/createRoom")
    public ResponseEntity<ChatRoomCreateResponse> createGetRoom(@RequestBody Map<String, String> users) {
        String user1 = users.get("user1");
        String user2 = users.get("user2");

        String chatRoomId = chatRoomService.getCreateChatRoomId(user1, user2);
        log.info("channel build {}", chatRoomId);

        return ResponseEntity.ok(new ChatRoomCreateResponse(chatRoomId));
    }
    @GetMapping("/chat/getChatHistory")
    public ResponseEntity<?> getChatHistory(@RequestParam String chatRoomId) {
        log.info("get chat history {}", chatRoomId);
        List<ChatMessage> chatMessages = chatMessageService.getMessagesByChatRoomId(chatRoomId);
        return ResponseEntity.ok(chatMessages);
    }

}
