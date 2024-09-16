package evelyn.site.socialmedia.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    public <V> void addMessageToCache(String key, V value, int messageLimit) {
        try{
            redisTemplate.opsForList().rightPush(key, value);
            redisTemplate.opsForList().trim(key, 0, messageLimit-1);
            log.info("Update cache for key:{} with value:{}", key, value);
        }catch (RedisConnectionFailureException e){
            log.error("Redis connection failed while updating cache: {}",key, e);
            // 快取更新失敗，將快取清除以維持資料一致性
            delete(key);
        }
    }
    // 獲取快取中的訊息
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
