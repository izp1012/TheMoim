package com.moim.payment.domain;

import com.moim.payment.domain.Usr.Usr;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @OneToOne(fetch = FetchType.LAZY)
    private Usr usr;

    private String token;

    @Builder
    public RefreshToken(Usr usr, String token) {
        this.usr = usr;
        this.token = token;
    }

    public RefreshToken updateValue(String token) {
        this.token = token;
        return this;
    }
}
