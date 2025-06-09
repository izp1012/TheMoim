package com.moim.payment.service;

import com.moim.payment.config.auth.LoginUsr;
import com.moim.payment.domain.Usr.UserRole;
import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.dto.user.*;
import com.moim.payment.exception.ResourceNotFoundException;
import com.moim.payment.repository.UsrRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsrService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UsrRepository usrRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    //서비스는 DTO로 요청받고 DTO로 응답한다.
    @Transactional //트랜잭션이 메서드 시작할때, 시작되고, 종료될 때 함께 종료
    public JoinRespDto join(JoinReqDto joinReqDto) {
        // 1. 동일 유저네임 존재 검사
        Optional<Usr> userOptional = usrRepository.findByUsrId(joinReqDto.getUserId());
        if (userOptional.isPresent()) {
            //Username 중복
            throw new RuntimeException("동일한 UserID 가 존재합니다.");
        }

        // 2. 패스워드 인코딩 - 회원가입
        Usr usr = usrRepository.save(joinReqDto.toEntity(passwordEncoder));
        usr.updateRole(UserRole.USER);

        // 3. dto 응답
        return new JoinRespDto(usr);
    }

    public TokenDTO login(LoginReqDto loginReqDto, HttpServletResponse response) {
        Optional<Usr> usrOptional = usrRepository.findByUsrId(loginReqDto.getUserId());

        if(usrOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        Usr usr = usrOptional.get();

        if(!passwordEncoder.matches(loginReqDto.getPassword(), usr.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        //JWT 생성
        LoginUsr loginUsr = new LoginUsr(usr);

        // SecurityContextHolder 에 저장
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUsr, null, loginUsr.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        ;
        return tokenService.createToken(usrOptional.get());
    }


    public Usr findUserbyUsername(String usrname) {
        Optional<Usr> usrOptional = Optional.ofNullable(usrRepository.findByUsrname(usrname)
                .orElseThrow(() -> new IllegalArgumentException("User not found for usrname: " + usrname)));

        return usrOptional.get();
    }

    /**
     * ID로 사용자 조회
     * @param usrId 사용자 ID
     * @return 사용자 엔티티
     */
    public UsrRespDto findUsrById(String usrId) {
        Usr user = usrRepository.findByUsrId(usrId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", usrId));

        return UsrRespDto.from(user);
    }

    public Usr findUserEntityById(String userId) {
        return usrRepository.findByUsrId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public Usr getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return findUserbyUsername(username);
        }

        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("사용자 로드 시도 : "+userId);
        Usr userPS =usrRepository.findByUsrId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("userId: " + userId + "를 데이터베이스에서 찾을 수 없습니다."));
        return new LoginUsr(userPS);
    }

}
