package com.moim.payment.dto;

import com.moim.payment.domain.Moim;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoimReqDTO {
    @NotBlank(message = "모임 명칭은 필수입니다.")
    @Size(max = 100, message = "모임 명칭은 100자 이하여야 합니다.")
    private String moimname;

    @Size(max = 500, message = "모임 설명은 500자 이하여야 합니다.")
    private String moimdesp;

    @NotNull(message = "모임 생성자 ID는 필수입니다.")
    private Long createdByUsrId; // 현재 로그인된 사용자의 ID

    public Moim toEntity() {
        return Moim.builder()
                .moimname(this.moimname)
                .moimdesp(this.moimdesp)
                .createdByUsrId(this.createdByUsrId)
                .build();
    }
}
