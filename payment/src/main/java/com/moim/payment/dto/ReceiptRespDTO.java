package com.moim.payment.dto;

import com.moim.payment.domain.Receipt;
import lombok.Getter;

@Getter
public class ReceiptRespDTO {
    private Long id;
    private String imageUrl;
    private Long paymentId;

    public ReceiptRespDTO(Receipt receipt) {
        this.id = receipt.getId();
        this.imageUrl = receipt.getImageUrl();
        this.paymentId = receipt.getPayment().getId();
    }
}