package com.moim.payment.repository;

import com.moim.payment.domain.Moim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoimRepository extends JpaRepository<Moim, Long> {
    // 생성자 ID로 모임 목록 조회
    List<Moim> findByCreatedByUsrId(Long createdByUsrId);

    // 모임 이름으로 모임 조회 (중복 확인 등)
    Optional<Moim> findByMoimname(String moimname);
}
