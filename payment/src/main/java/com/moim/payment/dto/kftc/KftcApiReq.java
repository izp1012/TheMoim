package com.moim.payment.dto.kftc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KftcApiReq {
    private String code;
    private String redirect_uri;
}
