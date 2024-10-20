package evelyn.site.socialmedia.controller;

import evelyn.site.socialmedia.dto.FriendRequestDTO;
import evelyn.site.socialmedia.dto.ProfileResponseDTO;
import evelyn.site.socialmedia.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    @GetMapping("/friend-requests")
    public ResponseEntity<List<FriendRequestDTO>> getFriendRequests(@RequestParam("userId") String userId) {
        List<FriendRequestDTO> requests = friendRequestService.getFriendRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/friend-requests")
    public ResponseEntity<FriendRequestDTO> sendFriendRequest(@RequestBody FriendRequestDTO friendRequestDTO) {
        FriendRequestDTO requests = friendRequestService.sendFriendRequest(friendRequestDTO);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/friend-requests/reply")
    public ResponseEntity<ProfileResponseDTO> replyToFriendRequest(@RequestParam("request_id") String id,
                                                                   @RequestParam(value = "senderId", required = false) String senderId,
                                                                   @RequestParam(value = "receiverId", required = false) String receiverId,
                                                                   @RequestParam("accept") boolean accept) {
        ProfileResponseDTO profileResponseDTO = friendRequestService.replyToFriendRequest(id, senderId, receiverId, accept);
        if (accept && profileResponseDTO != null) {
            return ResponseEntity.ok(profileResponseDTO);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

}
