package com.moim.payment.domain;

import com.moim.payment.domain.Usr.Usr;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usr_id")
    private Usr payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @Column(nullable = false)
    private String description; // 지불 내용 (예: 저녁 식사비, 회비 납부)

    @Column(nullable = false)
    private int amount; //금액

    @Column(nullable = false)
    private LocalDate paymentDate; // 지불 일자

    @CreatedDate
    private LocalDateTime paidAt;

//    public void setUsrMoim(UsrMoim usrMoim) {
//        this.usrMoim = usrMoim;
//    }
}
