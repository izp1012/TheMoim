package com.moim.payment.config.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String BEARER_PREFIX = "Bearer";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.debug("디버그: JWT 필터 요청 URI: {}", requestUri);

        // 로그인 및 회원가입 경로는 JWT 필터를 건너뜁니다.
        if (requestUri.startsWith("/api/login") || requestUri.startsWith("/api/signup") || requestUri.startsWith("/api/v1/auth/oauth2")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        log.debug("authorizationHeader : {}", authorizationHeader);
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtTokenProvider.getUsernameFromToken(jwt);
            } catch (IllegalArgumentException e) {
                log.error("JWT 토큰을 가져올 수 없습니다.");
            } catch (ExpiredJwtException e) {
                log.error("JWT 토큰이 만료되었습니다.");
            }
        } else {
            log.warn("Authorization 헤더가 없거나 Bearer 타입이 아닙니다.");
        }

        jwt = resolveToken(request);

        if(StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(7);    //"Bearer "를 뺀 값, 즉 토큰 값
        }

        return null;
    }
}
