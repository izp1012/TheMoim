package com.moim.payment.controller;

import com.moim.payment.dto.kftc.AccountInfoDto;
import com.moim.payment.dto.kftc.KftcApiReq;
import com.moim.payment.dto.kftc.KftcTokenResp;
import com.moim.payment.service.KftcApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/kftc")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class KftcApiController {

    private static final Logger log = LoggerFactory.getLogger(KftcApiController.class);
    private final KftcApiService kftcApiService;

    /**
     * 금융결재원 인가 코드를 받아 토큰 교환 및 계좌 정보 조회를 수행합니다.
     * 이 엔드포인트는 프론트엔드에서 호출됩니다.
     * POST /api/kftc/token-exchange
     * @param request 인가 코드와 리다이렉트 URI를 포함하는 요청 본문
     * @return 계좌 정보 DTO 또는 오류 응답
     */
    @PostMapping("/token-exchange")
    public ResponseEntity<?> exchangeTokenAndGetAccountInfo(@RequestBody KftcApiReq request) {
        log.info("프론트엔드로부터 토큰 교환 요청 수신: code={}, redirect_uri={}", 
                request.getCode(), request.getRedirect_uri());

        // 입력값 검증
        if (request.getCode() == null || request.getCode().isEmpty()) {
            log.warn("인가 코드가 누락되었습니다.");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authorization code is missing.");
            errorResponse.put("message", "인가 코드가 누락되었습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (request.getRedirect_uri() == null || request.getRedirect_uri().isEmpty()) {
            log.warn("리다이렉트 URI가 누락되었습니다.");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Redirect URI is missing.");
            errorResponse.put("message", "리다이렉트 URI가 누락되었습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            log.info("금융결재원 토큰 교환 시작...");
            
            // 1. 인가 코드를 사용하여 Access Token 교환
            KftcTokenResp tokenResponse = kftcApiService.exchangeCodeForToken(
                    request.getCode(),
                    request.getRedirect_uri()
            );

            // Access Token이 없으면 에러 처리
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                log.error("Access Token 교환에 실패했습니다. KFTC 응답: {}", tokenResponse);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to obtain Access Token from KFTC.");
                errorResponse.put("message", "금융결재원에서 액세스 토큰을 받아오는데 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            log.info("Access Token 획득 성공. userSeqNo: {}", tokenResponse.getUserSeqNo());

            // 2. 획득한 Access Token과 사용자 일련번호를 사용하여 계좌 정보 조회
            log.info("계좌 정보 조회 시작...");
            AccountInfoDto accountInfo = kftcApiService.getAccountInfo(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getUserSeqNo()
            );

            log.info("계좌 정보 조회 성공: bankName={}, accountNumber={}", 
                    accountInfo.getBankName(), accountInfo.getAccountNumber());
            
            return ResponseEntity.ok(accountInfo);

        } catch (RuntimeException e) {
            log.error("금융결재원 API 연동 중 오류 발생: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error during KFTC API integration: " + e.getMessage());
            errorResponse.put("message", "금융결재원 API 연동 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unexpected error occurred.");
            errorResponse.put("message", "예상치 못한 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/token")
    public ResponseEntity<?> getKftcToken(@RequestBody KftcApiReq request) {
        log.info("토큰 발급 요청: code={}, redirect_uri={}", request.getCode(), request.getRedirect_uri());

        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authorization code is missing."));
        }
        if (request.getRedirect_uri() == null || request.getRedirect_uri().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Redirect URI is missing."));
        }

        try {
            KftcTokenResp tokenResp = kftcApiService.exchangeCodeForToken(
                request.getCode(),
                request.getRedirect_uri()
            );
            if (tokenResp == null || tokenResp.getAccessToken() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to obtain Access Token from KFTC."));
            }
            return ResponseEntity.ok(tokenResp);
        } catch (Exception e) {
            log.error("KFTC 토큰 발급 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "KFTC token issue: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "KFTC API Controller is running");
        return ResponseEntity.ok(response);
    }
}
