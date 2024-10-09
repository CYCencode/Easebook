package evelyn.site.socialmedia.service;

import evelyn.site.socialmedia.dto.LoginRequestDTO;
import evelyn.site.socialmedia.dto.SignupRequestDTO;

public interface AuthService {
    String createUser(SignupRequestDTO signupRequestDTO);
    String loginUser(LoginRequestDTO loginRequestDTO);
}
