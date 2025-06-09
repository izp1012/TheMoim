package com.moim.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google", "kakao" 등
        attributes.put("provider", provider);

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email" // 기본 키 필드 (등록된 ClientRegistration에 따라 다를 수 있음)
        );
    }
}

