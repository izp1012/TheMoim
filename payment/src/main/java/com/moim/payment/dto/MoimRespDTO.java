package com.moim.payment.dto;

import com.moim.payment.domain.Moim;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MoimRespDTO {
    private Long id;
    private String moimname;
    private String moimdesp;
    private Long createdByUsrId;
    private LocalDateTime createdAt;

    public MoimRespDTO(Moim moim) {
        this.id = moim.getId();
        this.moimname = moim.getMoimname();
        this.moimdesp = moim.getMoimdesp();
        this.createdByUsrId = moim.getCreatedByUsrId();
        this.createdAt = moim.getCreatedAt();
    }
}
