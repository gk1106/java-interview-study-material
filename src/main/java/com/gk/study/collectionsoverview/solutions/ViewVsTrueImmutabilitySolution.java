package com.gk.study.collectionsoverview.solutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reference solution for E02 (see exercises.ViewVsTrueImmutability).
 */
public class ViewVsTrueImmutabilitySolution {

    public record Result(List<String> view, List<String> trulyImmutable) {
    }

    public static Result run(List<String> initialContents, String elementToAppend) {
        List<String> backing = new ArrayList<>(initialContents);
        List<String> view = Collections.unmodifiableList(backing);
        List<String> trulyImmutable = List.copyOf(initialContents);

        backing.add(elementToAppend); // mutate the backing list, not the view

        return new Result(List.copyOf(view), trulyImmutable);
    }
}
