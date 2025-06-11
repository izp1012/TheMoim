package com.moim.payment.dto;

import com.moim.payment.domain.SettlementHistory;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SettlementHistoryRespDTO {
    private Long id;
    private Long moimId;
    private String description;
    private Double amount;
    private LocalDate paymentDate;
    private Long payerUsrId;
    private List<Long> participantUsrIds;
    private LocalDateTime recordedAt;

    public SettlementHistoryRespDTO(SettlementHistory settlementHistory) {
        this.id = settlementHistory.getId();
        this.moimId = settlementHistory.getMoim().getId();
        this.description = settlementHistory.getDescription();
        this.amount = settlementHistory.getAmount();
        this.paymentDate = settlementHistory.getPaymentDate();
        this.payerUsrId = settlementHistory.getPayerUsrId();
        this.participantUsrIds = settlementHistory.getParticipantUsrIds();
        this.recordedAt = settlementHistory.getRecordedAt();
    }
}
