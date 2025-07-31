package com.moim.payment.dto.kftc;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder // Builder 패턴 사용 (선택 사항)
public class AccountInfoDto {
    private String bankName;
    private String accountNumber;
    private String balance;
    private String ownerName;
    private List<TransactionDTO> transactions;

    @Getter
    @Setter
    @Builder
    public static class TransactionDTO {
        private String id;
        private String date;
        private String description;
        private String amount;
    }
}
