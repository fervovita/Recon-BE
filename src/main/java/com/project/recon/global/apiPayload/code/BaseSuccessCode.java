package com.project.recon.global.apiPayload.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {

    HttpStatus getHttpStatus();

    String getCode();

    String getMessage();
}
