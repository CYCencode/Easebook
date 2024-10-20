package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.*;
import evelyn.site.socialmedia.exception.EmailAlreadyExistsException;
import evelyn.site.socialmedia.exception.InvalidUserException;
import evelyn.site.socialmedia.exception.ServerException;
import evelyn.site.socialmedia.model.UserProfile;
import evelyn.site.socialmedia.repository.UserRepository;
import evelyn.site.socialmedia.security.JwtTokenProvider;
import evelyn.site.socialmedia.service.AuthService;
import evelyn.site.socialmedia.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/friend")
    public ResponseEntity<List<UserProfile>> getFriends(@RequestParam String userId) {
        try {
            List<UserProfile> friends = userService.findFriendByUserId(userId);
            return ResponseEntity.ok(friends.isEmpty() ? Collections.emptyList() : friends);
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 搜尋用戶
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String username,
                                                     @RequestParam String currentUserId) {
        List<UserDTO> users = userService.findUsersByName(username, currentUserId);
        return ResponseEntity.ok(users);
    }

    //以userId 搜尋好友關係
    @GetMapping("/search/friendship")
    public ResponseEntity<UserDTO> searchFriendshipByUserId(@RequestParam String userId,
                                                            @RequestParam String currentUserId) {
        UserDTO user = userService.findFriendshipById(userId, currentUserId);
        log.info("/search/friendship by userId: {}", userId);
        log.info("/search/friendship user : {}", user);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/search/friends")
    public ResponseEntity<List<FriendDTO>> searchFriends(@RequestParam String username,
                                                         @RequestParam String currentUserId) {
        List<FriendDTO> friends = userService.findFriendsByName(username, currentUserId);
        return ResponseEntity.ok(friends);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signupRequestDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JwtDTO(null, errorMessage));
        }

        try {
            String jwt = authService.createUser(signupRequestDTO); // 回傳 JWT 字串
            return ResponseEntity.ok(new JwtDTO(jwt, "註冊成功"));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JwtDTO(null, e.getMessage())); // 信箱已存在，返回 403
        } catch (ServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JwtDTO(null, e.getMessage())); // 伺服器錯誤，返回 500
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JwtDTO(null, errorMessage));
        }

        try {
            String jwt = authService.loginUser(loginRequestDTO); // 回傳 JWT 字串
            return ResponseEntity.ok(new JwtDTO(jwt, "登入成功")); // 成功，回傳 200 OK 與 JWT
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new JwtDTO(null, "用戶尚未註冊")); // Email 或密碼不正確，返回 403
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JwtDTO(null, e.getMessage())); // 參數缺少，返回 400
        } catch (ServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JwtDTO(null, e.getMessage())); // 伺服器錯誤，返回 500
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        // 提取並驗證 JWT Token
        if (token == null || !token.startsWith("Bearer ")) {
            log.info("get user me with no jwt token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        String jwt = token.substring(7); // 去掉 "Bearer " 前綴

        // 驗證 JWT Token
        if (!jwtTokenProvider.validateToken(jwt)) {
            log.info("get user me with invalid token {}", jwt);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        // 從 Token 中提取使用者資訊
        Map<String, Object> claims = jwtTokenProvider.getAllClaimsFromToken(jwt);
        String email = (String) claims.get("email");
        String id = (String) claims.get("id");

        // 會變動的資訊(user name)從資料庫拿
        String name = userRepository.getUserNameByUserId(id);

        // 創建回應用戶的 JSON
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", name);
        userData.put("id", id);
        log.info("/api/me userdata : {}", userData);
        // 回傳用戶的資料
        return ResponseEntity.ok(userData);
    }


}

