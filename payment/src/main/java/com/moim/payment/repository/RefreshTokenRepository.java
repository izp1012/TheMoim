package com.moim.payment.repository;

import com.moim.payment.domain.RefreshToken;
import com.moim.payment.domain.Usr.Usr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUsr(Usr usr);
}
