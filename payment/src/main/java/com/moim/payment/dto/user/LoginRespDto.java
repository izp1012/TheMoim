package com.moim.payment.dto.user;

import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRespDto {
    private String usrId;
    private String usrname;
    private String email;
    private String loginAt;
    private String jwtToken;
    private boolean social;
    private UserRole role;

    public LoginRespDto(Usr usr) {
        this.usrname = usr.getUsrname();
        this.email = usr.getEmail();
        this.loginAt = CustomDateUtil.toStringFormat(usr.getCreatedAt());
        this.social = usr.isSocial();
        this.role = usr.getRole();
    }

    public LoginRespDto(Usr usr, String jwtToken) {
        this.usrname = usr.getUsrname();
        this.email = usr.getEmail();
        this.loginAt = CustomDateUtil.toStringFormat(usr.getCreatedAt());
        this.jwtToken = jwtToken;
        this.social = usr.isSocial();
        this.role = usr.getRole();
    }
}