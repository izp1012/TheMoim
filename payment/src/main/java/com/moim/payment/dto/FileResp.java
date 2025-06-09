package com.moim.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResp {
    private Long id;
    private String fileName;
    private String url; // 클라이언트가 접근할 수 있는 이미지 URL
    private String message; // 업로드 결과 메시지 (옵션)
}
