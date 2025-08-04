package com.moim.payment.service;

import com.moim.payment.dto.kftc.*;
import com.moim.payment.util.CustomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j // 로깅을 위한 Lombok 어노테이션
public class KftcApiService {

    @Value("${kftc.client-id}")
    private String clientId;

    @Value("${kftc.client-secret}")
    private String clientSecret;

    @Value("${kftc.token-endpoint}")
    private String tokenEndpoint;

    @Value("${kftc.account-list-endpoint}")
    private String accountListEndpoint;

    @Value("${kftc.user-info-endpoint}")
    private String userInfoEndpoint;

    @Value("${kftc.balance-inquiry-endpoint}")
    private String balanceInquiryEndpoint;

    @Value("${kftc.transaction-history-endpoint}")
    private String transactionHiistoryendpoint;

    @Value("${kftc.transfer.deposit-endpoint}")
    private String depositEndpoint;

    @Value("${kftc.transfer.withdraw-endpoint}")
    private String withdrawEndpoint;

    private final WebClient webClient;

    // WebClient.Builder를 주입받아 WebClient 인스턴스를 생성합니다.
    public KftcApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 금융결재원 인가 코드를 사용하여 Access Token을 교환합니다.
     * @param code 인가 코드
     * @param redirectUri 등록된 리다이렉트 URI
     * @return KftcTokenResp DTO
     */
    public KftcTokenResp exchangeCodeForToken(String code, String redirectUri) {
        log.info("Access Token 교환 요청 시작: code={}, redirectUri={}", code, redirectUri);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        try {
            KftcTokenResp response = webClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(KftcTokenResp.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("Access Token 교환 성공: userSeqNo={}", response != null ? response.getUserSeqNo() : "null");
            return response;

        } catch (WebClientResponseException e) {
            log.error("KFTC API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("KFTC 토큰 교환 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("토큰 교환 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 교환 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 특정 계좌의 잔액을 조회합니다.
     */
    public String getAccountBalance(String accessToken, String fintechUseNum) {
        log.info("계좌 잔액 조회 요청: fintechUseNum={}", fintechUseNum);

        String bankTranId = generateBankTranId("M202501486U");
        log.info("bankTranId = {}", bankTranId);
        String tranDtime = getCurrentTimestamp();

        try {
            String fullBalanceInquiryApiUri = UriComponentsBuilder
                    .fromUriString(balanceInquiryEndpoint)
                    .queryParam("bank_tran_id", bankTranId)
                    .queryParam("fintech_use_num", fintechUseNum)
                    .queryParam("tran_dtime", tranDtime)
                    .build()
                    .toUriString();

            log.debug("잔액 조회 API 호출: URL={}", fullBalanceInquiryApiUri);

            Map<String, Object> responseBody = webClient.get()
                    .uri(fullBalanceInquiryApiUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("KFTC 잔액 조회 실패: HTTP Status={}, Error Body={}",
                                                clientResponse.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("KFTC 잔액 조회 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (responseBody == null) {
                throw new RuntimeException("잔액 조회 응답이 비어있습니다.");
            }

            // 응답 상태 확인
            String rspCode = (String) responseBody.get("rsp_code");
            log.debug("rspCode = {}", rspCode);
            if (!"A0000".equals(rspCode)) {
                String rspMessage = (String) responseBody.get("rsp_message");
                log.error("잔액 조회 실패: code={}, message={}", rspCode, rspMessage);
                throw new RuntimeException("잔액 조회 실패: " + rspMessage);
            }

            // 잔액 추출
            Map<String, Object> resList = (Map<String, Object>) responseBody.get("res_list");
            log.debug("resList = {}", resList.toString());
            if (resList != null) {
                Object balance = resList.get("balance_amt");
                String balanceStr = balance != null ? balance.toString() : "0";

                log.info("잔액 조회 성공: balance={}", balanceStr);
                return balanceStr;
            }

            log.warn("응답에서 잔액 정보를 찾을 수 없습니다.");
            return "0";

        } catch (WebClientResponseException e) {
            log.error("KFTC 잔액 조회 API 호출 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("잔액 조회 실패: " + e.getMessage());

        } catch (Exception e) {
            log.error("잔액 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new RuntimeException("잔액 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 통합 계좌 정보 조회 (계좌 목록 + 잔액 + 거래내역)
     */
    public AccountInfoDto getComprehensiveAccountInfo(String accessToken, String userSeqNo) {
        log.info("통합 계좌 정보 조회 시작: userSeqNo={}", userSeqNo);

        try {
            // 1. 계좌 목록 조회
            List<AccountInfoDto.AccountSummary> accounts = getAccountList(accessToken, userSeqNo);

            if (accounts.isEmpty()) {
                throw new RuntimeException("등록된 계좌가 없습니다.");
            }

            // 첫 번째 계좌를 대표 계좌로 사용
            AccountInfoDto.AccountSummary primaryAccount = accounts.get(0);
            log.debug("primaryAccount.getFintechUseNum= {}", primaryAccount.getFintechUseNum());
            log.debug("primaryAccount.getBankCode= {}", primaryAccount.getBankCode());
            log.debug("primaryAccount.getBankName= {}", primaryAccount.getBankName());
            // 2. 잔액 조회
            String balance = getAccountBalance(accessToken, primaryAccount.getFintechUseNum());

            // 3. 거래 내역 조회 (최근 7일)
//            List<AccountInfoDto.TransactionDTO> transactions = getTransactionHistory(
//                    accessToken, primaryAccount.getFintechUseNum(), 7);

            // 4. 통합 정보 구성
            return AccountInfoDto.builder()
                    .bankName(primaryAccount.getBankName())
                    .accountNumber(primaryAccount.getAccountNumber())
                    .ownerName(primaryAccount.getOwnerName())
                    .balance(balance)
                    .fintechUseNum(primaryAccount.getFintechUseNum())
//                    .transactions(transactions)
                    .build();

        } catch (Exception e) {
            log.error("통합 계좌 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("계좌 정보 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 획득한 Access Token을 사용하여 계좌 정보를 조회합니다.
     * @param accessToken Access Token
     * @param userSeqNo 사용자 일련번호 (필요한 경우)
     * @return 필터링된 계좌 정보 DTO
     */
    public AccountInfoDto getAccountInfo(String accessToken, String userSeqNo) {
        log.info("계좌 정보 조회 요청 시작: userSeqNo={}", userSeqNo);

        // 실제 금융결재원 계좌 정보 조회 API 호출 시 user_seq_no 등의 파라미터를 추가해야 할 수 있습니다.
        // 현재는 예시 엔드포인트만 사용하며, 실제 API 문서를 참조하세요.
         String fullAccountApiUri = userInfoEndpoint + "?user_seq_no=" + userSeqNo;

        KftcAccountInfoResp accountResponse = webClient.get()
                .uri(fullAccountApiUri) // 또는 fullAccountApiUri
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("KFTC 계좌 정보 조회 실패: HTTP Status={}, Error Body={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("KFTC 계좌 정보 조회 실패: " + errorBody));
                                })
                )
                .bodyToMono(KftcAccountInfoResp.class)
                .block(); // 블로킹 방식으로 Mono 결과를 기다립니다

        // 금융결재원 응답을 프론트엔드에 필요한 형태로 가공
        return AccountInfoDto.builder()
                .bankName(accountResponse.getBankName() != null ? accountResponse.getBankName() : "가상은행")
                .accountNumber(accountResponse.getAccountNumberMasked() != null ? accountResponse.getAccountNumberMasked() : "123-456-7890")
                .balance(accountResponse.getBalanceAmount() != null ? accountResponse.getBalanceAmount() + "원" : "1,234,567원")
                .ownerName(accountResponse.getUserName() != null ? accountResponse.getUserName() : "홍길동")
                .transactions(accountResponse.getTransactionList() != null ?
                        accountResponse.getTransactionList().stream()
                                .map(tx -> AccountInfoDto.TransactionDTO.builder()
                                        .id(tx.getId())
                                        .tran_date(tx.getDate())
                                        .print_content(tx.getDescription())
                                        .amount(tx.getAmount())
                                        .build())
                                .collect(Collectors.toList()) :
                        // 가상 거래 내역
                        List.of(
                                AccountInfoDto.TransactionDTO.builder().id("1").tran_date("2024-06-10").print_content("급여 입금").amount("+1,000,000원").build(),
                                AccountInfoDto.TransactionDTO.builder().id("2").tran_date("2024-06-11").print_content("온라인 쇼핑").amount("-50,000원").build()
                        )
                )
                .build();
    }

    // 유틸리티 메서드들
    private String generateBankTranId(String prefix) {
        String uniqueId = String.format("%09d", System.currentTimeMillis() % 1_000_000_000);
        return prefix + uniqueId;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * Access Token을 사용하여 계좌 목록을 조회합니다.
     */
    public List<AccountInfoDto.AccountSummary> getAccountList(String accessToken, String userSeqNo) {
        log.info("계좌 목록 조회 요청 시작: userSeqNo={}", userSeqNo);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        try {
            String fullAccountListApiUri = accountListEndpoint + "?user_seq_no=" + userSeqNo;

            log.debug("계좌 목록 조회 API 호출: URL={}", fullAccountListApiUri);

            Map<String, Object> responseBody = webClient.get()
                    .uri(fullAccountListApiUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("KFTC 계좌 목록 조회 실패: HTTP Status={}, Error Body={}",
                                                clientResponse.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("KFTC 계좌 목록 조회 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (responseBody == null) {
                throw new RuntimeException("계좌 목록 조회 응답이 비어있습니다.");
            }

            // 응답 상태 확인
            String rspCode = (String) responseBody.get("rsp_code");
            if (!"A0000".equals(rspCode)) {
                String rspMessage = (String) responseBody.get("rsp_message");
                log.error("계좌 목록 조회 실패: code={}, message={}", rspCode, rspMessage);
                throw new RuntimeException("계좌 목록 조회 실패: " + rspMessage);
            }

            // 계좌 목록 추출 및 변환
            List<Map<String, Object>> resList = (List<Map<String, Object>>) responseBody.get("res_list");
            if (resList == null || resList.isEmpty()) {
                log.warn("조회된 계좌가 없습니다.");
                return new ArrayList<>();
            }

            List<AccountInfoDto.AccountSummary> accountList = new ArrayList<>();

            for (Map<String, Object> account : resList) {
                try {
                    AccountInfoDto.AccountSummary summary = AccountInfoDto.AccountSummary.builder()
                            .bankName(getStringValue(account, "bank_name"))
                            .bankCode(getStringValue(account, "bank_code_std"))
                            .accountNumber(maskAccountNumber(getStringValue(account, "account_num_masked")))
                            .ownerName(getStringValue(account, "account_holder_name"))
                            .fintechUseNum(getStringValue(account, "fintech_use_num"))
                            .accountType(getStringValue(account, "account_type"))
                            .inquiryAgreeYn(getStringValue(account, "inquiry_agree_yn"))
                            .transferAgreeYn(getStringValue(account, "transfer_agree_yn"))
                            .accountState(getStringValue(account, "account_state"))
                            .productName(getStringValue(account, "product_name"))
                            .accountIssueDate(getStringValue(account, "account_issue_date"))
                            .maturityDate(getStringValue(account, "maturity_date"))
                            .lastTranDate(getStringValue(account, "last_tran_date"))
                            .build();

                    accountList.add(summary);

                } catch (Exception e) {
                    log.warn("계좌 정보 파싱 실패: {}, account={}", e.getMessage(), account);
                    // 개별 계좌 파싱 실패해도 전체는 계속 진행
                }
            }

            log.info("계좌 목록 조회 성공: 총 {}개 계좌", accountList.size());
            return accountList;

        } catch (HttpClientErrorException e) {
            log.error("계좌 목록 조회 클라이언트 오류: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("계좌 목록 조회 실패 (클라이언트 오류): " + e.getResponseBodyAsString());

        } catch (HttpServerErrorException e) {
            log.error("계좌 목록 조회 서버 오류: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("계좌 목록 조회 실패 (서버 오류): " + e.getResponseBodyAsString());

        } catch (ResourceAccessException e) {
            log.error("계좌 목록 조회 네트워크 오류: {}", e.getMessage(), e);
            throw new RuntimeException("계좌 목록 조회 연결 오류: " + e.getMessage());

        } catch (Exception e) {
            log.error("계좌 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new RuntimeException("계좌 목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 출금이체 처리
     */
    public TransferRespDto processWithdrawTransfer(String accessToken, TransferReqDto request) {
        log.info("출금이체 API 호출 시작: fintechUseNum={}", request.getFintechUseNum());

        String bankTranId = generateBankTranId("M201");  // 출금이체용 거래고유번호
        String tranDtime = getCurrentTimestamp();

        // 요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();

        // Header 파라미터들
        requestBody.put("bank_tran_id", bankTranId);
        requestBody.put("cntr_account_type", request.getCntrAccountType());
        requestBody.put("cntr_account_num", request.getCntrAccountNum());
        requestBody.put("dps_print_content", request.getDpsPrintContent());
        requestBody.put("fintech_use_num", request.getFintechUseNum());
        requestBody.put("wd_print_content", request.getWdPrintContent());
        requestBody.put("tran_amt", request.getTranAmt());
        requestBody.put("tran_dtime", tranDtime);
        requestBody.put("req_client_name", request.getReqClientName());
        requestBody.put("req_client_bank_code", request.getReqClientBankCode());
        requestBody.put("req_client_account_num", request.getReqClientAccountNum());
        requestBody.put("req_client_num", "HONGKILDONG1234"); // 요청고객회원번호 (고정값 또는 동적 생성)
        requestBody.put("transfer_purpose", request.getTransferPurpose());

        // Body 파라미터들 (하위기관정보 - 선택사항)
        if (request.getSubFmcName() != null) {
            requestBody.put("sub_fmc_name", request.getSubFmcName());
            requestBody.put("sub_fmc_num", request.getSubFmcNum());
            requestBody.put("sub_fmc_business_num", request.getSubFmcBusinessNum());
        }

        requestBody.put("recv_client_name", request.getRecvClientName());
        requestBody.put("recv_client_bank_code", request.getRecvClientBankCode());
        requestBody.put("recv_client_account_num", request.getRecvClientAccountNum());

        try {
            Map<String, Object> responseBody = webClient.post()
                    .uri(withdrawEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("KFTC 출금이체 실패: HTTP Status={}, Error Body={}",
                                                clientResponse.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("KFTC 출금이체 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return mapToTransferResponse(responseBody);

        } catch (Exception e) {
            log.error("출금이체 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("출금이체 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 입금이체 처리
     */
    public TransferRespDto processDepositTransfer(String accessToken, TransferReqDto request) {
        log.info("입금이체 API 호출 시작: fintechUseNum={}", request.getFintechUseNum());

        String bankTranId = generateBankTranId("M202");  // 입금이체용 거래고유번호
        String tranDtime = getCurrentTimestamp();

        // 요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();

        // Header 파라미터들
        requestBody.put("cntr_account_type", request.getCntrAccountType());
        requestBody.put("cntr_account_num", request.getCntrAccountNum());
        requestBody.put("wd_pass_phrase", request.getDpsWdPassPhrase());
        requestBody.put("wd_print_content", request.getWdPrintContent());
        requestBody.put("name_check_option", request.getNameCheckOption());

        // 하위기관정보 (선택사항)
        if (request.getSubFmcName() != null) {
            requestBody.put("sub_fmc_name", request.getSubFmcName());
            requestBody.put("sub_fmc_num", request.getSubFmcNum());
            requestBody.put("sub_fmc_business_num", request.getSubFmcBusinessNum());
        }

        requestBody.put("tran_dtime", tranDtime);
        requestBody.put("req_cnt", 1);

        // Body 파라미터들 (req_list는 배열)
        Map<String, Object> reqListItem = new HashMap<>();
        reqListItem.put("tran_no", "1");
        reqListItem.put("bank_tran_id", bankTranId);
        reqListItem.put("fintech_use_num", request.getFintechUseNum());
        reqListItem.put("print_content", request.getDpsPrintContent());
        reqListItem.put("tran_amt", request.getTranAmt());
        reqListItem.put("req_client_name", request.getReqClientName());
        reqListItem.put("req_client_bank_code", request.getReqClientBankCode());
        reqListItem.put("req_client_account_num", request.getReqClientAccountNum());
        reqListItem.put("req_client_fintech_use_num", request.getReqClientFintechUseNum());

        requestBody.put("req_list", List.of(reqListItem));

        try {
            Map<String, Object> responseBody = webClient.post()
                    .uri(depositEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("KFTC 입금이체 실패: HTTP Status={}, Error Body={}",
                                                clientResponse.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("KFTC 입금이체 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return mapToTransferResponse(responseBody);

        } catch (Exception e) {
            log.error("입금이체 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("입금이체 처리 실패: " + e.getMessage());
        }
    }

    /**
     * API 응답을 DTO로 변환
     */
    private TransferRespDto mapToTransferResponse(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("이체 응답이 비어있습니다.");
        }

        // 응답 상태 확인
        String rspCode = getStringValue(responseBody, "rsp_code");
        String rspMessage = getStringValue(responseBody, "rsp_message");

        TransferRespDto.TransferRespDtoBuilder builder = TransferRespDto.builder()
                .rspCode(rspCode)
                .rspMessage(rspMessage)
                .apiTranId(getStringValue(responseBody, "api_tran_id"))
                .apiTranDtm(getStringValue(responseBody, "api_tran_dtm"));

        // 성공한 경우에만 상세 정보 매핑
        if ("A0000".equals(rspCode)) {
            // res_list에서 첫 번째 결과 추출 (입금이체의 경우)
            List<Map<String, Object>> resList = (List<Map<String, Object>>) responseBody.get("res_list");
            if (resList != null && !resList.isEmpty()) {
                Map<String, Object> firstResult = resList.get(0);
                builder
                        .bankTranId(getStringValue(firstResult, "bank_tran_id"))
                        .bankTranDate(getStringValue(firstResult, "bank_tran_date"))
                        .bankCodeTran(getStringValue(firstResult, "bank_code_tran"))
                        .bankRspCode(getStringValue(firstResult, "bank_rsp_code"))
                        .bankRspMessage(getStringValue(firstResult, "bank_rsp_message"))
                        .fintechUseNum(getStringValue(firstResult, "fintech_use_num"))
                        .accountNum(getStringValue(firstResult, "account_num"))
                        .printContent(getStringValue(firstResult, "print_content"))
                        .tranAmt(getStringValue(firstResult, "tran_amt"))
                        .wdLimitRemainAmt(getStringValue(firstResult, "wd_limit_remain_amt"));
            } else {
                // 출금이체의 경우 직접 매핑
                builder
                        .bankTranId(getStringValue(responseBody, "bank_tran_id"))
                        .bankTranDate(getStringValue(responseBody, "bank_tran_date"))
                        .bankCodeTran(getStringValue(responseBody, "bank_code_tran"))
                        .bankRspCode(getStringValue(responseBody, "bank_rsp_code"))
                        .bankRspMessage(getStringValue(responseBody, "bank_rsp_message"))
                        .fintechUseNum(getStringValue(responseBody, "fintech_use_num"))
                        .accountNum(getStringValue(responseBody, "account_num"))
                        .printContent(getStringValue(responseBody, "print_content"))
                        .tranAmt(getStringValue(responseBody, "tran_amt"))
                        .wdLimitRemainAmt(getStringValue(responseBody, "wd_limit_remain_amt"));
            }
        }

        return builder.build();
    }

    private List<AccountInfoDto.TransactionDTO> createMockTransactions() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                AccountInfoDto.TransactionDTO.builder()
                        .id("TX001")
                        .tran_date(now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .tran_time(now.minusDays(1).format(DateTimeFormatter.ofPattern("HHmmss")))
                        .tran_amt("500000")
                        .inout_type("1") // 1: 입금, 2: 출금
                        .print_content("급여입금")
                        .after_balance_amt("1234567")
                        .build(),
                AccountInfoDto.TransactionDTO.builder()
                        .id("TX002")
                        .tran_date(now.minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .tran_time(now.minusDays(2).format(DateTimeFormatter.ofPattern("HHmmss")))
                        .tran_amt("50000")
                        .inout_type("2")
                        .print_content("온라인쇼핑")
                        .after_balance_amt("734567")
                        .build()
        );
    }

    /**
     * Map에서 String 값을 안전하게 추출하는 헬퍼 메서드
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 계좌번호 마스킹 처리
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }

        // 이미 마스킹된 경우 그대로 반환
        if (accountNumber.contains("*")) {
            return accountNumber;
        }

        // 뒤 4자리만 보이도록 마스킹
        int length = accountNumber.length();
        String masked = "*".repeat(length - 4) + accountNumber.substring(length - 4);

        // 계좌번호 포맷팅 (예: ***-**-1234)
        if (length > 10) {
            return masked.substring(0, 3) + "-" +
                    masked.substring(3, 5) + "-" +
                    masked.substring(5);
        }

        return masked;
    }
}
