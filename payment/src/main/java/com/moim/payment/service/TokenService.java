package com.moim.payment.service;

import com.moim.payment.config.auth.JwtTokenProvider;
import com.moim.payment.domain.RefreshToken;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.usr.LoginRespDto;
import com.moim.payment.dto.usr.TokenDTO;
import com.moim.payment.handler.exception.CustomApiException;
import com.moim.payment.repository.RefreshTokenRepository;
import com.moim.payment.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsrRepository usrRepository;

    public TokenDTO createToken(LoginRespDto loginRespDto) {
        Usr usr = usrRepository.findByUsrname(loginRespDto.getUsrname())
                .orElseThrow(() -> new RuntimeException("Wrong Access (user does not exist)"));
        return createAndSaveToken(usr);
    }

    public TokenDTO createToken(Usr usr) {
        return createAndSaveToken(usr);
    }

    private TokenDTO createAndSaveToken(Usr usr) {
        TokenDTO tokenDTO = tokenProvider.createTokenReqDto(usr.getUsrname(), usr.getRole());

        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUsr(usr);

        if (existingRefreshToken.isPresent()) {
            RefreshToken refreshToken = existingRefreshToken.get();
            refreshToken.updateValue(tokenDTO.getRefreshToken());
            refreshTokenRepository.save(refreshToken);
        } else {
            RefreshToken refreshToken = RefreshToken.builder()
                    .usr(usr)
                    .token(tokenDTO.getRefreshToken())
                    .build();
            refreshTokenRepository.save(refreshToken);
        }

        return tokenDTO;
    }

    public TokenDTO refresh(TokenDTO tokenDTO) {
        //1. 토큰 유효성 검증
        if(!tokenProvider.validateToken(tokenDTO.getRefreshToken())) {
            throw new CustomApiException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. Access Token에서 사용자명(principal) 추출
        Authentication authentication = tokenProvider.getAuthentication(tokenDTO.getAccessToken());
        String username = authentication.getName();

        // 3. DB에서 사용자 정보와 리프레시 토큰 조회
        Usr usr = usrRepository.findByUsrname(username)
                .orElseThrow(() -> new CustomApiException("사용자 정보가 존재하지 않습니다."));

        RefreshToken refreshToken = refreshTokenRepository.findByUsr(usr)
                .orElseThrow(() -> new CustomApiException("로그아웃 된 사용자입니다."));

        // 4. 요청의 Refresh Token과 DB에 저장된 토큰이 일치하는지 확인
        if (!refreshToken.getToken().equals(tokenDTO.getRefreshToken())) {
            // 토큰이 일치하지 않는 경우, 탈취로 간주하고 DB의 리프레시 토큰을 삭제
            refreshTokenRepository.delete(refreshToken);
            throw new CustomApiException("Refresh Token이 일치하지 않습니다. (재로그인 필요)");
        }

        // 5. 새로운 Access Token과 Refresh Token 발급
        TokenDTO newTokenDto = tokenProvider.createTokenReqDto(usr.getUsrname(), usr.getRole());

        // 6. DB에 저장된 기존 Refresh Token을 새로운 토큰으로 업데이트하고 저장
        refreshToken.updateValue(newTokenDto.getRefreshToken());
        refreshTokenRepository.save(refreshToken);

        return newTokenDto;
    }
}