package com.moim.payment.dto;

import com.moim.payment.domain.Payment;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PaymentRespDTO {
    private Long id;
    private Long moimId;
    private Double amount;
    private String description;
    private LocalDate paymentDate;
    private Long payerMemberId;

     public PaymentRespDTO(Payment payment) {
        this.id = payment.getId();
        this.moimId = payment.getMoim().getId();
        this.amount = payment.getAmount();
        this.description = payment.getDescription();
        this.paymentDate = payment.getPaymentDate();
        this.payerMemberId = payment.getPayer().getId();
    }
}