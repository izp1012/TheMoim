package com.moim.payment.dto.kftc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferReqDto {
    private String fintechUseNum;        // 핀테크이용번호
    private String cntrAccountType;      // 약정 계좌/계좌 구분 (N: 계좌)
    private String cntrAccountNum;       // 약정 계좌/계좌 번호
    private String dpsWdPassPhrase;      // 입출금이체 암호문구
    private String dpsPrintContent;      // 입금계좌인자내역
    private String wdPrintContent;       // 출금계좌인자내역 (출금 시에만)
    private String tranAmt;              // 거래금액
    private String reqClientName;        // 요청고객성명
    private String reqClientBankCode;    // 요청고객계좌 개설기관.표준코드
    private String reqClientAccountNum;  // 요청고객계좌번호
    private String reqClientFintechUseNum; // 요청고객핀테크이용번호
    private String transferPurpose;      // 이체용도 (TR: 송금)
    private String recvClientName;       // 최종수취고객성명
    private String recvClientBankCode;   // 최종수취고객계좌 개설기관.표준코드
    private String recvClientAccountNum; // 최종수취고객계좌번호
    private String nameCheckOption;      // 수취인성명검증 ("on" 또는 "off")

    // 하위기관정보 (선택사항)
    private String subFmcName;           // 하위기관명
    private String subFmcNum;            // 하위기관번호
    private String subFmcBusinessNum;    // 하위기관 사업자등록번호
}
