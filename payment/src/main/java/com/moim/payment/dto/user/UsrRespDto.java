package com.moim.payment.dto.user;

import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.domain.Usr.Usr;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsrRespDto {
    private String username;
    private String email;
    private UserRole role;
    private boolean social;

    public static UsrRespDto from(Usr usr) {
        return new UsrRespDto(usr.getUsrname(), usr.getEmail(), usr.getRole(), usr.isSocial());
    }
}
