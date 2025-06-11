package com.moim.payment.dto.usr;

import com.moim.payment.domain.Usr.Usr;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UsrReqDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginReqDto {
        private String userId;
//        private String username;
        private String password;

        public UsernamePasswordAuthenticationToken toAuthentication() {
            return new UsernamePasswordAuthenticationToken(userId, password);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JoinReqDto {

        //영문 숫자 길이 2~20
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해주세요.")
        @NotEmpty // null or 공백일 수 없다
        private String username;

        //길이 4~20
        @Size(min = 4, max = 20)
        @NotEmpty
        private String password;

        //이메일형식
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}@[a-zA-Z0-9]{2,20}\\.[a-zA-Z]{2,3}", message = "이메일 형식으로 작성해주세요.")
        @NotEmpty
        private String email;


        public Usr toEntity(BCryptPasswordEncoder passwordEncoder) {
            return Usr.builder()
                    .usrname(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .build();
        }
    }


}
