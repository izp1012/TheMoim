package com.moim.payment.controller;

import com.moim.payment.dto.SettlementHistoryReqDTO;
import com.moim.payment.dto.SettlementHistoryRespDTO;
import com.moim.payment.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements") // settlements로 매핑
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // 모임에 정산 결재 이력 추가
    @PostMapping
    public ResponseEntity<SettlementHistoryRespDTO> addSettlementHistory(@Valid @RequestBody SettlementHistoryReqDTO reqDTO) {
        try {
            SettlementHistoryRespDTO newHistory = settlementService.addSettlementHistory(reqDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newHistory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 특정 모임의 모든 정산 이력 조회
    @GetMapping("/by-moim/{moimId}")
    public ResponseEntity<List<SettlementHistoryRespDTO>> getSettlementHistoriesByMoim(@PathVariable Long moimId) {
        try {
            List<SettlementHistoryRespDTO> histories = settlementService.getSettlementHistoriesByMoim(moimId);
            return ResponseEntity.ok(histories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
