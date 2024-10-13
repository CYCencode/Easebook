package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.model.ChatNotify;
import evelyn.site.socialmedia.repository.ChatNotifyRepository;
import evelyn.site.socialmedia.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatNotifyServiceImpl implements ChatNotifyService {
    private final ChatNotifyRepository chatNotifyRepository;

    @Override
    public void upsertChatNotify(ChatMessage chatMessage) {
        // 1. 查詢是否已存在符合特定聊天室、收訊對象的紀錄
        Optional<ChatNotify> existingNotify = chatNotifyRepository.findByChatRoomIdAndReceiverId(
                chatMessage.getChatRoomId(), chatMessage.getReceiverId());

        // 2. 如果存在，進行更新
        if (existingNotify.isPresent()) {
            log.info("Chat notify already exists: " + chatMessage.getChatRoomId());
            // 取得整筆紀錄，因為包含 _id，save時會執行更新而不是新增一筆紀錄
            ChatNotify notify = existingNotify.get();
            notify.setSenderName(chatMessage.getSenderName());
            notify.setContent(chatMessage.getContent());
            notify.setCreateAt(chatMessage.getCreateAt());
            // 重置已讀紀錄
            notify.setSeen(false);
            chatNotifyRepository.save(notify);
        } else {
            // 3. 如果不存在，保存新紀錄
            log.info("Chat notify not found: " + chatMessage.getChatRoomId());
            ChatNotify newNotify = new ChatNotify();
            newNotify.setId(UUIDGenerator.generateUUID()); // 使用隨機UUID作為id
            newNotify.setChatRoomId(chatMessage.getChatRoomId());
            newNotify.setSenderName(chatMessage.getSenderName());
            newNotify.setSenderId(chatMessage.getSenderId());
            newNotify.setReceiverId(chatMessage.getReceiverId());
            newNotify.setContent(chatMessage.getContent());
            newNotify.setCreateAt(chatMessage.getCreateAt());
            chatNotifyRepository.save(newNotify); // 保存新紀錄
        }

    }

    @Override
    public List<ChatNotify> getChatRequests(String userId) {
        return chatNotifyRepository.findBySeenFalseAndReceiverIdOrderByCreateAtDesc(userId);
    }

    @Override
    public void updateIsReadStatus(String chatRoomId, String receiverId) {
        Optional<ChatNotify> notifyOptional = chatNotifyRepository.findByChatRoomIdAndReceiverId(chatRoomId, receiverId);
        if (notifyOptional.isPresent()) {
            ChatNotify notify = notifyOptional.get();
            notify.setSeen(true);  // 更新為已讀
            chatNotifyRepository.save(notify);  // 保存到 MongoDB
        }
    }
}

