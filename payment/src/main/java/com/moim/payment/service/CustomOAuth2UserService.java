package com.moim.payment.service;

import com.moim.payment.config.auth.LoginUsr;
import com.moim.payment.domain.Usr.Provider;
import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.OAuthAttributes;
import com.moim.payment.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsrRepository usrRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("CustomOAuth2UserService.loadUser() 실행: OAuth2 로그인 요청 진입");

        // 부모 클래스의 loadUser 메서드를 호출하여 기본 OAuth2User 객체를 받아옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분 (google, kakao, naver...)
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google", "kakao" 등

        // OAuth2 로그인 진행 시 키가 되는 필드값 (PK)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 서비스별로 응답받은 사용자 정보를 파싱하는 메소드 호출
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        String email = attributes.getEmail();
        String name = attributes.getName();
        String picture = attributes.getPicture();

        Optional<Usr> userOptional = usrRepository.findByEmail(email);

        Usr usr;
        if (userOptional.isPresent()) {
            // 이미 존재하는 사용자인 경우, 이름이나 프로필 사진이 변경되었을 수 있으므로 업데이트
            usr = userOptional.get();
            usr.updateUser(name, email, picture);
            usr.updateSocial(Provider.of(registrationId));
        } else {
            // 새로운 사용자인 경우, DB에 새로 저장
            usr = Usr.builder()
                    .usrname(name)
                    .email(email)
                    .social(true)
                    .provider(Provider.of(registrationId))
                    .picture(picture)
                    .role(UserRole.USER) // 기본 권한 부여
                    .build();
            usrRepository.save(usr);
        }
        log.info("사용자 정보 저장 또는 업데이트 성공. Principal 객체 생성");
        return new LoginUsr(usr, attributes.getAttributes());
    }

}

