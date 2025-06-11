package com.moim.payment.controller;

import com.moim.payment.dto.MoimInviteReqDTO;
import com.moim.payment.dto.MoimReqDTO;
import com.moim.payment.dto.MoimRespDTO;
import com.moim.payment.service.MoimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moims") // moims로 매핑
@RequiredArgsConstructor
public class MoimController {

    private final MoimService moimService;

    // 1. 모임 생성
    @PostMapping
    public ResponseEntity<MoimRespDTO> createMoim(@Valid @RequestBody MoimReqDTO moimReqDTO) {
        try {
            MoimRespDTO newMoim = moimService.createMoim(moimReqDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMoim);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 예: 생성자 Usr ID 오류
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 2. 특정 Usr가 속한 모든 모임 조회
    @GetMapping("/by-usr/{usrId}")
    public ResponseEntity<List<MoimRespDTO>> getMoimsByUsr(@PathVariable Long usrId) {
        try {
            List<MoimRespDTO> moims = moimService.getMoimsByUsrId(usrId);
            return ResponseEntity.ok(moims);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 예: Usr를 찾을 수 없음
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 2. 단일 모임 조회
    @GetMapping("/{moimId}")
    public ResponseEntity<MoimRespDTO> getMoimById(@PathVariable Long moimId) {
        try {
            MoimRespDTO moim = moimService.getMoimById(moimId);
            return ResponseEntity.ok(moim);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 3. 모임에 사용자 초대 메시지 발송
    @PostMapping("/invite")
    public ResponseEntity<String> inviteUsrToMoim(@Valid @RequestBody MoimInviteReqDTO inviteReqDTO) {
        try {
            moimService.inviteUsrToMoim(inviteReqDTO);
            return ResponseEntity.ok("초대 이메일이 성공적으로 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 이미 초대됨
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("초대 이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }
}
