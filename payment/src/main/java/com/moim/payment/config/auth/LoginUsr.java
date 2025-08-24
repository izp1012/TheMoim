package com.moim.payment.config.auth;

import com.moim.payment.domain.Usr.Usr;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Spring Security의 Principal 객체 역할을 하는 통합 클래스.
 * 일반 로그인을 위한 UserDetails와 소셜 로그인을 위한 OAuth2User를 모두 구현.
 */
@RequiredArgsConstructor
@Getter
public class LoginUsr implements UserDetails, OAuth2User {
    private final Usr usr;
    private Map<String, Object> attributes;

    // 소셜 로그인 시 사용하는 생성자
    public LoginUsr(Usr usr, Map<String, Object> attributes) {
        this.usr = usr;
        this.attributes = attributes;
    }

    // --- UserDetails 인터페이스 구현 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> usr.getRole().name());
    }

    @Override
    public String getPassword() {
        return usr.getPassword();
    }

    @Override
    public String getUsername() {
        return usr.getUsrname();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- OAuth2User 인터페이스 구현 ---

    @Override
    public Map<String, Object> getAttributes() {
        // 소셜 로그인 시에만 사용되는 속성 정보 맵 반환
        return attributes;
    }

    @Override
    public String getName() {
        // 소셜 로그인 시, 어떤 값을 사용자의 고유 식별자로 할지 결정.
        // 여기서는 카카오/구글이 제공하는 id 값을 사용하도록 설정할 수 있습니다.
        // 일반적으로 UserDetails의 getUsername()과 혼동될 수 있어 잘 사용되지는 않습니다.
        return usr.getUsrname();
    }
}
