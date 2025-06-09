package com.moim.payment.repository;

import com.moim.payment.domain.UsrMoim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsrMoimRepository extends JpaRepository<UsrMoim, Long> {

    List<UsrMoim> getAllByMoimName(String usrMoimName);
}
