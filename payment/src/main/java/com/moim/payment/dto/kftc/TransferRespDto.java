package com.moim.payment.dto.kftc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRespDto {
    private String rspCode;              // 응답코드
    private String rspMessage;           // 응답메시지
    private String apiTranId;            // API거래고유번호
    private String apiTranDtm;           // API거래일시
    private String bankTranId;           // 은행거래고유번호
    private String bankTranDate;         // 은행거래일자
    private String bankCodeTran;         // 거래은행코드
    private String bankRspCode;          // 은행응답코드
    private String bankRspMessage;       // 은행응답메시지
    private String fintechUseNum;        // 핀테크이용번호
    private String accountNum;           // 계좌번호
    private String printContent;         // 계좌인자내역
    private String tranAmt;              // 거래금액
    private String wdLimitRemainAmt;     // 출금한도잔여금액
}