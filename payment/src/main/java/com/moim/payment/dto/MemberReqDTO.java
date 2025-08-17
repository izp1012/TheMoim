package com.moim.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberReqDTO {
    @NotBlank(message = "회원 이름은 필수입니다.")
    private String name;
    private String role; // 예: "회원", "총무"
    private String contactInfo;
    @NotNull(message = "기본 회비는 필수입니다.")
    private Double defaultFee;
}
