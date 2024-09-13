package evelyn.site.socialmedia.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
@Log4j2
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 取得用戶名
            String username = accessor.getFirstNativeHeader("username");
            log.info("username {}" , username);
            if (username != null) {
                // 創建自定義的 Principal
                Principal userPrincipal = new Principal() {
                    @Override
                    public String getName() {
                        return username;
                    }
                };

                // 將 userPrincipal 設為 WebSocket session 中的用戶身份
                // accessor.setUser接收principal介面, 包含UsernamePasswordAuthenticationToken
                accessor.setUser(userPrincipal);

                // 將身份驗證信息存入 SecurityContext
                // 包含用戶名、密碼以及權限
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        return message;
    }
}

