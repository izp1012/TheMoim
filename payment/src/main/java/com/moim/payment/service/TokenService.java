package com.moim.payment.service;

import com.moim.payment.config.auth.JwtTokenProvider;
import com.moim.payment.domain.RefreshToken;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.user.LoginRespDto;
import com.moim.payment.dto.user.TokenDTO;
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
        TokenDTO tokenDTO = tokenProvider.createTokenReqDto(loginRespDto.getUsrId(), loginRespDto.getRole());
        Usr usr = usrRepository.findByUsrId(loginRespDto.getUsrId()).orElseThrow(()
                -> new RuntimeException("Wrong Access (user does not exist)"));

        // 기존 리프레시 토큰이 있는지 확인
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUsr(usr);

        if (existingRefreshToken.isPresent()) {
            // 기존 토큰이 있다면 업데이트
            RefreshToken refreshToken = existingRefreshToken.get();
            refreshToken.updateValue(tokenDTO.getRefreshToken());
            refreshTokenRepository.save(refreshToken);
        } else {
            // 기존 토큰이 없다면 새로 생성
            RefreshToken refreshToken = RefreshToken.builder()
                    .usr(usr)
                    .token(tokenDTO.getRefreshToken())
                    .build();
            refreshTokenRepository.save(refreshToken);
        }

        return tokenDTO;
    }

    public TokenDTO createToken(Usr usr) {

        TokenDTO tokenDTO = tokenProvider.createTokenReqDto(usr.getUsrId(), usr.getRole());

        // 기존 리프레시 토큰이 있는지 확인
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUsr(usr);

        if (existingRefreshToken.isPresent()) {
            // 기존 토큰이 있다면 업데이트
            RefreshToken refreshToken = existingRefreshToken.get();
            refreshToken.updateValue(tokenDTO.getRefreshToken());
            refreshTokenRepository.save(refreshToken);
        } else {
            // 기존 토큰이 없다면 새로 생성
            RefreshToken refreshToken = RefreshToken.builder()
                    .usr(usr)
                    .token(tokenDTO.getRefreshToken())
                    .build();
            refreshTokenRepository.save(refreshToken);
        }

        return tokenDTO;
    }

    public TokenDTO refresh(TokenDTO tokenDTO) {
        if(!tokenProvider.validateToken(tokenDTO.getRefreshToken())) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        Authentication authentication = tokenProvider.getAuthentication(tokenDTO.getAccessToken());

        RefreshToken refreshToken = refreshTokenRepository.findByUsr(usrRepository.findByUsrId(authentication.getName()).get())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        if (!refreshToken.getToken().equals(tokenDTO.getRefreshToken())) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }

        Usr user = usrRepository.findByUsrId(refreshToken.getUsr().getUsrId()).orElseThrow(() -> new RuntimeException("존재하지 않는 계정입니다."));
        TokenDTO tokenDto = tokenProvider.createTokenReqDto(user.getUsrId(), user.getRole());

        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        return tokenDto;
    }
}