package com.moim.payment.domain.Usr;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "usr_tb")
@Entity
@DynamicUpdate
public class Usr { //extends 시간설정 (상속)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 21, unique = true) //id
    private String usrname;

    @Column(nullable = true, length = 60) //패스워드 인코딩(BCrypt)
    private String password;

    private String nickname;

    private LocalDate birth;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role; //USER, ADMIN

    @Column
    private boolean social;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String zipcode;
    private String street;
    private String addressDetail;
    private String phoneNo;

    @CreatedDate //Insert
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate //Insert, Update
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Usr(String usrname, String password, String email, UserRole role, boolean social, Provider provider, LocalDateTime createdAt){
        this.usrname = usrname;
        this.password = password;
        this.email = email;
        this.role = role;
        this.social = social;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    public Usr updateUser(String username, String email) {
        this.usrname = username;
        this.email = email;

        return this;
    }
    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateSocial(Provider provider) {
        this.social = true;
        this.provider = provider;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}