package com.moim.payment.service;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.SettlementHistory;
import com.moim.payment.dto.SettlementHistoryReqDTO;
import com.moim.payment.dto.SettlementHistoryRespDTO;
import com.moim.payment.repository.MoimRepository;
import com.moim.payment.repository.SettlementHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementHistoryRepository settlementHistoryRepository;
    private final MoimRepository moimRepository;

    @Transactional
    public SettlementHistoryRespDTO addSettlementHistory(SettlementHistoryReqDTO reqDTO) {
        Moim moim = moimRepository.findById(reqDTO.getMoimId())
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + reqDTO.getMoimId()));

        // 결재자 Usr ID 및 참여 Usr ID 목록의 유효성 검사 (실제 Usr가 존재하는지 등)
        // 여기서는 UsrService를 주입받아 validateUsrExists 등의 메서드를 호출할 수 있음

        SettlementHistory settlementHistory = reqDTO.toEntity(moim);
        SettlementHistory savedHistory = settlementHistoryRepository.save(settlementHistory);
        return new SettlementHistoryRespDTO(savedHistory);
    }

    // 특정 모임의 모든 정산 이력 조회
    public List<SettlementHistoryRespDTO> getSettlementHistoriesByMoim(Long moimId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + moimId));

        return settlementHistoryRepository.findByMoim(moim).stream()
                .map(SettlementHistoryRespDTO::new)
                .collect(Collectors.toList());
    }
}