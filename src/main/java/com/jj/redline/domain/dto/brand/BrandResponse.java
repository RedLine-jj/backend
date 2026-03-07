package com.jj.redline.domain.dto.brand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "브랜드 응답")
public class BrandResponse {

    @Schema(description = "브랜드 ID", example = "1")
    private Long id;

    @Schema(description = "브랜드 이름 (영문)", example = "Samsung")
    private String brandName;

    @Schema(description = "브랜드 이름 (한글)", example = "삼성")
    private String brandNameKo;
}
