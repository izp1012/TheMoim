package com.moim.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Moim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String moimname; // 모임 명칭

    @Column(nullable = true, length = 500)
    private String moimdesp; // 모임 설명

    // 모임 생성자 (Usr의 ID)
    @Column(nullable = false)
    private Long createdByUsrId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 양방향 매핑 (Moim과 UsrMoim)
    // Moim이 삭제되면 UsrMoim도 함께 삭제
    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsrMoim> usrMoims = new ArrayList<>();

    // Moim과 SettlementHistory (1:N 관계)
    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlementHistory> settlementHistories = new ArrayList<>();


    @Builder
    public Moim(String moimname, String moimdesp, Long createdByUsrId) {
        this.moimname = moimname;
        this.moimdesp = moimdesp;
        this.createdByUsrId = createdByUsrId;
    }

    // 모임 정보 업데이트 메서드
    public void update(String moimname, String moimdesp) {
        if (moimname != null && !moimname.trim().isEmpty()) {
            this.moimname = moimname;
        }
        if (moimdesp != null) {
            this.moimdesp = moimdesp;
        }
    }
}
