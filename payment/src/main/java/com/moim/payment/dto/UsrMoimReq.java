package com.moim.payment.dto;

import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.domain.UsrMoim;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsrMoimReq {
    private Long moimAccountId;

    private String moimName;

    private String moimDesp;

    private String imageUrl;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private String usrId;

    private boolean active;

    private boolean hidden;

    public static UsrMoim toEntity(UsrMoimReq usrMoimReq, Usr usr, String imageUrl) {
        return UsrMoim.builder()
                .moimName(usrMoimReq.getMoimName())
                .moimDesp(usrMoimReq.getMoimDesp())
                .imageUrl(imageUrl)
                .createBy(usr)
                .active(usrMoimReq.isActive())
                .hidden(usrMoimReq.isHidden())
                .build();
    }
}
