package com.project.recon.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = resolveToken(request);

        if (accessToken != null) {

            String tokenStatus = jwtTokenProvider.validateTokenWithError(accessToken);

            if ("VALID".equals(tokenStatus) && jwtTokenProvider.isAccessToken(accessToken)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
                String role = jwtTokenProvider.getRoleFromToken(accessToken);

                if (userId != null && role != null) {

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    request.setAttribute("exception", "INVALID_TOKEN");
                }
            } else if ("VALID".equals(tokenStatus)) {
                request.setAttribute("exception", "INVALID_TOKEN");
            } else {
                request.setAttribute("exception", tokenStatus);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(CookieProvider.ACCESS_TOKEN_COOKIE)) {
                    return cookie.getValue();
                }
            }
        }

        // 테스트용 fallback
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
