package com.moim.payment.service;

import com.moim.payment.dto.kftc.AccountInfoDto;
import com.moim.payment.dto.kftc.KftcAccountInfoResp;
import com.moim.payment.dto.kftc.KftcTokenResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
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

    @Value("${kftc.user-info-endpoint}")
    private String userInfoEndpoint;

    @Value("${kftc.account-api-endpoint}")
    private String accountApiEndpoint;

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
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("KFTC Access Token 교환 실패: HTTP Status={}, Error Body={}",
                                                clientResponse.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException("KFTC Access Token 교환 실패: " + errorBody));
                                    })
                    )
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
     * Access Token을 사용하여 계좌 목록을 조회합니다.
     */
    public List<AccountInfoDto.AccountSummary> getAccountList(String accessToken, String userSeqNo) {
        log.info("계좌 목록 조회 요청 시작: userSeqNo={}", userSeqNo);

        String bankTranId = generateBankTranId("T990");
        String tranDtime = getCurrentTimestamp();

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("bank_tran_id", bankTranId);
        requestBody.add("user_seq_no", userSeqNo != null ? userSeqNo : "1100000001");
        requestBody.add("include_cancel_yn", "Y");
        requestBody.add("sort_order", "D");
        requestBody.add("tran_dtime", tranDtime);

        try {
            // 실제 금융결재원 API 호출 로직을 여기에 구현
            // 현재는 테스트용 가상 데이터 반환
            return createMockAccountList();

        } catch (Exception e) {
            log.error("계좌 목록 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("계좌 목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 특정 계좌의 잔액을 조회합니다.
     */
    public String getAccountBalance(String accessToken, String fintechUseNum) {
        log.info("계좌 잔액 조회 요청: fintechUseNum={}", fintechUseNum);

        String bankTranId = generateBankTranId("T991");
        String tranDtime = getCurrentTimestamp();

        try {
            // 실제 금융결재원 잔액 조회 API 호출 로직
            // 현재는 테스트용 가상 데이터 반환
            return "1234567";

        } catch (Exception e) {
            log.error("잔액 조회 실패: {}", e.getMessage(), e);
            return "0";
        }
    }

    /**
     * 특정 계좌의 거래 내역을 조회합니다.
     */
    public List<AccountInfoDto.TransactionDTO> getTransactionHistory(String accessToken, String fintechUseNum, int days) {
        log.info("거래 내역 조회 요청: fintechUseNum={}, days={}", fintechUseNum, days);

        String bankTranId = generateBankTranId("T992");
        String tranDtime = getCurrentTimestamp();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusDays(days);

        String fromDateStr = fromDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String toDateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        try {
            // 실제 금융결재원 거래내역 조회 API 호출 로직
            // 현재는 테스트용 가상 데이터 반환
            return createMockTransactions();

        } catch (Exception e) {
            log.error("거래 내역 조회 실패: {}", e.getMessage(), e);
            return List.of();
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

            // 2. 잔액 조회
            String balance = getAccountBalance(accessToken, primaryAccount.getFintechUseNum());

            // 3. 거래 내역 조회 (최근 7일)
            List<AccountInfoDto.TransactionDTO> transactions = getTransactionHistory(
                    accessToken, primaryAccount.getFintechUseNum(), 7);

            // 4. 통합 정보 구성
            return AccountInfoDto.builder()
                    .bankName(primaryAccount.getBankName())
                    .accountNumber(primaryAccount.getAccountNumber())
                    .ownerName(primaryAccount.getOwnerName())
                    .balance(balance)
                    .ownerName(primaryAccount.getFintechUseNum())
                    .transactions(transactions)
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
         String fullAccountApiUri = userInfoEndpoint + "?user_seq_no=" + userSeqNo; // 예시

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
        return prefix + System.currentTimeMillis();
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // 테스트용 가상 데이터 생성 메서드들
    private List<AccountInfoDto.AccountSummary> createMockAccountList() {
        return List.of(
                AccountInfoDto.AccountSummary.builder()
                        .bankName("테스트은행")
                        .accountNumber("123-456-****")
                        .ownerName("홍길동")
                        .fintechUseNum("FINTECH123456789")
                        .build()
        );
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
}
