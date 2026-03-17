package com.jj.redline.common.util;

import java.util.regex.Pattern;

public final class OptionLabelNormalizer {

    private static final Pattern PARENTHETICAL = Pattern.compile("\\s*\\(.*?\\)\\s*");

    private OptionLabelNormalizer() {}

    public static String normalize(String label) {
        if (label == null || label.isBlank()) return label;
        return PARENTHETICAL.matcher(label).replaceAll("").strip();
    }
}
