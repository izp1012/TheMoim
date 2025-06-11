package com.moim.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoimInviteReqDTO {
    @NotNull(message = "모임 ID는 필수입니다.")
    private Long moimId;

    @NotBlank(message = "초대할 사용자의 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String inviteeEmail; // 초대받을 사용자의 이메일

    @NotNull(message = "초대하는 사용자 ID는 필수입니다.")
    private Long inviterUsrId; // 초대하는 사용자의 ID
}