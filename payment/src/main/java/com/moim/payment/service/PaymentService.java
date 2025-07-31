package com.moim.payment.service;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.Payment;
import com.moim.payment.domain.UsrMoim;
import com.moim.payment.dto.PaymentReq;
import com.moim.payment.dto.PaymentResp;
import com.moim.payment.repository.MoimRepository;
import com.moim.payment.repository.UsrMoimRepository;
import com.moim.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MoimRepository MoimRepository;

    public PaymentResp createPayment(PaymentReq request) {
        Moim moim = MoimRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Moim not found"));

        Payment payment = Payment.builder()
                .payer(request.getPayer())
                .amount(request.getAmount())
                .paidAt(LocalDateTime.now())
                .moim(moim)
                .build();

        Payment saved = paymentRepository.save(payment);

        return PaymentResp.builder()
                .id(saved.getId())
                .payerName(saved.getPayer().getUsrname())
                .amount(saved.getAmount())
                .paidAt(saved.getPaidAt())
//                .groupId(saved.getUsrMoim().getId())
                .build();
    }

    public List<PaymentResp> getPaymentsByGroup(Long groupId) {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(p -> p.getMoim().getId().equals(groupId))
                .map(p -> PaymentResp.builder()
                        .id(p.getId())
                        .payerName(p.getPayer().getUsrname())
                        .amount(p.getAmount())
                        .paidAt(p.getPaidAt())
                        .groupId(p.getMoim().getId())
                        .build())
                .collect(Collectors.toList());
    }
}

