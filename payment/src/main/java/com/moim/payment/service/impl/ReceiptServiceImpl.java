package com.moim.payment.service.impl;

import com.moim.payment.domain.Payment;
import com.moim.payment.domain.Receipt;
import com.moim.payment.dto.ReceiptRespDTO;
import com.moim.payment.repository.PaymentRepository;
import com.moim.payment.repository.ReceiptRepository;
import com.moim.payment.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PaymentRepository paymentRepository;

    // 파일을 임시 저장하는 경로 (실제 서비스에서는 클라우드 스토리지 사용)
    private final String uploadDir = "uploads/receipts";

    /**
     * 특정 결제에 대한 영수증 사진을 업로드하고 저장합니다.
     *
     * @param paymentId 결제 ID
     * @param file      업로드할 파일
     * @return 저장된 영수증 정보 DTO
     */
    @Override
    @Transactional
    public ReceiptRespDTO uploadReceiptPhoto(Long paymentId, MultipartFile file) {
        // 1. 결제 유효성 확인
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + paymentId));

        // 2. 파일 저장 로직 (예시: 로컬 디스크에 저장)
        try {
            // 업로드 디렉토리가 없으면 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/receipts/" + uniqueFileName; // 프론트엔드에서 접근할 URL

            // 3. 영수증 엔티티 생성 및 저장
            Receipt newReceipt = Receipt.builder()
                    .payment(payment)
                    .imageUrl(fileUrl)
                    .build();

            Receipt savedReceipt = receiptRepository.save(newReceipt);

            return new ReceiptRespDTO(savedReceipt);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 특정 결제의 모든 영수증 목록을 조회합니다.
     *
     * @param paymentId 결제 ID
     * @return 영수증 목록 응답 DTO 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReceiptRespDTO> getReceiptsByPaymentId(Long paymentId) {
        // 1. 결제 유효성 확인 (선택 사항이지만 안전한 코드)
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + paymentId));

        // 2. 결제 ID로 영수증 목록 조회
        List<Receipt> receipts = receiptRepository.findByPaymentId(paymentId);

        // 3. 엔티티 리스트를 DTO 리스트로 변환
        return receipts.stream()
                .map(ReceiptRespDTO::new)
                .collect(Collectors.toList());
    }
}
