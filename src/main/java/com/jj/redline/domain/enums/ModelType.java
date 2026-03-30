package com.jj.redline.domain.enums;

public enum ModelType {
    DENIM_PANTS("청바지"),
    DENIM_JACKET("청자켓");

    private final String label;

    ModelType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
