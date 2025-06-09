package com.moim.payment.repository;

import com.moim.payment.domain.Usr.Usr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UsrRepository extends JpaRepository<Usr, Long> {

    Optional<Usr> findByUsrId(String usrId);
    Optional<Usr> findByUsrname(String usrName); //Jpa NamedQuery 작동

    Optional<Usr> findByEmail(String usrmail); //Jpa NamedQuery 작동
    //save - 이미 만들어져 있음
    boolean existsByUsrId(String userId);
}
