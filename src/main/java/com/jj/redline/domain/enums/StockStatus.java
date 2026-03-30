package com.jj.redline.domain.enums;


/**
 * 최종 산출물(options.status)에 들어갈 재고 상태.
 * (DB에서는 boolean으로 저장하더라도, 파일/DTO에서는 의미가 명확한 enum을 유지)
 */
public enum StockStatus {
    AVAILABLE,
    SOLD_OUT
}