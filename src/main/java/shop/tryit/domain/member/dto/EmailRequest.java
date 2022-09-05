package shop.tryit.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class EmailRequest {
    @Email
    @NotBlank(message = "이메일 인증은 필수입니다.")
    private String email;
}
