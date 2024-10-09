package evelyn.site.socialmedia.config;

import evelyn.site.socialmedia.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String jwtToken = accessor.getFirstNativeHeader("jwt");
                // 若 jwt 為合法，作為 Principal
                if (jwtTokenProvider.validateToken(jwtToken)) {
                    // 創建自定義的 Principal，使用 jwtToken
                    // 創建 Principal 並設置到 accessor
                    String userId = (String) jwtTokenProvider.getAllClaimsFromToken(jwtToken).get("id");
                    //String userId = claims.get("id", String.class);
                    Principal userPrincipal = () -> userId;
                    accessor.setUser(userPrincipal);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return message;
    }
}


