package com.jj.redline.crawling.modeman.list;

public final class ModeManListSelectors {

    private ModeManListSelectors() {}

    /** 상품 카드 루트(li). 보통 Cafe24는 anchorBoxId_상품번호 형태 */
    public static final String PRODUCT_CARD = "li[id^=anchorBoxId_]";

    /** 상품 상세 링크 */
    public static final String PRODUCT_LINK =
        "a[href^=/product/]:not([href*='list.html'])";

    /** 썸네일 이미지 */
    public static final String PRODUCT_IMAGE = "img";

    /** 상품명 텍스트가 있는 곳(없을 수도 있어 fallback 필요) */
    public static final String PRODUCT_NAME = ".nm, .name, .description, .title";

    /** 브랜드 텍스트가 있는 곳(사이트마다 다를 수 있음) */
    public static final String PRODUCT_BRAND = ".b a, .brand a, .brand";

    /** Cafe24 목록 가격 attribute (ec-data-price) */
    public static final String PRICE_WITH_ATTR = "[ec-data-price]";

    /** 가격 텍스트 fallback */
    public static final String PRICE_TEXT = ".price, .prdPrice, .dsc, .spec";
}