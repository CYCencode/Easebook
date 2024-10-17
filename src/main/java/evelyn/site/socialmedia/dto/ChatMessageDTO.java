package evelyn.site.socialmedia.dto;

import evelyn.site.socialmedia.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private boolean hasLastPage;
    private List<ChatMessage> chatMessages;
}

