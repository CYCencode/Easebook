package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.ProfileRequestDTO;
import evelyn.site.socialmedia.dto.ProfileResponseDTO;

import java.io.IOException;

public interface ProfileService {
    ProfileResponseDTO updateProfile(ProfileRequestDTO profileRequestDTO) throws IOException;
    ProfileResponseDTO getProfileByUserId(String userId);
}
