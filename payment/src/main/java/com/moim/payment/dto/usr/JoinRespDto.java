package com.moim.payment.dto.usr;

import com.moim.payment.domain.Usr.Usr;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JoinRespDto {
    private String username;
    private String email;

    public JoinRespDto(Usr usr) {
        this.username = usr.getUsrname();
        this.email = usr.getEmail();
    }
}