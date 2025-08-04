package com.moim.payment.dto.kftc;

import lombok.*;

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
    private String fintechUseNum;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {

        // 기본 계좌 정보
        private String bankName;              // 은행명
        private String bankCode;              // 은행코드
        private String accountNumber;         // 계좌번호 (마스킹된)
        private String ownerName;             // 계좌소유자명
        private String fintechUseNum;         // 핀테크이용번호

        // 계좌 상세 정보
        private String accountType;           // 계좌종류 (1:수시입출금, 2:예적금, 3:대출계좌 등)
        private String inquiryAgreeYn;        // 조회서비스 이용동의여부 (Y/N)
        private String transferAgreeYn;       // 출금이체 이용동의여부 (Y/N)
        private String accountState;          // 계좌상태 (1:정상, 2:정지 등)

        // 상품 정보
        private String productName;           // 상품명

        // 날짜 정보
        private String accountIssueDate;      // 계좌개설일 (YYYYMMDD)
        private String maturityDate;          // 만기일 (YYYYMMDD)
        private String lastTranDate;          // 최종거래일 (YYYYMMDD)

        // 편의 메서드들
        public boolean isInquiryAvailable() {
            return "Y".equals(inquiryAgreeYn);
        }

        public boolean isTransferAvailable() {
            return "Y".equals(transferAgreeYn);
        }

        public boolean isActiveAccount() {
            return "1".equals(accountState);
        }

        public String getFormattedAccountNumber() {
            if (accountNumber == null) return null;

            // 이미 포맷팅된 경우
            if (accountNumber.contains("-")) {
                return accountNumber;
            }

            // 포맷팅 적용 (예: 123456789012 -> 123-456-****789012)
            if (accountNumber.length() >= 10) {
                return accountNumber.substring(0, 3) + "-" +
                        accountNumber.substring(3, 6) + "-" +
                        accountNumber.substring(6);
            }

            return accountNumber;
        }
    }
}
