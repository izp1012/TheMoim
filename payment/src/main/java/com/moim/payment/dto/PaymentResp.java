package com.moim.payment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResp {
    private Long id;
    private String payerName;
    private int amount;
    private LocalDateTime paidAt;
    private Long groupId;
}