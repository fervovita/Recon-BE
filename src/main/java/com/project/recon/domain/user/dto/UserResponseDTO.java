package com.project.recon.domain.user.dto;

import com.project.recon.domain.user.entity.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class UserResponseDTO {

    @Getter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class UserProfileResponseDTO {
        private Long id;
        private String nickName;
        private String email;
        private String phoneNumber;
        private boolean phoneNumberVerified;
        private LocalDate birthDate;
        private ProviderType provider;
    }
}
