package com.moim.payment.domain;

import com.moim.payment.domain.Usr.Usr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제자를 UsrGroupMember와 연결하여 유연성 확보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usr_group_member_id")
    private UsrGroupMember payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    // @CreatedDate 대신 @CreationTimestamp 사용
    @CreationTimestamp
    private LocalDateTime paidAt;
}

