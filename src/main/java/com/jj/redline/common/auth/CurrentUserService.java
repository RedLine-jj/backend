package com.jj.redline.common.auth;

import com.jj.redline.domain.entity.User;
import com.jj.redline.domain.repository.UserRepository;
import com.jj.redline.exception.NotFoundException;
import com.jj.redline.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        String userId = authentication.getName();
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
