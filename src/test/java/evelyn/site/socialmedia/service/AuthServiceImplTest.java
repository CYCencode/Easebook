/*
測試 AuthService createUser 函數
 */
package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.SignupRequestDTO;
import evelyn.site.socialmedia.exception.EmailAlreadyExistsException;
import evelyn.site.socialmedia.model.Users;
import evelyn.site.socialmedia.repository.UserProfileRepository;
import evelyn.site.socialmedia.repository.UserRepository;
import evelyn.site.socialmedia.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void testCreateUser_EmailNotExists_ShouldRegisterUser() {
        // 準備 mock data
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setEmail("test@example.com");
        signupRequestDTO.setName("testuser");
        signupRequestDTO.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtTokenProvider.createToken(any(Users.class))).thenReturn("mockJwtToken");

        // 模擬 userRepository.save
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            return user;
        });

        // 測試 createUser 函數
        String result = authService.createUser(signupRequestDTO);

        // 檢查結果
        assertNotNull(result);
        assertEquals("mockJwtToken", result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    public void testCreateUser_EmailExists_ShouldThrowException() {
        // 準備 mock data
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setEmail("test@example.com");
        signupRequestDTO.setName("testuser");
        signupRequestDTO.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // 執行 createUser 函數並檢查
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.createUser(signupRequestDTO)
        );

        assertEquals("此信箱已被註冊", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }
}
