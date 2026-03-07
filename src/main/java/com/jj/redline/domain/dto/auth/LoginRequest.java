package com.jj.redline.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청")
public class LoginRequest {

    @NotBlank
    @Schema(description = "사용자 ID", example = "user123")
    private String userId;

    @NotBlank
    @Schema(description = "비밀번호", example = "password1!")
    private String password;
}
