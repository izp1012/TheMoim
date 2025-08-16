package com.moim.payment.service;

import com.moim.payment.domain.Usr.Provider;
import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsrRepository usrRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 loadUser 메서드를 호출하여 기본 OAuth2User 객체를 받아옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        //provider 정보를 제공하기 위한 수정가능한 Map 객체로 복사
        Map<String, Object> mutableAttributes = new HashMap<>(attributes);
        
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google", "kakao" 등
        mutableAttributes.put("provider", provider);

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
//        String picture = (String) attributes.get("picture");

        Optional<Usr> userOptional = usrRepository.findByEmail(email);

        Usr usr;
        if (userOptional.isPresent()) {
            // 이미 존재하는 사용자인 경우, 이름이나 프로필 사진이 변경되었을 수 있으므로 업데이트
            usr = userOptional.get();
            usr.updateUser(name, email);
            usr.updateSocial(Provider.of(provider));
        } else {
            // 새로운 사용자인 경우, DB에 새로 저장
            usr = Usr.builder()
                    .usrname(name)
                    .email(email)
                    .social(true)
                    .provider(Provider.of(provider))
//                    .picture(picture)
                    .role(UserRole.USER) // 기본 권한 부여
                    .build();
            usrRepository.save(usr);
        }

        // Spring Security가 인증 처리에 사용할 수 있도록 DefaultOAuth2User 객체를 생성하여 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(usr.getRole().getType())),
                mutableAttributes,
                "email" // 사용자 정보의 key가 되는 필드 이름
        );
    }
}

