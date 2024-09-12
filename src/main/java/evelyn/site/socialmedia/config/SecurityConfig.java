package evelyn.site.socialmedia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.context.annotation.Bean;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/ws/**").permitAll() // WebSocket 無需認證
                        .anyRequest().permitAll() // 允許所有請求
                )
                .csrf(csrf -> csrf.disable()); // 禁用 CSRF 防護（WebSocket 通常不需要）

        return http.build();
    }
}