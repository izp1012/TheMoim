package com.moim.payment.controller;

import com.moim.payment.config.auth.LoginUsr;
import com.moim.payment.dto.ResponseDto;
import com.moim.payment.dto.user.*;
import com.moim.payment.service.UsrService;
import com.moim.payment.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
@Slf4j
@Tag(name = "사용자 CRUD", description = "사용자 CRUD 관련 API")
public class UsrController {
    private final UsrService usrService;

    @PostMapping("/auth/join")
    public ResponseEntity<?> join(@RequestBody @Valid JoinReqDto joinReqDto){
        JoinRespDto joinRespDto = usrService.join(joinReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), joinRespDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login2(HttpServletResponse response, @RequestBody @Valid LoginReqDto loginReqDto){
        TokenDTO tokenDTO = usrService.login(loginReqDto, response);

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", tokenDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(tokenDTO.getDuration())
                .path("/")
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString()); // Set-Cookie 헤더에 쿠키 추가

        TokenRespDto tokenResponseDTO = TokenRespDto.builder()
                .isNewMember(false)
                .accessToken(tokenDTO.getAccessToken())
                .build();
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), tokenResponseDTO), HttpStatus.OK);
    }

    @GetMapping("/auth/get-current-member")
    public UsrRespDto getCurrentMember(Authentication authentication){
        String userId = ((LoginUsr) authentication.getPrincipal()).getUsr().getUsrId();
        log.debug("authentication.getName() : " + userId);
        return usrService.findUsrById(userId);
    }
}
