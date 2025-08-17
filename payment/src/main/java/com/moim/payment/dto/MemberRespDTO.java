package com.moim.payment.dto;

import com.moim.payment.domain.UsrGroupMember;
import lombok.Getter;

@Getter
public class MemberRespDTO {
    private Long id;
    private String name;
    private String role;
    private String contactInfo;
    private Double defaultFee;
    private Long groupId;

    public MemberRespDTO(UsrGroupMember usrGroupMember) {
        this.id = usrGroupMember.getId();
        this.name = usrGroupMember.getMembername();
        this.role = usrGroupMember.getRole();
        this.contactInfo = usrGroupMember.getContactInfo();
        this.defaultFee = usrGroupMember.getDefaultFee();
        this.groupId = usrGroupMember.getGroupId();
    }
}