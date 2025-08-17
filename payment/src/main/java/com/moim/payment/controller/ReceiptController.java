package com.moim.payment.controller;

import com.moim.payment.dto.ReceiptRespDTO;
import com.moim.payment.service.ReceiptService; // 새로운 서비스
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/{paymentId}/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /**
     * 특정 결제에 대한 영수증 사진을 업로드합니다.
     * 엔드포인트: POST /api/v1/payments/{paymentId}/receipts
     */
    @PostMapping
    public ResponseEntity<ReceiptRespDTO> uploadReceiptPhoto(@PathVariable Long paymentId,
                                                             @RequestParam("file") MultipartFile file) {
        try {
            ReceiptRespDTO newReceipt = receiptService.uploadReceiptPhoto(paymentId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReceipt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 특정 결제의 모든 영수증 사진 목록을 조회합니다.
     * 엔드포인트: GET /api/v1/payments/{paymentId}/receipts
     */
    @GetMapping
    public ResponseEntity<List<ReceiptRespDTO>> getReceiptPhotos(@PathVariable Long paymentId) {
        try {
            List<ReceiptRespDTO> receipts = receiptService.getReceiptsByPaymentId(paymentId);
            return ResponseEntity.ok(receipts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}