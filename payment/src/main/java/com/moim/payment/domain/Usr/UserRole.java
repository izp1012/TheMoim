package com.moim.payment.domain.Usr;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("관리자"),
    USER("사용자");

    private final String type;

    UserRole(String type) {
        this.type = type;
    }

}
