package com.moim.payment.controller;

import com.moim.payment.domain.Payment;
import com.moim.payment.dto.PaymentReq;
import com.moim.payment.dto.PaymentResp;
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
    public ResponseEntity<PaymentResp> create(@RequestBody PaymentReq request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<PaymentResp>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(paymentService.getPaymentsByGroup(groupId));
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

