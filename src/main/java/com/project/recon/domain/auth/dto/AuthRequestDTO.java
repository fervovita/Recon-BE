package com.project.recon.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class AuthRequestDTO {

    @Getter
    public static class KakaoLoginRequestDTO {
        @NotBlank(message = "카카오 코드가 없습니다.")
        private String code;
    }

    @Getter
    public static class EmailLoginRequestDTO {
        @NotBlank(message = "이메일이 없습니다.")
        private String email;

        @NotBlank(message = "비밀번호가 없습니다.")
        private String password;
    }

    @Getter
    public static class ReissueTokenRequestDTO {
        @NotBlank(message = "Refresh Token이 없습니다.")
        private String refreshToken;
    }
}
