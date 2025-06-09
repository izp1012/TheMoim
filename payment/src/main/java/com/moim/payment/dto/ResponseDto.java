package com.moim.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDto<T> {
    //카산드라 DB (Log 남기기 좋은 NoSQL DB)
    private final Integer code; //1 성공 -1 실패
    private final String msg;
    private final String localDateTime; //응답 시간
    private final T data;
}
