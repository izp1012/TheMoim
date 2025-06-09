package com.moim.payment.config.auth;//package com.moim.payment.config.auth;

import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.dto.user.TokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Component
@Log4j2
public class JwtTokenProvider {

    private String secretKey = JwtVO.SECRET;
    private SecretKey key;
    private static final String BEARER_TYPE = "Bearer";
    private static final Long accessTokenValidationTime = 30 * 60 * 1000L;   //30분
    private static final Long refreshTokenValidationTime = 7 * 24 * 60 * 60 * 1000L;  //7일

    @PostConstruct
    protected void init() {
        // Plain String을 바로 byte[]로 변환하여 SecretKey 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * accessToken과 refreshToken을 생성함
     * @param subject
     * @return TokenDTO
     * subject는 Form Login방식의 경우 userId, Social Login방식의 경우 email
     */
    public TokenDTO createTokenReqDto(String subject, UserRole role) {

        Instant now = Instant.from(OffsetDateTime.now());

        // accessToken 만료 시간은 accessTokenValidationTime으로 설정
        Instant accessTokenExpirationDate = now.plusMillis(accessTokenValidationTime);
        // refreshToken 만료 시간은 refreshTokenValidationTime으로 설정
        Instant refreshTokenExpirationDate = now.plusMillis(refreshTokenValidationTime);

        //accessToken 생성
        String accessToken = Jwts.builder()
                .subject(subject)
                .claim("roles", role.name())
                .expiration(Date.from(refreshTokenExpirationDate))
                .signWith(key)
                .compact();
        log.debug("디버그: Access Token 생성됨 (Subject: {}, Role: {}, Expires: {})", subject, role.name(), accessTokenExpirationDate);


        //refreshToken 생성
        String refreshToken = Jwts.builder()
                .expiration(Date.from(now.plusMillis(refreshTokenValidationTime)))
                .signWith(key)
                .compact();
        log.debug("디버그: Refresh Token 생성됨 (Expires: {})", refreshTokenExpirationDate);


        //TokenDTO에 두 토큰을 담아서 반환
        return TokenDTO.builder()
                .tokenType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .duration(Duration.ofMillis(accessTokenValidationTime))
                .build();
    }

    /**
     * Access Token을 파싱하여 Authentication 객체를 반환
     * @param accessToken JWT Access Token
     * @return Authentication 객체
     * @throws ExpiredJwtException 토큰이 만료되었을 때
     * @throws RuntimeException 권한 정보가 없는 토큰일 때
     */
    public Authentication getAuthentication(String accessToken) throws ExpiredJwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        if (claims.get("roles") == null) {
            log.warn("경고: 권한 정보가 없는 토큰입니다. Subject: {}", claims.getSubject());
            throw new RuntimeException("권한정보가 없는 토큰입니다.");
        }

        // 단일 역할만 저장하는 경우
        // claims.get("roles")는 UserRole.ADMIN.name() 같은 단일 문자열이므로 split(',') 필요 없음
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(claims.get("roles").toString())
        );

        // UserDetails 객체 생성 (비밀번호는 인증 후이므로 빈 문자열)
        UserDetails user = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }


    /**
     * JWT 토큰의 유효성을 검증
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            log.debug("디버그: JWT 토큰 유효성 검증 성공.");
            return true;
        } catch (SignatureException e) { // io.jsonwebtoken.security.SecurityException 대신 SignatureException 사용
            log.info("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            log.info("잘못된 형식의 JWT 토큰입니다."); // MalformedJwtException 추가
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었거나 비어 있습니다.");
        }
        return false;
    }

    /**
     * 토큰에서 사용자 이름을 추출
     * @param token JWT 토큰 문자열
     * @return 토큰의 subject (사용자 이름)
     * @throws JwtException 토큰 파싱 실패 또는 유효하지 않은 경우
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}