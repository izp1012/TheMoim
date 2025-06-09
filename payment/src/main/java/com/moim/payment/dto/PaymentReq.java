package com.moim.payment.dto;

import com.moim.payment.domain.Usr.Usr;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReq {
    private Long groupId;
    private Usr payer;
    private int amount;
}