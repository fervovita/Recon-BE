package com.project.recon.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class UserRequestDTO {


    @Getter
    public static class SmsVerifyRequestDTO {

        @NotBlank(message = "인증 코드가 없습니다.")
        private String code;
    }
}
