package evelyn.site.socialmedia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {

    @NotBlank
    @Email(message = "請輸入有效的信箱")
    private String email;
    @NotBlank(message = "密碼不能為空")
    @Size(min=6, message="密碼需要六位以上")
    private String password;
    @NotBlank(message = "姓名不能為空")
    private String name;
    private Instant createAt;
}

