package com.moim.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kftc")
public class KftcApiProperties {
    private String clientId;
    private String clientSecret;
    private String clientUseCode;
    private String clientUseName;
    private String orgCode;
    private String mainBankCode;
    private Endpoints endpoints = new Endpoints();

    @Data
    public static class Endpoints {
        private String authorize;
        private String token;
        private String revoke;
        private String userMe;
        private String accountList;
        private String accountUpdateInfo;
        private String accountCancel;
        private String balanceFinNum;
        private String transactionListFinNum;
        private String withdrawFinNum;
        private String depositFinNum;
        private String userRegister;
    }
}