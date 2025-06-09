package com.moim.payment.domain;

import com.moim.payment.domain.Usr.Usr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsrMoim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String moimName;

    private String moimDesp;

    private String imageUrl;

    @OneToMany(mappedBy = "usrMoim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Usr createBy;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean hidden;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean active;

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setUsrMoim(this);
    }
}