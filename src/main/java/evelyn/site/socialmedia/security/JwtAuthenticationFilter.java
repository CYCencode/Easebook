package evelyn.site.socialmedia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        // 從header 拿
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }


        // 檢查是否取得 JWT
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 從token 獲得 email 作為身份驗證
            String username = jwtTokenProvider.getAllClaimsFromToken(token).get("email").toString();

            // 載入用戶資訊
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 設定 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (token == null) {
            log.warn("JWT Token is missing. Request URI: {}", request.getRequestURI());
        } else {
            log.warn("JWT Token is invalid. Request URI: {}", request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}