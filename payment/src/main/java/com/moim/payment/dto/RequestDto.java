package com.moim.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class RequestDto<T> {
    private final String userId;
    private final String ip;
    private final LocalDateTime localDateTime; //호출 시간
    private final T data;
}
