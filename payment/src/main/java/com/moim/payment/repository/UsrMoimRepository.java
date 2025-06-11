package com.moim.payment.repository;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.domain.UsrMoim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsrMoimRepository extends JpaRepository<UsrMoim, Long> {

    List<UsrMoim> getAllByMoim_Moimname(String usrMoimName);

    // 특정 Usr가 속한 모든 UsrMoim 조회
    List<UsrMoim> findByUsr(Usr usr);

    // 특정 Usr가 특정 Moim에 이미 속해 있는지 확인
    Optional<UsrMoim> findByUsrAndMoim(Usr usr, Moim moim);

    // Usr ID와 Moim ID로 UsrMoim 엔티티 조회 (초대 여부 확인 등)
    Optional<UsrMoim> findByUsr_IdAndMoim_Id(Long usrId, Long moimId);
}
