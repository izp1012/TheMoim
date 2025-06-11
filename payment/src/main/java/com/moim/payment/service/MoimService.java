package com.moim.payment.service;

import com.moim.payment.domain.Moim;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.domain.UsrMoim;
import com.moim.payment.dto.MoimInviteReqDTO;
import com.moim.payment.dto.MoimReqDTO;
import com.moim.payment.dto.MoimRespDTO;
import com.moim.payment.repository.MoimRepository;
import com.moim.payment.repository.UsrMoimRepository;
import com.moim.payment.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimService {

    private final MoimRepository moimRepository;
    private final UsrRepository usrRepository; // UsrService에서 UsrRepository를 주입받아 사용
    private final UsrMoimRepository usrMoimRepository;
    private final JavaMailSender mailSender; // 이메일 발송용

    // 1. 모임 생성
    @Transactional
    public MoimRespDTO createMoim(MoimReqDTO moimReqDTO) {
        // 생성자 Usr 유효성 확인
        Usr creatorUsr = usrRepository.findById(moimReqDTO.getCreatedByUsrId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 생성자 Usr ID입니다: " + moimReqDTO.getCreatedByUsrId()));

        // Moim 엔티티 생성
        Moim moim = moimReqDTO.toEntity();
        Moim savedMoim = moimRepository.save(moim);

        // 모임 생성자를 모임의 OWNER로 UsrMoim에 추가
        UsrMoim usrMoim = UsrMoim.builder()
                .usr(creatorUsr)
                .moim(savedMoim)
                .role(UsrMoim.UsrMoimRole.OWNER)
                .build();
        usrMoimRepository.save(usrMoim);

        return new MoimRespDTO(savedMoim);
    }

    // 2. 모임 조회 (단일 모임)
    public MoimRespDTO getMoimById(Long moimId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + moimId));
        return new MoimRespDTO(moim);
    }

    // 2. 모임 조회 (사용자가 속한 모든 모임)
    public List<MoimRespDTO> getMoimsByUsrId(Long usrId) {
        Usr usr = usrRepository.findById(usrId)
                .orElseThrow(() -> new IllegalArgumentException("Usr를 찾을 수 없습니다: " + usrId));

        // 해당 Usr가 속한 모든 UsrMoim 엔티티 조회
        List<UsrMoim> usrMoims = usrMoimRepository.findByUsr(usr);

        // UsrMoim에서 Moim 엔티티를 추출하여 DTO로 변환
        return usrMoims.stream()
                .map(UsrMoim::getMoim)
                .map(MoimRespDTO::new)
                .collect(Collectors.toList());
    }

    // 3. 모임에 사용자 초대 메시지 발송 (이메일)
    @Transactional
    public void inviteUsrToMoim(MoimInviteReqDTO inviteReqDTO) {
        Moim moim = moimRepository.findById(inviteReqDTO.getMoimId())
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + inviteReqDTO.getMoimId()));

        Usr inviterUsr = usrRepository.findById(inviteReqDTO.getInviterUsrId())
                .orElseThrow(() -> new IllegalArgumentException("초대하는 Usr를 찾을 수 없습니다: " + inviteReqDTO.getInviterUsrId()));

        Usr inviteeUsr = usrRepository.findByEmail(inviteReqDTO.getInviteeEmail())
                .orElseThrow(() -> new IllegalArgumentException("초대할 Usr의 이메일이 등록되어 있지 않습니다: " + inviteReqDTO.getInviteeEmail()));

        // 이미 모임에 초대된(속해있는) Usr인지 확인
        if (usrMoimRepository.findByUsrAndMoim(inviteeUsr, moim).isPresent()) {
            throw new IllegalStateException("해당 Usr는 이미 이 모임에 속해 있습니다.");
        }

        // 이메일 발송 로직
        sendInvitationEmail(moim.getMoimname(), inviterUsr.getUsrname(), inviteeUsr.getEmail());

        // UsrMoim 엔티티 생성 (초대 후 바로 멤버로 추가하는 경우, 아니면 초대 수락 로직 필요)
        UsrMoim newUsrMoim = UsrMoim.builder()
                .usr(inviteeUsr)
                .moim(moim)
                .role(UsrMoim.UsrMoimRole.MEMBER) // 초대된 사용자는 기본적으로 MEMBER 역할
                .build();
        usrMoimRepository.save(newUsrMoim);
    }

    private void sendInvitationEmail(String moimName, String inviterUsername, String inviteeEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(inviteeEmail);
        message.setSubject("[모임통장] " + inviterUsername + "님께서 '" + moimName + "' 모임에 초대하셨습니다!");
        message.setText(
                "안녕하세요!\n\n" +
                        inviterUsername + "님께서 '" + moimName + "' 모임통장 모임에 당신을 초대했습니다.\n\n" +
                        "모임에 참여하시려면 앱을 다운로드하고 로그인하여 해당 모임을 확인해주세요.\n\n" +
                        "감사합니다.\n" +
                        "모임통장 팀 드림"
        );
        mailSender.send(message);
    }
}
