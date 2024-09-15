package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.model.ChatRequest;

import evelyn.site.socialmedia.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat.sendRequest")
    public void sendRequest(@Payload ChatRequest chatRequest) {
        chatService.processChatRequest(chatRequest);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        chatService.processChatMessage(chatMessage,principal);
    }

}





