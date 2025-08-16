package com.moim.payment.handler;

import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.usr.TokenDTO;
import com.moim.payment.repository.UsrRepository;
import com.moim.payment.service.TokenService;
import com.moim.payment.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsrRepository usrRepository;
    private final TokenService tokenService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 1. UserService에서 처리 완료된 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        // 2. DB에서 최신 정보를 다시 조회.
        Usr usr = usrRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 인증 후 사용자를 찾을 수 없습니다."));

        log.info("OAuth2 로그인 성공. 사용자: {}", usr.getEmail());


        // 3. JWT 토큰생성
        TokenDTO tokenDTO = tokenService.createToken(usr);

        // 4. 리프레시 토큰을 담은 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie
                    .from("refresh_token", tokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(tokenDTO.getDuration())
                    .path("/")
                    .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // 5. 최종 리디렉션 URL 결정 및 토큰 추가
        String targetUrl = determineTargetUrl(request, response, authentication);
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl) // 쿠키에 저장된 redirect_uri 사용
                .queryParam("accessToken", tokenDTO.getAccessToken())
                .build().toUriString();

        // 6. 리디렉션 및 쿠키 정리
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }


    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        // 쿠키에 저장된 리디렉션 URI가 없으면 기본값으로 설정
        return redirectUri.orElse("http://localhost:3000/oauth/callback");
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}