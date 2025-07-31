package com.moim.payment.dto.kftc;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private List<AccountSummary> accountSummaries;

    @Getter
    @Setter
    @Builder
    public static class TransactionDTO {
        private String id;
        private String amount;
        private String tran_date;
        private String tran_time;
        private String tran_amt;
        private String inout_type;
        private String print_content;
        private String after_balance_amt;
    }

    @Getter
    @Setter
    @Builder
    public static class AccountSummary {
        private String id;
        private String bankName;
        private String accountNumber;
        private String ownerName;
        private String fintechUseNum;
    }
}
