package com.moim.payment.controller;

import com.moim.payment.dto.kftc.*;
import com.moim.payment.service.KftcApiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private final KftcApiService kftcApiService;

    @PostMapping("/token")
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody KftcApiReq request) {
        log.info("토큰 발급 요청: code={}, redirect_uri={}", request.getCode(), request.getRedirect_uri());

        // 입력값 검증
        if (StringUtils.isBlank(request.getCode())) {
            log.warn("인가 코드가 누락되었습니다.");
            return ResponseEntity.badRequest().body(createErrorResponse(
                    "MISSING_CODE", "인가 코드가 누락되었습니다."));
        }

        if (StringUtils.isBlank(request.getRedirect_uri())) {
            log.warn("리다이렉트 URI가 누락되었습니다.");
            return ResponseEntity.badRequest().body(createErrorResponse(
                    "MISSING_REDIRECT_URI", "리다이렉트 URI가 누락되었습니다."));
        }

        try {
            KftcTokenResp tokenResp = kftcApiService.exchangeCodeForToken(
                    request.getCode(),
                    request.getRedirect_uri()
            );
            if (tokenResp == null || StringUtils.isBlank(tokenResp.getAccessToken())) {
                log.error("Access Token 교환 실패 - 응답이 null이거나 토큰이 없음");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("TOKEN_EXCHANGE_FAILED", "토큰 교환에 실패했습니다."));
            }

            log.info("토큰 교환 성공 - userSeqNo: {}", tokenResp.getUserSeqNo());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", tokenResp.getAccessToken());
            response.put("userSeqNo", tokenResp.getUserSeqNo());
            response.put("tokenType", tokenResp.getTokenType());
            response.put("expiresIn", tokenResp.getExpiresIn());
            response.put("scope", tokenResp.getScope());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("KFTC 토큰 교환 중 런타임 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("KFTC_API_ERROR", "금융결재원 API 오류: " + e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 교환 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("UNEXPECTED_ERROR", "예상치 못한 오류가 발생했습니다."));
        }
    }

    @PostMapping("/account-info")
    public ResponseEntity<?> getAccountInfo(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        String userSeqNo = request.get("userSeqNo");

        log.info("계좌 정보 조회 요청: userSeqNo={}", userSeqNo);

        if (StringUtils.isBlank(accessToken)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("MISSING_TOKEN", "액세스 토큰이 누락되었습니다."));
        }

        if (StringUtils.isBlank(userSeqNo)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("MISSING_USER_SEQ", "사용자 일련번호가 누락되었습니다."));
        }

        try {
            AccountInfoDto accountInfo = kftcApiService.getComprehensiveAccountInfo(accessToken, userSeqNo);

            log.info("계좌 정보 조회 성공: bankName={}, accountNumber={}",
                    accountInfo.getBankName(), accountInfo.getAccountNumber());

            return ResponseEntity.ok(accountInfo);

        } catch (RuntimeException e) {
            log.error("계좌 정보 조회 중 런타임 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("ACCOUNT_INFO_ERROR", "계좌 정보 조회 실패: " + e.getMessage()));
        } catch (Exception e) {
            log.error("계좌 정보 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("UNEXPECTED_ERROR", "예상치 못한 오류가 발생했습니다."));
        }
    }

    /**
     * 금융결재원 인가 코드를 받아 토큰 교환 및 계좌 정보 조회를 수행합니다.
     * 이 엔드포인트는 프론트엔드에서 호출됩니다.
     * POST /api/kftc/token-exchange
     * @param request 인가 코드와 리다이렉트 URI를 포함하는 요청 본문
     * @return 계좌 정보 DTO 또는 오류 응답
     */
    @PostMapping("/token-exchange")
    public ResponseEntity<?> exchangeTokenAndGetAccountInfo(@RequestBody KftcApiReq request) {
        log.info("통합 토큰 교환 및 계좌 정보 조회 요청: code={}, redirect_uri={}",
                request.getCode(), request.getRedirect_uri());

        // 입력값 검증
        if (StringUtils.isBlank(request.getCode()) || StringUtils.isBlank(request.getRedirect_uri())) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("INVALID_REQUEST", "인가 코드 또는 리다이렉트 URI가 누락되었습니다."));
        }

        try {
            log.info("금융결재원 토큰 교환 시작...");
            
            // 1. 인가 코드를 사용하여 Access Token 교환
            KftcTokenResp tokenResponse = kftcApiService.exchangeCodeForToken(
                    request.getCode(),
                    request.getRedirect_uri()
            );

            // 2. 계좌 정보 조회
            AccountInfoDto accountInfo = kftcApiService.getComprehensiveAccountInfo(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getUserSeqNo()
            );

            log.info("통합 처리 성공: bankName={}, accountNumber={}",
                    accountInfo.getBankName(), accountInfo.getAccountNumber());

            return ResponseEntity.ok(accountInfo);

        } catch (RuntimeException e) {
            log.error("통합 처리 중 런타임 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTEGRATION_ERROR", "통합 처리 실패: " + e.getMessage()));
        } catch (Exception e) {
            log.error("통합 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("UNEXPECTED_ERROR", "예상치 못한 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 계좌의 최신 잔액 조회 API
     * POST /api/kftc/balance
     */
    @PostMapping("/balance")
    public ResponseEntity<?> getAccountBalance(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        String fintechUseNum = request.get("fintechUseNum");

        if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(fintechUseNum)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("MISSING_PARAMS", "필수 파라미터가 누락되었습니다."));
        }

        try {
            String balance = kftcApiService.getAccountBalance(accessToken, fintechUseNum);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("balance", balance);
            response.put("balanceFormatted", formatAmount(balance));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("잔액 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("BALANCE_ERROR", "잔액 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "KFTC API Controller is running");
        return ResponseEntity.ok(response);
    }

    /**
     * 출금이체 (송금)
     */
    @PostMapping("/transfer/withdraw")
    public ResponseEntity<?> withdrawTransfer(@RequestBody TransferReqDto request,
                                              HttpServletRequest httpRequest) {
        log.info("출금이체 요청: fintechUseNum={}, amount={}",
                request.getFintechUseNum(), request.getTranAmt());

        try {
            // JWT 토큰에서 액세스 토큰 추출
            String accessToken = extractAccessTokenFromRequest(httpRequest);

            TransferRespDto response = kftcApiService.processWithdrawTransfer(accessToken, request);

            log.info("출금이체 성공: bankTranId={}", response.getBankTranId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("출금이체 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("WITHDRAW_TRANSFER_ERROR", "출금이체 처리 실패: " + e.getMessage()));
        }
    }

    /**
     * 입금이체 (수취)
     */
    @PostMapping("/transfer/deposit")
    public ResponseEntity<?> depositTransfer(@RequestBody TransferReqDto request,
                                             HttpServletRequest httpRequest) {
        log.info("입금이체 요청: fintechUseNum={}, amount={}",
                request.getFintechUseNum(), request.getTranAmt());

        try {
            // JWT 토큰에서 액세스 토큰 추출
            String accessToken = extractAccessTokenFromRequest(httpRequest);

            TransferRespDto response = kftcApiService.processDepositTransfer(accessToken, request);

            log.info("입금이체 성공: bankTranId={}", response.getBankTranId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("입금이체 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DEPOSIT_TRANSFER_ERROR", "입금이체 처리 실패: " + e.getMessage()));
        }
    }

    private String extractAccessTokenFromRequest(HttpServletRequest request) {
        // JWT 토큰에서 KFTC 액세스 토큰을 추출하는 로직
        // 실제 구현은 토큰 저장 방식에 따라 달라집니다
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // JWT 토큰을 파싱하여 KFTC 액세스 토큰 추출
            // 또는 세션/데이터베이스에서 조회
            return "extracted_kftc_access_token";
        }
        throw new RuntimeException("액세스 토큰을 찾을 수 없습니다.");
    }

    // 유틸리티 메서드들
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    private String formatAmount(String amount) {
        if (StringUtils.isBlank(amount)) return "0원";
        try {
            long numAmount = Long.parseLong(amount);
            return String.format("%,d원", numAmount);
        } catch (NumberFormatException e) {
            return amount + "원";
        }
    }
}
