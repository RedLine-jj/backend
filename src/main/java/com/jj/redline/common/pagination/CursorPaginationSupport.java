package com.jj.redline.common.pagination;

import java.util.List;
import java.util.function.Function;

public final class CursorPaginationSupport {

    private CursorPaginationSupport() {
    }

    public static <T> CursorSlice<T> slice(List<T> items, int size, Function<T, Long> cursorExtractor) {
        boolean hasNext = items.size() > size;
        List<T> content = hasNext ? items.subList(0, size) : items;
        Long nextCursor = hasNext && !content.isEmpty()
                ? cursorExtractor.apply(content.get(content.size() - 1))
                : null;
        return new CursorSlice<>(content, nextCursor, hasNext);
    }

    public record CursorSlice<T>(List<T> content, Long nextCursor, boolean hasNext) {
    }
}
