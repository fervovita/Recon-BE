package com.project.recon.global.jwt;

import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = resolveToken(request);

        if (accessToken != null) {

            String tokenStatus = jwtTokenProvider.validateTokenWithError(accessToken);

            if ("VALID".equals(tokenStatus) && jwtTokenProvider.isAccessToken(accessToken)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

                if (userId != null) {
                    Optional<User> user = userRepository.findById(userId);
                    if (user.isPresent()) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user.get(),
                                null,
                                user.get().getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
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
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
