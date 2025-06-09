package com.moim.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Getter
@Configuration
public class AppConfig {

    @Value("${IMAGE_BASE_URL}")
    private String imageBaseUrl;

}
