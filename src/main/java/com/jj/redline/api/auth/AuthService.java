package com.jj.redline.api.auth;

import com.jj.redline.common.util.JwtProvider;
import com.jj.redline.domain.dto.auth.LoginRequest;
import com.jj.redline.domain.dto.auth.LoginResponse;
import com.jj.redline.domain.dto.auth.SignupRequest;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.UserRepository;
import com.jj.redline.exception.BadRequestException;
import com.jj.redline.exception.NotFoundException;
import com.jj.redline.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        userRepository.findByUserId(request.getUserId())
                .ifPresent(u -> {
                    throw new BadRequestException("이미 사용 중인 아이디입니다.");
                });

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.of(request.getUserId(), encodedPassword, request.getUserName());
        user.setAuditId("redline");
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않거나 만료된 리프레시 토큰입니다.");
        }

        String userId = jwtProvider.getUserId(refreshToken);
        userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }
}
