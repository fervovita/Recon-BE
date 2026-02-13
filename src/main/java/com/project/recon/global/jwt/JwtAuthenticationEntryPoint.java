package com.project.recon.global.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String exception = (String) request.getAttribute("exception");

        GeneralErrorCode errorCode;

        if ("TOKEN_EXPIRED".equals(exception)) {
            errorCode = GeneralErrorCode.TOKEN_EXPIRED;
        } else if ("INVALID_TOKEN".equals(exception)) {
            errorCode = GeneralErrorCode.INVALID_TOKEN;
        } else {
            errorCode = GeneralErrorCode.MISSING_AUTH_INFO;
        }

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.onFailure(errorCode, null))
        );
    }
}
