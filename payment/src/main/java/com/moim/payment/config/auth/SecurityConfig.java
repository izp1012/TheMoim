package com.moim.payment.config.auth;

import com.moim.payment.handler.JwtAccessDeniedHandler;
import com.moim.payment.handler.JwtAuthenticationEntryPoint;
import com.moim.payment.handler.OAuth2SuccessHandler;
import com.moim.payment.service.CustomOAuth2UserService;
import com.moim.payment.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그: BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // JWT 서버 생성 예정. Session 미사용
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenService tokenService, AuthenticationManager authenticationManager) throws Exception {

        log.debug("디버그: filterChain 빈 등록됨");

        // iframe 미사용
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // CORS 설정
        http.cors(cors -> cors.configurationSource(configurationSource()));

        // JSessionId를 서버에서 관리하지 않음 (JWT 사용을 위한 Stateless 설정)
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 폼 로그인 및 HTTP Basic 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 인증 및 권한 예외 처리
        // 인증(401) 또는 인가(403) 관련 예외가 최종적으로 발생하면 어떻게 처리할지 정의
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
        );

        // 커스텀 보안 필터 관리자 설정
        // 들어오는 모든 요청에 대해 JWT를 검사
        // 1. '인증' 필터 (로그인 처리)
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, tokenService);
        // 2. '인가' 필터 (매 요청마다 토큰 검증)
        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtTokenProvider);


        http.addFilter(jwtAuthenticationFilter); // 기본 UsernamePasswordAuthenticationFilter 위치에 등록
        http.addFilterBefore(jwtAuthorizationFilter, JwtAuthenticationFilter.class); // 인증 필터보다 먼저 실행되어야 함


        // OAuth2 설정
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService)
                )
        );

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v3/api-docs/**", // OpenAPI JSON
                        "/api-docs/**",
                        "/swagger-ui/**",    // Swagger UI
                        "/swagger-ui.html",
                        "/api/login/**",
                        "/api/signup/**",
                        "/api/oauth2/**",
                        "/api/oauth2/login/google",
                        "/api/image/**",
                        "/api/kftc/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        return http.build();
    }

    public CorsConfigurationSource configurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); //GET POST PUT DELETE (Javascript 요청 허용)
        configuration.addAllowedOriginPattern("*"); //모든 IP 주소 허용 (프론트 엔드 IP만 허용 react)
        configuration.setAllowCredentials(true); //클라이언트에서 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); //과거에는 디폴트. 브라우저에 Authorization 값을 노출 가능하게 함

        //모든 주소에 위 설정을 넣어준다
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
