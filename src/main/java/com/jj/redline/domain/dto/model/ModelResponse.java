package com.jj.redline.domain.dto.model;

import com.jj.redline.domain.entity.ModelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모델 응답")
public class ModelResponse {

    @Schema(description = "모델 ID", example = "1")
    private Long id;

    @Schema(description = "브랜드 ID", example = "1")
    private Long brandId;

    @Schema(description = "브랜드 이름 (영문)", example = "LVC")
    private String brandName;

    @Schema(description = "브랜드 이름 (한글)", example = "리바이스 빈티지 클로딩")
    private String brandNameKo;

    @Schema(description = "모델 이름", example = "갤럭시 S24")
    private String modelName;

    @Schema(description = "모델 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "모델 타입", example = "DENIM_PANTS")
    private ModelType type;
}
