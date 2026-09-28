package com.gk.study.collectionsoverview.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reference solution for E04 (see exercises.ImmutabilityExceptionClassifier).
 */
public class ImmutabilityExceptionClassifierSolution {

    public static Map<String, String> classify() {
        Map<String, String> result = new HashMap<>();

        result.put("listOfNull", attempt(() -> List.of("a", "b", null)));
        result.put("setOfDuplicate", attempt(() -> Set.of("a", "a")));

        List<String> unmodifiableWithNull = new ArrayList<>();
        String constructionOutcome = attempt(() -> {
            unmodifiableWithNull.addAll(Arrays.asList("a", null));
            return Collections.unmodifiableList(unmodifiableWithNull);
        });
        result.put("unmodifiableWithNullConstruction", constructionOutcome);

        List<String> unmodifiableView = Collections.unmodifiableList(new ArrayList<>(List.of("a", "b")));
        result.put("unmodifiableMutation", attempt(() -> unmodifiableView.add("c")));

        List<String> immutable = List.of("a", "b");
        result.put("listOfMutation", attempt(() -> immutable.add("c")));

        return result;
    }

    private static String attempt(java.util.concurrent.Callable<?> action) {
        try {
            action.call();
            return "OK";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName();
        } catch (Exception e) {
            // Callable's checked-exception escape hatch; not expected here.
            return e.getClass().getSimpleName();
        }
    }
}
