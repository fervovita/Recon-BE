package com.project.recon.domain.auth.service;

import com.project.recon.domain.auth.dto.AuthRequestDTO;
import com.project.recon.domain.auth.dto.AuthResponseDTO;
import com.project.recon.domain.auth.dto.KakaoTokenResponseDTO;
import com.project.recon.domain.auth.dto.KakaoUserInfoResponseDTO;
import com.project.recon.domain.user.entity.ProviderType;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponseDTO.LoginResponseDTO kakaoLogin(AuthRequestDTO.KakaoLoginRequestDTO request) {

        // code를 이용해 카카오 accessToken 받기
        KakaoTokenResponseDTO kakaoToken;
        try {
            kakaoToken = kakaoOAuthService.getAccessToken(request.getCode());
        } catch (Exception e) {
            log.error("카카오 토큰 요청 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.KAKAO_INVALID_CODE);
        }

        // 카카오에서 유저 정보 받기
        KakaoUserInfoResponseDTO kakaoUserInfo;
        try {
            kakaoUserInfo = kakaoOAuthService.getUserInfo(kakaoToken.getAccessToken());
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.KAKAO_USER_INFO_FAILED);
        }

        if (kakaoUserInfo.getId() == null) {
            throw new GeneralException(GeneralErrorCode.KAKAO_USER_INFO_FAILED);
        }

        // 유저 조회 (없으면 새로 생성)
        User user = userRepository.findByProviderAndProviderId(ProviderType.KAKAO, kakaoUserInfo.getId())
                .orElseGet(() -> {
                    User newUser = User.createKakaoUser(
                            kakaoUserInfo.getId(),
                            kakaoUserInfo.getNickname()
                    );

                    return userRepository.save(newUser);
                });

        // accessToken & refreshToken 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // refreshToken을 Redis에 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return AuthResponseDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO.LoginResponseDTO emailLogin(AuthRequestDTO.EmailLoginRequestDTO request) {

        // email로 유저 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_LOGIN));

        // 소셜 로그인 유저는 이메일 로그인 불가
        if (user.getPassword() == null) {
            throw new GeneralException(GeneralErrorCode.INVALID_LOGIN);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GeneralException(GeneralErrorCode.INVALID_LOGIN);
        }

        // accessToken & refreshToken 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // refreshToken을 Redis에 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return AuthResponseDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO.ReissueTokenResponseDTO reissueToken(AuthRequestDTO.ReissueTokenRequestDTO request) {

        String refreshToken = request.getRefreshToken();

        // refreshToken 유효성 검증
        if (!jwtTokenProvider.isRefreshToken(refreshToken) || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Redis에 저장된 refreshToken과 비교
        String savedRefreshToken = refreshTokenService.getRefreshToken(userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // 새로운 accessToken & refreshToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Redis에 새 refreshToken 저장
        refreshTokenService.saveRefreshToken(userId, newRefreshToken);

        return AuthResponseDTO.ReissueTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(AuthRequestDTO.LogoutRequestDTO request) {

        String refreshToken = request.getRefreshToken();

        // refreshToken 유효성 검증
        if (!jwtTokenProvider.isRefreshToken(refreshToken) || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Redis에 저장된 refreshToken과 비교
        String savedRefreshToken = refreshTokenService.getRefreshToken(userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
        }

        // Redis에서 refreshToken 삭제
        refreshTokenService.deleteRefreshToken(userId);
    }
}
