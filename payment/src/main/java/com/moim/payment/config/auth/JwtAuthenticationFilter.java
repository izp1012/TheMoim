package com.moim.payment.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moim.payment.dto.usr.LoginReqDto;
import com.moim.payment.dto.usr.LoginRespDto;
import com.moim.payment.dto.usr.TokenDTO;
import com.moim.payment.service.TokenService;
import com.moim.payment.util.CustomUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TokenService tokenService;
    private AuthenticationManager authenticationManager;


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, TokenService tokenService) {
        super(authenticationManager);
        this.tokenService = tokenService;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        logger.debug("디버그 : attemptAuthentication 호출됨");

        try{
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            //강제 로그인
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginReqDto.getUsrname(), loginReqDto.getPassword());

            logger.debug("AuthenticationManager에 인증 위임: {}", authenticationToken);

            return getAuthenticationManager().authenticate(authenticationToken);

        } catch (IOException e) {
            logger.error("JSON 처리 중 오류", e);
            throw new InternalAuthenticationServiceException("요청 처리 중 오류가 발생했습니다", e);
        }
    }

    //return authtication 잘 작동하면 successfulAuthentication 메서드 호출된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        logger.debug("디버그 : successfulAuthentication 호출됨");

        LoginUsr loginUser = (LoginUsr) authResult.getPrincipal();
        TokenDTO tokenDTO = tokenService.createToken(new LoginRespDto(loginUser.getUsr()));

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", tokenDTO.getRefreshToken())
                .httpOnly(true)
                //개발
                .secure(false)
                .sameSite("Lax")
                //운영
//                .secure(true)
//                .sameSite("None")
                .maxAge(tokenDTO.getDuration())
                .path("/")
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString()); // Set-Cookie 헤더에 쿠키 추가
        logger.debug("Refresh Token 쿠키 설정 완료: {}", responseCookie);

        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUsr(), tokenDTO.getAccessToken());

        CustomUtil.success(response, "Login", "Login 성공", loginRespDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed){
        logger.debug("디버그: unsuccessfulAuthentication 호출");
        String errorMessage = switch (failed) {
            case UsernameNotFoundException usernameNotFoundException ->
                // 계정을 찾을 수 없을 때 발생하는 예외
                    "존재하지 않는 계정입니다.";
            case BadCredentialsException badCredentialsException ->
                // 비밀번호가 일치하지 않을 때 발생하는 예외
                    "비밀번호가 일치하지 않습니다.";
            case InternalAuthenticationServiceException internalAuthenticationServiceException ->
                // 내부 시스템 문제로 인증 처리를 할 수 없는 경우 발생하는 예외
                    "내부 시스템 오류로 인해 로그인할 수 없습니다. 관리자에게 문의하세요.";
            case null, default ->
                // 기타 인증 관련 예외 처리
                    "알 수 없는 이유로 로그인에 실패했습니다.";
        };

        logger.error("로그인 실패: {}", errorMessage, failed); // 실패 원인과 함께 전체 예외 로그를 남기는 것이 좋음

        CustomUtil.fail(response, "Login Failed", errorMessage, HttpStatus.UNAUTHORIZED);
    }
}
