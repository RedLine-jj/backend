package com.jj.redline.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; 

/**
 * 최종 산출물 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSnapshot {
    private Site site;               // "MODEMAN"
    private CategoryDto category;      // {code, name}
 
    private String brand;              // JSON-LD brand.name
    private String name;               // 상품명
    private String url;                // 상품 상세 URL
    private String imageUrl;           // 대표 이미지
    private Integer price;             // 기본 가격(대표가)

    private OffsetDateTime capturedAt; // 스냅샷 생성 시각(상세 파싱 완료 시각 추천) 
    private List<ProductOption> options;

    private ParseStatus parseStatus;
    private String parseMessage;       // (선택) 실패/부분실패 사유 간단 메시지
}
