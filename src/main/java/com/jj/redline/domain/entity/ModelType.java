package com.jj.redline.domain.entity;

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
