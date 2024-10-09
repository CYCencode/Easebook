package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.LoginRequestDTO;
import evelyn.site.socialmedia.dto.SignupRequestDTO;
import evelyn.site.socialmedia.exception.EmailAlreadyExistsException;
import evelyn.site.socialmedia.exception.InvalidUserException;
import evelyn.site.socialmedia.model.UserProfile;
import evelyn.site.socialmedia.model.Users;
import evelyn.site.socialmedia.repository.UserProfileRepository;
import evelyn.site.socialmedia.repository.UserRepository;
import evelyn.site.socialmedia.security.JwtTokenProvider;
import evelyn.site.socialmedia.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String createUser(SignupRequestDTO signupRequestDTO) {
        log.info("收到註冊請求: {}", signupRequestDTO.getEmail());

        // 檢查信箱是否已存在
        if (userRepository.existsByEmail(signupRequestDTO.getEmail())) {
            log.warn("註冊失敗 : 該信箱已被註冊: {}", signupRequestDTO.getEmail());
            throw new EmailAlreadyExistsException("此信箱已被註冊");
        }
        return registerUser(signupRequestDTO);
    }


    private String registerUser(SignupRequestDTO signupRequestDTO) {
        log.debug("嘗試註冊新用戶，信箱: {}", signupRequestDTO.getEmail());

        // 創建新用戶
        Users user = new Users();
        user.setId(UUIDGenerator.generateUUID());
        user.setName(signupRequestDTO.getName());
        user.setEmail(signupRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));
        user.setCreateAt(Instant.now());

        // 保存用戶
        user = userRepository.save(user);
        // 同步資訊到用戶個人頁面
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getId());
        userProfile.setUsername(user.getName());
        userProfile.setEmail(signupRequestDTO.getEmail());
        userProfileRepository.save(userProfile);
        // 生成 JWT 並返回
        String jwt = jwtTokenProvider.createToken(user);
        log.info("用戶註冊成功，JWT 已生成: {}", jwt);
        return jwt;
    }

    @Override
    public String loginUser(LoginRequestDTO loginRequestDTO) {
        log.info("收到登入請求: {}", loginRequestDTO.getEmail());

        // 查找用戶
        Users user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("登入失敗 : 該信箱尚未註冊: {}", loginRequestDTO.getEmail());
                    return new IllegalArgumentException("該信箱尚未註冊");
                });

        // 驗證密碼是否正確
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            log.warn("登入失敗 : 密碼不正確: {}", loginRequestDTO.getEmail());
            throw new InvalidUserException("密碼不正確");
        }

        // 生成 JWT 並返回
        String jwt = jwtTokenProvider.createToken(user);
        log.info("用戶登入成功，JWT 已生成: {}", jwt);
        return jwt;
    }
}


