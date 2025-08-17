package com.moim.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentReqDTO {
    // moimId 필드 추가
    @NotNull(message = "모임 ID는 필수입니다.")
    private Long moimId;

    @NotNull(message = "결제 금액은 필수입니다.")
    private Double amount;

    private String description;

    @NotNull(message = "결제일은 필수입니다.")
    private LocalDate paymentDate;

    @NotNull(message = "결제 회원의 ID는 필수입니다.")
    private Long payerMemberId;
}