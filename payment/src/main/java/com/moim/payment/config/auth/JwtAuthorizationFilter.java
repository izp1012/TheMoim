package com.moim.payment.config.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader(JwtVO.HEADER_STRING);

        // 1. JWT가 존재하지 않으면 필터 체인 계속 진행
        if (token == null || !token.startsWith(JwtVO.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. "Bearer " 제거 후 검증
            String jwtToken = token.replace(JwtVO.TOKEN_PREFIX, "").trim();

            // 3. 검증과 인증 객체 생성을 동시에 처리 (getAuthentication 기능)
            Authentication authentication = jwtProvider.getAuthentication(jwtToken);

            // 4. 성공적으로 끝나면 SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            // 5. 토큰 검증 과정에서 발생하는 모든 JwtException을 여기서 잡지 않고, SecurityConfig의 exceptionHandling에서 처리
            logger.warn("JWT 처리 중 오류 발생: "+ e.getMessage());
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}