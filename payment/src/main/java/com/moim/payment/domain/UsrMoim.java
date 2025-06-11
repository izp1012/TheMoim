package com.moim.payment.domain;

import com.moim.payment.domain.Usr.Usr;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
// 복합 키를 사용하지 않고 단순히 PK로 UsrMoim ID를 사용하는 방식
public class UsrMoim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usr_id", nullable = false)
    private Usr usr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    private Moim moim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsrMoimRole role; // 역할: MEMBER, MANAGER, OWNER 등

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public UsrMoim(Usr usr, Moim moim, UsrMoimRole role) {
        this.usr = usr;
        this.moim = moim;
        this.role = role;
    }

    // UsrMoimRole Enum
    public enum UsrMoimRole {
        OWNER,     // 모임 생성자
        MANAGER,   // 모임 관리자
        MEMBER     // 일반 회원
    }

    // 역할 업데이트 메서드 (필요시 사용)
    public void updateRole(UsrMoimRole newRole) {
        this.role = newRole;
    }
}