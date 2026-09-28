package com.gk.study.foundations.solutions;

import java.util.List;

/** Reference solution for {@code exercises.PecsCopy}. */
public class PecsCopySolution {

    public static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) {
            dest.add(item);
        }
    }
}
