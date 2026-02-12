package com.project.recon.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;

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

    @Getter
    public static class LogoutRequestDTO {
        @NotBlank(message = "Refresh Token이 없습니다.")
        private String refreshToken;
    }

    @Getter
    public static class SignupRequestDTO {
        @NotBlank(message = "닉네임이 없습니다.")
        private String nickName;

        @NotBlank(message = "이메일이 없습니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비빌번호가 없습니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "비밀번호는 8~16자이며, 영문·숫자·특수문자를 모두 포함해야 합니다."
        )
        private String password;

        @NotBlank(message = "전화번호가 없습니다.")
        @Pattern(
                regexp = "^010\\d{7,8}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        private String phoneNumber;

        @NotNull(message = "생년월일이 없습니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate birthDate;
    }
}
