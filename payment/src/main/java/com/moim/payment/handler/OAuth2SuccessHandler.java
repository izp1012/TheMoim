package com.moim.payment.handler;

import com.moim.payment.domain.Usr.Provider;
import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.usr.LoginRespDto;
import com.moim.payment.dto.usr.TokenDTO;
import com.moim.payment.repository.UsrRepository;
import com.moim.payment.service.TokenService;
import com.moim.payment.util.CookieUtil;
import jakarta.servlet.ServletException;
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

    private final UsrRepository userRepository;
    private final TokenService tokenService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String targetUrl = determineTargetUrl(request, response, authentication);

        LoginRespDto usrDTO = userRepository.findByEmail(oAuth2User.getAttribute("email").toString())
                .map(user -> new LoginRespDto(user, request.getHeader("Authorization")))
                .orElse(saveNewUser(oAuth2User));    //orElse에 계정저장

        //소셜이 아닌 회원이 이메일로 저장했을 때
        if (!usrDTO.isSocial()) {
            response.sendError(404, "해당 이메일을 가진 회원이 존재합니다.");
            clearAuthenticationAttributes(request, response);
        } else {
            TokenDTO tokenDTO = tokenService.createToken(usrDTO);
            ResponseCookie refreshTokenCookie = ResponseCookie
                    .from("refresh_token", tokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(tokenDTO.getDuration())
                    .path("/")
                    .build();

            response.addHeader("Set-Cookie", refreshTokenCookie.toString());
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl).queryParam("accessToken", tokenDTO.getAccessToken()).build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }


    protected LoginRespDto saveNewUser(OAuth2User oAuth2User) {

        Usr usr = Usr.builder()
                .provider(Provider.of(oAuth2User.getAttribute("provider").toString()))
                .social(true)
                .email(oAuth2User.getAttribute("email"))
                .usrname(oAuth2User.getAttribute("username"))
                .role(UserRole.USER)
                .build();

        userRepository.save(usr);

        return new LoginRespDto(usr);

    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUrl = CookieUtil.getCookie(request, httpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUrl.orElse(getDefaultTargetUrl());

        return UriComponentsBuilder.fromUriString(targetUrl).toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}