package com.moim.payment.dto;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.SettlementHistory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SettlementHistoryReqDTO {
    @NotNull(message = "모임 ID는 필수입니다.")
    private Long moimId;

    @NotBlank(message = "결재 내용은 필수입니다.")
    private String description;

    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    private Double amount;

    @NotNull(message = "결재 일자는 필수입니다.")
    private LocalDate paymentDate;

    @NotNull(message = "결재자 Usr ID는 필수입니다.")
    private Long payerUsrId;

    @NotNull(message = "참여 Usr ID 목록은 필수입니다.")
    private List<Long> participantUsrIds;

    // DTO에서 엔티티로 변환하는 메서드
    // Moim 객체는 서비스 레이어에서 직접 조회하여 주입해야 합니다.
    public SettlementHistory toEntity(Moim moim) {
        return SettlementHistory.builder()
                .moim(moim)
                .description(this.description)
                .amount(this.amount)
                .paymentDate(this.paymentDate)
                .payerUsrId(this.payerUsrId)
                .participantUsrIds(this.participantUsrIds)
                .build();
    }
}
