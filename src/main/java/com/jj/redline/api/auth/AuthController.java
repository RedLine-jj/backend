package com.jj.redline.api.auth;

import com.jj.redline.common.ApiResponse;
import com.jj.redline.domain.dto.auth.LoginRequest;
import com.jj.redline.domain.dto.auth.LoginResponse;
import com.jj.redline.domain.dto.auth.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입 / 로그인")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok();
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(null, null);
    }
}
