package com.moim.payment.repository;

import com.moim.payment.domain.Usr.Usr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsrRepository extends JpaRepository<Usr, Long> {

    Optional<Usr> findByUsrname(String usrName);

    Optional<Usr> findByEmail(String usrmail);

    boolean existsByUsrname(String usrname);

    boolean existsByEmail(String email);

}
