package com.moim.payment.config;//package com.moim.payment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .schemaRequirement("BearerAuth", securityScheme)
                .info(new Info()
                        .title("AI Subscription API")
                        .version("1.0")
                        .description("사용자가 AI를 구독하고, 채팅을 할 수 있는 API입니다.")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("uf-support@google.com"))
                )

                // 추가 설정 속성
                // API가 배포된 서버들을 정의
                .servers(List.of(
//                        new Server().url("https://3.39.234.47:8081").description("Develop Server"),  // 개발 서버 설정
                        new Server().url("http://localhost:8081").description("Local Server")  // 로컬 서버 설정
//                        new Server().url("https://www.uf-production.com").description("Production Server")  // 프로덕션 서버 설정
                ));
    }
}