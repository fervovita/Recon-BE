package com.project.recon.domain.user.entity;

import com.project.recon.global.BaseEntity;
import com.project.recon.global.encryption.EncryptConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private ProviderType provider;

    @Column(name = "provider_id")
    private Long providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @Column(name = "nick_name", length = 20)
    private String nickName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Convert(converter = EncryptConverter.class)
    @Column(name = "phone_number", length = 100)
    private String phoneNumber;

    @Column(name = "phone_number_hash", length = 64)
    private String phoneNumberHash;

    @Column(name = "phone_verified")
    private boolean phoneNumberVerified;

    public static User createKakaoUser(Long kakaoId, String nickName) {
        return User.builder()
                .provider(ProviderType.KAKAO)
                .providerId(kakaoId)
                .nickName(nickName)
                .role(UserRole.USER)
                .build();
    }

    public static User createEmailUser(String nickName, String email, String password, String phoneNumber, String phoneNumberHash, LocalDate birthDate) {
        return User.builder()
                .nickName(nickName)
                .email(email)
                .password(password)
                .phoneNumber(phoneNumber)
                .phoneNumberHash(phoneNumberHash)
                .phoneNumberVerified(false)
                .birthDate(birthDate)
                .provider(ProviderType.LOCAL)
                .role(UserRole.USER)
                .build();
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePhoneNumber(String phoneNumber, String phoneNumberHash) {
        this.phoneNumber = phoneNumber;
        this.phoneNumberHash = phoneNumberHash;
        phoneNumberVerified = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }
}
