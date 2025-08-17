package com.moim.payment.repository;

import com.moim.payment.domain.UsrGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsrGroupMemberRepository extends JpaRepository<UsrGroupMember, Long> {

    // 특정 groupId에 속한 모든 UsrGroupMember 목록을 조회
    List<UsrGroupMember> findByGroupId(Long groupId);
}