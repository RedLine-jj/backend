package com.jj.redline.domain.dto;

/**
 * 상세 파싱 결과 상태.
 * - OK: JSON-LD 파싱 성공 + offers 정상
 * - PARTIAL: JSON-LD는 찾았지만 offers 비었거나 일부 필드 누락
 * - FAIL: JSON-LD(Product) 자체를 못 찾았거나 JSON 파싱 실패 등
 */
public enum ParseStatus {
    OK,
    PARTIAL,
    FAIL
}