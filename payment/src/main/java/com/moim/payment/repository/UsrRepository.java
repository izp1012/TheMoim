package com.moim.payment.repository;

import com.moim.payment.domain.Usr.Usr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UsrRepository extends JpaRepository<Usr, Long> {

    Optional<Usr> findByUsrname(String usrName);

    Optional<Usr> findByEmail(String usrmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
