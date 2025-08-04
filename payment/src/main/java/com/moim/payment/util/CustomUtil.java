package com.moim.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moim.payment.dto.ResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import com.moim.payment.util.CustomDateUtil;
import java.time.LocalDateTime;
import java.util.Map;

public class CustomUtil {
    private static final Logger log = LoggerFactory.getLogger(CustomUtil.class);

    public static void success(HttpServletResponse response, String ifid, String msg, Object dto){
        try{
            ObjectMapper om = new ObjectMapper();
            ResponseDto<?> responseDto = new ResponseDto<>(1, msg, new CustomDateUtil().toStringFormat(LocalDateTime.now()), dto);
            String responseBody = om.writeValueAsString(responseDto);
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(200);
            response.getWriter().println(responseBody); //공통적인 DTO 생성
        } catch (Exception e){
            log.error("success 서버 파싱 에러", e);
        }
    }

    public static void fail(HttpServletResponse response, String ifid, String msg, HttpStatus httpStatus){
        try{
            ObjectMapper om = new ObjectMapper();
            ResponseDto<?> responseDto = new ResponseDto<>(-1, msg, new CustomDateUtil().toStringFormat(LocalDateTime.now()), null);
            String responseBody = om.writeValueAsString(responseDto);
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(httpStatus.value());
            response.getWriter().println(responseBody); //공통적인 DTO 생성
        } catch (Exception e){
            log.error("fail 서버 파싱 에러", e);
        }
    }
}
