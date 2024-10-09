package evelyn.site.socialmedia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "請輸入有效的信箱")
    @Email(message = "請輸入有效的信箱")
    private String email;

    @NotBlank(message = "密碼不能為空")
    private String password;
}
