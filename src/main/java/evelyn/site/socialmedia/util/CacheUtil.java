package evelyn.site.socialmedia.util;

import evelyn.site.socialmedia.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    public <V> void addMessageToCache(String key, V value, int messageLimit) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            // 截斷，保留最新的 messageLimit + 1 筆訊息
            redisTemplate.opsForList().trim(key, -(messageLimit + 1), -1);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed while updating cache: {}", key, e);
            // 快取更新失敗，將快取清除以維持資料一致性
            delete(key);
        }
    }

    public <V> void addMessagesToCache(String key, List<V> messages, int messageLimit) {
        try {
            redisTemplate.opsForList().rightPushAll(key, messages.toArray(new ChatMessage[0]));
            // 截斷，保留最新的 messageLimit + 1 筆訊息
            redisTemplate.opsForList().trim(key, -(messageLimit + 1), -1);
            List<Object> cachedMessages = redisTemplate.opsForList().range(key, 0, -1);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed while adding messages to cache: {}", key, e);
            delete(key);
        }
    }

    public <V> List<V> getMessagesFromCache(String key) {
        try {
            log.info("Fetching messages from cache key:{}", key);
            return (List<V>) redisTemplate.opsForList().range(key, 0, -1); // 取得全部訊息
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed while fetching messages from cache: {}", key, e);
            return null;
        }
    }

    public boolean delete(String key) {
        try {
            log.info("delete key:{}", key);
            return redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed while deleting key: {}", key, e);
            return false;
        }
    }
}