package com.jj.redline.api.auth;

import com.jj.redline.common.util.JwtProvider;
import com.jj.redline.domain.dto.auth.LoginRequest;
import com.jj.redline.domain.dto.auth.LoginResponse;
import com.jj.redline.domain.dto.auth.SignupRequest;
import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void signup(SignupRequest request) {
        userRepository.findByUserId(request.getUserId())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
                });

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.of(request.getUserId(), encodedPassword, request.getUserName());
        user.setAuditId("redline");
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        return new LoginResponse(accessToken, refreshToken);
    }
}
