package com.moim.payment.service;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.Payment;
import com.moim.payment.domain.UsrGroupMember; // 추가
import com.moim.payment.dto.PaymentReqDTO;
import com.moim.payment.dto.PaymentRespDTO;
import com.moim.payment.repository.MoimRepository;
import com.moim.payment.repository.PaymentRepository;
import com.moim.payment.repository.UsrGroupMemberRepository; // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MoimRepository moimRepository;
    private final UsrGroupMemberRepository usrGroupMemberRepository; // UsrRepository 대신 UsrGroupMemberRepository 사용

    @Transactional
    public PaymentRespDTO addPayment(PaymentReqDTO request) {
        Moim moim = moimRepository.findById(request.getMoimId())
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + request.getMoimId()));

        UsrGroupMember payerMember = usrGroupMemberRepository.findById(request.getPayerMemberId())
                .orElseThrow(() -> new IllegalArgumentException("결제한 회원을 찾을 수 없습니다: " + request.getPayerMemberId()));

        Payment payment = Payment.builder()
                .moim(moim)
                .payer(payerMember) // UsrGroupMember 엔티티를 직접 설정
                .amount(request.getAmount())
                .description(request.getDescription())
                .paymentDate(request.getPaymentDate())
                .build();

        Payment saved = paymentRepository.save(payment);

        return new PaymentRespDTO(saved);
    }

    public List<PaymentRespDTO> getPaymentsByMoimId(Long moimId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + moimId));

        List<Payment> payments = paymentRepository.findByMoimId(moimId);

        return payments.stream()
                .map(PaymentRespDTO::new)
                .collect(Collectors.toList());
    }
}
