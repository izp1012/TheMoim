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
import reactor.core.publisher.Mono;

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

        // Basic 인증 헤더 생성
        String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes()
        );

        return webClient.post()
                .uri(tokenEndpoint)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("KFTC Access Token 교환 실패: HTTP Status={}, Error Body={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("KFTC Access Token 교환 실패: " + errorBody));
                                })
                )
                .bodyToMono(KftcTokenResp.class)
                .block(); // 블로킹 방식으로 Mono 결과를 기다립니다 (비동기 처리 시에는 block() 제거)
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
        // String fullAccountApiUri = accountApiEndpoint + "?user_seq_no=" + userSeqNo; // 예시

        KftcAccountInfoResp accountResponse = webClient.get()
                .uri(accountApiEndpoint) // 또는 fullAccountApiUri
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
                                        .date(tx.getDate())
                                        .description(tx.getDescription())
                                        .amount(tx.getAmount())
                                        .build())
                                .collect(Collectors.toList()) :
                        // 가상 거래 내역
                        List.of(
                                AccountInfoDto.TransactionDTO.builder().id("1").date("2024-06-10").description("급여 입금").amount("+1,000,000원").build(),
                                AccountInfoDto.TransactionDTO.builder().id("2").date("2024-06-11").description("온라인 쇼핑").amount("-50,000원").build()
                        )
                )
                .build();
    }
}
