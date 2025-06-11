package com.moim.payment.dto.usr;

import com.moim.payment.domain.Usr.Usr;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpReqDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해주세요.")
    @NotEmpty // null or 공백일 수 없다
    private String usrname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 20)
    @NotEmpty
    private String password;

    @NotBlank(message = "이메일은 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{2,20}@[a-zA-Z0-9]{2,20}\\.[a-zA-Z]{2,3}", message = "이메일 형식으로 작성해주세요.") //이메일 형식
    @NotEmpty
    private String email;

    public Usr toEntity(BCryptPasswordEncoder passwordEncoder) {
        return Usr.builder()
                .usrname(usrname)
                .password(passwordEncoder.encode(password))
                .email(email)
                .build();
    }
}