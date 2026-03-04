package com.jj.redline.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @NotBlank
    @Schema(description = "사용자 ID", example = "user123")
    private String userId;

    @NotBlank
    @Size(min = 8)
    @Schema(description = "비밀번호 (최소 8자)", example = "password1!")
    private String password;

    @NotBlank
    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;
}
