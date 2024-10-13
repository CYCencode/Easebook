package evelyn.site.socialmedia.service;

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
        // 3. 更新快取 -> rightPush 把新增的那筆資料推入尾端 ＆ 截斷
        String cacheKey = "chatRoom:" + message.getChatRoomId();
        cacheUtil.addMessageToCache(cacheKey, message, messageLimit);
    }

    @Override
    public List<ChatMessage> getMessagesByChatRoomId(String chatRoomId) {
        // 優先從快取中獲取
        String cacheKey = "chatRoom:" + chatRoomId;
        List<ChatMessage> cacheMessages = cacheUtil.getMessagesFromCache(cacheKey);
        log.info("cacheUtil.getCache Messages : {}", cacheMessages);
        if (cacheMessages != null && !cacheMessages.isEmpty()) {
            log.info("cache hit for room : {}", chatRoomId);
            return cacheMessages;
        }

        // 快取中沒有，則從資料庫中查詢前 MESSAGE_LIMIT 筆資料
        Pageable pageable = PageRequest.of(0, messageLimit);
        // 依據timestamp 由新到舊(時間最大者在前)、取前 MESSAGE_LIMIT 筆資料
        List<ChatMessage> chatMessages = chatMessageRepository
                .findByChatRoomIdOrderByCreateAtDesc(chatRoomId, pageable);
        log.info("chatMessages from db : {}", chatMessages);
        // mongoDB 若查不到資料會返回空array
        if (!chatMessages.isEmpty()) {
            // 展示資料時，由舊到新排序，以符合聊天室對話邏輯
            chatMessages.sort(Comparator.comparing(ChatMessage::getCreateAt));
            log.info("use db : {}", chatMessages);
            // 將資料庫中查到的資料順序的存入 Redis 快取（由舊到新排序rightPush）
            for (ChatMessage message : chatMessages) {
                cacheUtil.addMessageToCache(cacheKey, message, messageLimit);
            }
        }

        return chatMessages;
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




