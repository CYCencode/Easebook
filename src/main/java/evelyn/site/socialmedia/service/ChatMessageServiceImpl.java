package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.ChatMessageDTO;
import evelyn.site.socialmedia.model.ChatMessage;
import evelyn.site.socialmedia.repository.ChatMessageRepository;
import evelyn.site.socialmedia.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatMessageServiceImpl implements ChatMessageService {

    @Value("${chat.message.limit:30}")
    private int messageLimit;
    private final CacheUtil cacheUtil;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatNotifyService chatNotifyService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Transactional
    public void saveMessage(ChatMessage message) {
        // 1. 資料庫儲存歷史聊天記錄
        chatMessageRepository.save(message);
        // 2. 創建訊息通知紀錄或是更新content
        chatNotifyService.upsertChatNotify(message);
        // 3. 更新快取
        String cacheKey = "chatRoom:" + message.getChatRoomId();
        cacheUtil.addMessageToCache(cacheKey, message, messageLimit);
    }

    @Override
    public ChatMessageDTO getPagingMessagesByChatRoomId(String chatRoomId, Instant lastCreateAt) {
        int limit = messageLimit;
        // 取得 limit + 1 筆訊息
        List<ChatMessage> messages = getMessagesByChatRoomId(chatRoomId, lastCreateAt, limit + 1);
        // 判斷是否有上一頁
        boolean hasLastPage = messages.size() > limit;
        if (hasLastPage) {
            // 截斷，只取前 limit 筆
            messages = messages.subList(messages.size() - limit, messages.size());
        }
        return new ChatMessageDTO(hasLastPage, messages);
    }

    @Override
    public List<ChatMessage> getMessagesByChatRoomId(String chatRoomId, Instant lastCreateAt, int limit) {
        if (lastCreateAt == null) {
            // 第一頁，嘗試從快取中獲取
            String cacheKey = "chatRoom:" + chatRoomId;
            List<ChatMessage> cacheMessages = cacheUtil.getMessagesFromCache(cacheKey);
            if (cacheMessages != null && !cacheMessages.isEmpty()) {
                log.info("cache hit for room : {}", chatRoomId);
                return cacheMessages;
            }
            // 快取沒有，從資料庫獲取並存入快取
            Pageable pageable = PageRequest.of(0, limit + 1);
            List<ChatMessage> chatMessages = chatMessageRepository
                    .findByChatRoomIdOrderByCreateAtDesc(chatRoomId, pageable);
            if (!chatMessages.isEmpty()) {
                // 由舊到新排序，以符合聊天室的顯示邏輯
                chatMessages.sort(Comparator.comparing(ChatMessage::getCreateAt));
                // 存入快取
                cacheUtil.addMessagesToCache(cacheKey, chatMessages, messageLimit);
            }
            return chatMessages;
        } else {
            // 如果不是第一頁，是查詢更舊的資料，從資料庫獲取
            Pageable pageable = PageRequest.of(0, limit + 1);
            List<ChatMessage> chatMessages = chatMessageRepository
                    .findByChatRoomIdAndCreateAtBeforeOrderByCreateAtDesc(chatRoomId, lastCreateAt, pageable);
            if (!chatMessages.isEmpty()) {
                // 由舊到新排序
                chatMessages.sort(Comparator.comparing(ChatMessage::getCreateAt));
            }
            return chatMessages;
        }
    }

    @Override
    public void processChatMessage(ChatMessage chatMessage) {
        String chatRoomId = chatMessage.getChatRoomId();
        // 保存消息到 MongoDB
        saveMessage(chatMessage);
        // 將消息發送到該私人聊天室
        simpMessagingTemplate.convertAndSend("/chat/private/" + chatRoomId, chatMessage);
    }
}