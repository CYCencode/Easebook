package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.ProfileRequestDTO;
import evelyn.site.socialmedia.dto.ProfileResponseDTO;
import evelyn.site.socialmedia.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Log4j2
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // POST: 用於上傳、修改用戶個人資料和照片
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@ModelAttribute ProfileRequestDTO profileRequestDTO) {
        log.info("updateProfile ProfileRequestDTO : {}", profileRequestDTO);
        try {
            ProfileResponseDTO profileResponseDTO = profileService.updateProfile(profileRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(profileResponseDTO);
        } catch (IOException e) {
            log.error(e);
            // 捕獲 S3 或文件上傳相關的錯誤
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files: " + e.getMessage());
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update profile: " + e.getMessage());
        }
    }

    // GET: 用於獲取指定 userId 的用戶資料
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDTO> getProfile(@PathVariable String userId) {
        ProfileResponseDTO profileResponseDTO = profileService.getProfileByUserId(userId);
        if (profileResponseDTO != null) {
            return ResponseEntity.ok(profileResponseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}

