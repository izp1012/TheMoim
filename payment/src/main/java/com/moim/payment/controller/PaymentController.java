package com.moim.payment.controller;

import com.moim.payment.domain.Payment;
import com.moim.payment.dto.PaymentReqDTO;
import com.moim.payment.dto.PaymentRespDTO;
import com.moim.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentRespDTO> addPayMent(@RequestBody PaymentReqDTO request) {
        return ResponseEntity.ok(paymentService.addPayment(request));
    }

    @GetMapping("/group/{moidId}")
    public ResponseEntity<List<PaymentRespDTO>> getByMoim(@PathVariable Long moidId) {
        return ResponseEntity.ok(paymentService.getPaymentsByMoimId(moidId));
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> addTransaction(@RequestBody Payment payment) {
        System.out.println("Transaction added: " + payment);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllTransactions() {
        return ResponseEntity.ok(new ArrayList<>());
    }
}

