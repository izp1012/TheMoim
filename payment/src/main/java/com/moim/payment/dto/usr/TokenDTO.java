package com.moim.payment.dto.usr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Duration duration;
}