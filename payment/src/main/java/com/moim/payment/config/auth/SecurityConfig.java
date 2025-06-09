package com.moim.payment.config.auth;

import com.moim.payment.handler.JwtAccessDeniedHandler;
import com.moim.payment.handler.JwtAuthenticationEntryPoint;
import com.moim.payment.service.TokenService;
import com.moim.payment.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그: BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //JWT 필터 등록이 필요함
    public static class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
        private final JwtTokenProvider jwtTokenProvider;
        private final TokenService tokenService;

        public CustomSecurityFilterManager(JwtTokenProvider jwtTokenProvider, TokenService tokenService) {
            this.jwtTokenProvider = jwtTokenProvider;
            this.tokenService = tokenService;
        }

        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            builder.addFilterBefore(new JwtRequestFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
            super.configure(builder);
        }

        public HttpSecurity build() {
            return getBuilder();
        }
    }

    // JWT 서버 생성 예정. Session 미사용
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenService tokenService) throws Exception {

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

        // 인증 및 권한 예외 처리 (한 곳으로 통합)
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
        );
        // 커스텀 보안 필터 관리자 설정
        http.with(new CustomSecurityFilterManager(jwtTokenProvider, tokenService), CustomSecurityFilterManager::build);

        // 인증 실패 가로채기
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    CustomUtil.fail(response, "login", "로그인을 진행해 주세요", HttpStatus.UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    CustomUtil.fail(response, "login", "권한이 없습니다.", HttpStatus.FORBIDDEN);
                })
        );

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v3/api-docs/**", // OpenAPI JSON
                        "/api-docs/**",
                        "/swagger-ui/**",    // Swagger UI
                        "/swagger-ui.html",
                        "/api/login/**",
                        "/api/join/**",
                        "/api/oauth2/**",
                        "/api/oauth2/login/google",
                        "/api/image/**").permitAll()
                .requestMatchers("/api/auth/**").authenticated());

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
