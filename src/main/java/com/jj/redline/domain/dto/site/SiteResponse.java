package com.jj.redline.domain.dto.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@Schema(description = "사이트 응답")
public class SiteResponse implements Serializable {

    @Schema(description = "사이트 ID", example = "1")
    private Long id;

    @Schema(description = "사이트 이름", example = "쿠팡")
    private String siteName;

    @Schema(description = "사이트 링크", example = "https://www.coupang.com")
    private String siteLink;
}
