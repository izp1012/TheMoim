package com.moim.payment.handler;

import com.moim.payment.dto.ResponseDto; // 기존에 사용하시던 응답 DTO
import com.moim.payment.exception.CustomApiException; // 직접 만드신 예외
import com.moim.payment.util.CustomDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice // 모든 @RestController에서 발생하는 예외를 처리
public class GlobalExceptionHandler {

    // CustomApiException 타입의 예외가 발생하면 이 메소드가 처리
    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<?> handleCustomApiException(CustomApiException e) {
        return new ResponseEntity<>(
                new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), // CustomDateUtil 등 필요하면 추가
                HttpStatus.CONFLICT
        );
    }

    /**
     * @Valid 유효성 검증 실패 시 발생하는 예외 처리
     * (예: DTO의 필드가 @NotBlank, @Email 등의 조건을 만족하지 못할 때)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.error("ValidationException 발생: {}", e.getMessage());

        // 어떤 필드가 어떤 이유로 실패했는지 상세 정보를 담아 응답
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(
                new ResponseDto<>(-1, "입력 값 유효성 검사에 실패했습니다.", CustomDateUtil.toStringFormat(LocalDateTime.now()), null),
                HttpStatus.BAD_REQUEST // 유효성 검증 실패는 400 Bad Request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        return new ResponseEntity<>(
                new ResponseDto<>(-1, "서버 내부 오류가 발생했습니다: " + e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}