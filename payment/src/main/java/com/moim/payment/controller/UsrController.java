package com.moim.payment.controller;

import com.moim.payment.config.auth.LoginUsr;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.ResponseDto;
import com.moim.payment.dto.usr.*;
import com.moim.payment.exception.CustomApiException;
import com.moim.payment.service.UsrService;
import com.moim.payment.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
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

    @PostMapping("/signup")
    public ResponseEntity<?> join(@RequestBody @Valid SignUpReqDto signUpReqDto){
        SignUpRespDto signUpRespDto = usrService.signup(signUpReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), signUpRespDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletResponse response, @RequestBody @Valid LoginReqDto loginReqDto){
        try {
            TokenDTO tokenDTO = usrService.login(loginReqDto);

            Usr usr = usrService.findUserbyUsername(loginReqDto.getUsrname());
            LoginRespDto loginRespDto = new LoginRespDto(usr);

            ResponseCookie responseCookie = ResponseCookie
                    .from("refresh_token", tokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(tokenDTO.getDuration())
                    .path("/")
                    .build();

            response.addHeader("Set-Cookie", responseCookie.toString()); // Set-Cookie 헤더에 쿠키 추가

            loginRespDto.setJwtToken(tokenDTO.getAccessToken());

            return new ResponseEntity<>(new ResponseDto<>(1, "로그인 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), loginRespDto), HttpStatus.OK);
        } catch (CustomApiException e){
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/auth/get-current-member")
    public UsrRespDto getCurrentMember(Authentication authentication){
        String userId = ((LoginUsr) authentication.getPrincipal()).getUsr().getUsrname();
        log.debug("authentication.getName() : " + userId);
        return usrService.findUsrById(userId);
    }
}
