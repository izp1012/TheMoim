package com.moim.payment.service;

import com.moim.payment.dto.ReceiptRespDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ReceiptService {
    ReceiptRespDTO uploadReceiptPhoto(Long paymentId, MultipartFile file);
    List<ReceiptRespDTO> getReceiptsByPaymentId(Long paymentId);
}