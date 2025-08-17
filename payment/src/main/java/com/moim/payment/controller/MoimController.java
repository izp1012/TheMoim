package com.moim.payment.controller;

import com.moim.payment.dto.*;
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

    /**
     * 4. 특정 모임에 회원을 추가합니다.
     * 엔드포인트: POST /api/v1/moims/{moimId}/members
     * @param moimId 모임 ID
     * @param memberReqDTO 회원 정보 DTO
     * @return 생성된 회원 응답 DTO
     */
    @PostMapping("/{moimId}/members")
    public ResponseEntity<MemberRespDTO> addMemberToMoim(@PathVariable Long moimId, @Valid @RequestBody MemberReqDTO memberReqDTO) {
        try {
            MemberRespDTO newMember = moimService.addMemberToMoim(moimId, memberReqDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMember);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 5. 특정 모임의 모든 회원 목록을 조회합니다.
     * 엔드포인트: GET /api/v1/moims/{moimId}/members
     * @param moimId 모임 ID
     * @return 회원 목록 응답 DTO 리스트
     */
    @GetMapping("/{moimId}/members")
    public ResponseEntity<List<MemberRespDTO>> getMembersByMoim(@PathVariable Long moimId) {
        try {
            List<MemberRespDTO> members = moimService.getMembersByMoimId(moimId);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
