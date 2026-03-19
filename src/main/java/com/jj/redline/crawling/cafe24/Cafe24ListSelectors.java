package com.jj.redline.crawling.cafe24;

public final class Cafe24ListSelectors {

    private Cafe24ListSelectors() {}

    public static final String PRODUCT_CARD = "li[id^=anchorBoxId_]";
    public static final String PRODUCT_LINK =
            "a[href*=/product/]:not([href*='list.html'])";
    public static final String PRODUCT_IMAGE = "img";
    public static final String PRODUCT_NAME = ".nm, .name, .description, .title";
    public static final String PRODUCT_BRAND = ".b a, .brand a, .brand";
    public static final String PRICE_WITH_ATTR = "[ec-data-price]";
    public static final String PRICE_TEXT = ".price, .prdPrice, .dsc, .spec";
    public static final String PAGINATION_CONTAINER = "div.xans-product-normalpaging";
    public static final String NEXT_PAGE_LINK = "a[href]:contains(>)";
}
