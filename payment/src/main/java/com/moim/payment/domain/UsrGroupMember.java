package com.moim.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "usr_group_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UsrGroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usrId; // 실제 사용자 ID (선택 사항)
    private Long groupId;
    private String membername;
    private String role;
    private String contactInfo;
    private Double defaultFee;

    @CreationTimestamp
    private LocalDateTime createdAt;
}