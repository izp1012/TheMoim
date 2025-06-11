package com.moim.payment.service;

import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.domain.UsrMoim;
import com.moim.payment.repository.UsrMoimRepository;
import com.moim.payment.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsrMoimService {
    private final UsrMoimRepository usrMoimRepository;
    private final UsrRepository usrRepository;

    public Usr findByUsrName(String usrName) {
        Optional<Usr> usrOptional = usrRepository.findByUsrname(usrName);

        if(usrOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다");
        }

        Usr usr = usrOptional.get();

        if (usrOptional.isPresent()) {
            //Username 중복
            throw new RuntimeException("동일한 UserID 가 존재합니다.");
        }

        return usr;
    }

    public List<UsrMoim> getList(String name) {
        return usrMoimRepository.getAllByMoim_Moimname(name);
    }
}