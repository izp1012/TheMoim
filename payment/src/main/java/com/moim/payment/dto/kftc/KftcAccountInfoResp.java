package com.moim.payment.dto.kftc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KftcAccountInfoResp {
    @JsonProperty("res_code")
    private String resCode;
    @JsonProperty("res_message")
    private String resMessage;
    @JsonProperty("bank_name") // 은행명
    private String bankName;
    @JsonProperty("account_num_masked") // 마스킹된 계좌번호
    private String accountNumberMasked;
    @JsonProperty("balance_amt") // 잔액
    private String balanceAmount;
    @JsonProperty("user_name") // 예금주
    private String userName;
    @JsonProperty("tran_list") // 거래 내역 리스트 (예시)
    private List<Transaction> transactionList;

    @Getter
    @Setter
    public static class Transaction {
        private String id;
        private String date;
        private String description;
        private String amount;
    }
}
