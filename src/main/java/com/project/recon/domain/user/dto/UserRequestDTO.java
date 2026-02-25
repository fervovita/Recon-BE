package com.project.recon.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class UserRequestDTO {


    @Getter
    public static class UpdateNickNameRequestDTO {

        @NotBlank(message = "닉네임이 없습니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
        private String nickName;
    }

    @Getter
    public static class EmailSendRequestDTO {

        @NotBlank(message = "이메일이 없습니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;
    }

    @Getter
    public static class UpdateEmailRequestDTO {

        @NotBlank(message = "이메일이 없습니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "인증 코드가 없습니다.")
        private String code;
    }

    @Getter
    public static class PhoneSendRequestDTO {

        @NotBlank(message = "전화번호가 없습니다.")
        @Pattern(
                regexp = "^010\\d{7,8}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        private String phoneNumber;
    }

    @Getter
    public static class UpdatePhoneRequestDTO {
        @NotBlank(message = "전화번호가 없습니다.")
        @Pattern(
                regexp = "^010\\d{7,8}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        private String phoneNumber;

        @NotBlank(message = "인증 코드가 없습니다.")
        private String code;
    }
}
