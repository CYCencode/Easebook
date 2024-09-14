package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dao.ChatRoomRepository;
import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.service.ChatMessageService;
import evelyn.site.socialmedia.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Log4j2
@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/chat/chatRoomExist")
    public ResponseEntity<Map<String, Boolean>> checkChatRoomExist(@RequestParam String chatRoomId) {
        boolean exists = chatRoomService.chatRoomExists(chatRoomId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        log.info("chat room exist check response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/createRoom")
    public ResponseEntity<Map<String, String>> createGetRoom(@RequestBody Map<String, String> users) {
        String user1 = users.get("user1");
        String user2 = users.get("user2");

        String chatRoomId = chatRoomService.getCreateChatRoomId(user1, user2);
        log.info("channel build {}", chatRoomId);
        Map<String, String> response = new HashMap<>();
        response.put("chatRoomId", chatRoomId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/chat/getChatHistory")
    public ResponseEntity<?> getChatHistory(@RequestParam String chatRoomId) {
        log.info("get chat history {}", chatRoomId);
        List<ChatMessage> chatMessages = chatMessageService.getMessagesByChatRoomId(chatRoomId);
        return ResponseEntity.ok(chatMessages);
    }
}
