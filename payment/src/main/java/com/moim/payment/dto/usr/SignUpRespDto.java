package com.moim.payment.dto.usr;

import com.moim.payment.domain.Usr.Usr;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class SignUpRespDto {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    // 엔티티에서 DTO로 변환하는 생성자
    public SignUpRespDto(Usr usr) {
        this.id = usr.getId();
        this.username = usr.getUsrname();
        this.email = usr.getEmail();
        this.createdAt = usr.getCreatedAt();
    }
}