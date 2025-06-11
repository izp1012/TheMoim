package com.moim.payment.repository;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {

    // 특정 모임의 모든 정산 이력 조회
    List<SettlementHistory> findByMoim(Moim moim);

    // 특정 모임의 특정 결재자가 지불한 정산 이력 조회
    List<SettlementHistory> findByMoimAndPayerUsrId(Moim moim, Long payerUsrId);
}