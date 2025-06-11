package com.moim.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SettlementHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim; // 어떤 모임에 속하는 정산 이력인지

    @Column(nullable = false)
    private String description; // 결재 내용 (예: 저녁 식사비, 회비 납부)

    @Column(nullable = false)
    private Double amount; // 금액

    @Column(nullable = false)
    private LocalDate paymentDate; // 결재 일자

    @Column(nullable = false)
    private Long payerUsrId; // 결재자 Usr ID (누가 지불했는지)

    @ElementCollection // 컬렉션 타입의 필드를 엔티티 내에 포함 (단일 테이블 매핑)
    @CollectionTable(name = "settlement_participants", joinColumns = @JoinColumn(name = "settlement_id"))
    @Column(name = "participant_usr_id")
    private List<Long> participantUsrIds = new ArrayList<>(); // 참여 Usr ID 목록

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime recordedAt; // 기록된 시각

    @Builder
    public SettlementHistory(Moim moim, String description, Double amount, LocalDate paymentDate, Long payerUsrId, List<Long> participantUsrIds) {
        this.moim = moim;
        this.description = description;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.payerUsrId = payerUsrId;
        this.participantUsrIds = participantUsrIds != null ? new ArrayList<>(participantUsrIds) : new ArrayList<>();
    }

    // 정산 이력 업데이트 메서드 (필요시 사용)
    public void update(String description, Double amount, LocalDate paymentDate, Long payerUsrId, List<Long> participantUsrIds) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description;
        }
        if (amount != null && amount >= 0) {
            this.amount = amount;
        }
        if (paymentDate != null) {
            this.paymentDate = paymentDate;
        }
        if (payerUsrId != null && payerUsrId > 0) {
            this.payerUsrId = payerUsrId;
        }
        if (participantUsrIds != null) {
            this.participantUsrIds = new ArrayList<>(participantUsrIds);
        }
    }
}
