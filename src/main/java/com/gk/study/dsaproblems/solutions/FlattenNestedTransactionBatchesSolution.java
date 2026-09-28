package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.FlattenNestedTransactionBatches}.
 *
 * <p>Iterative DFS using an explicit {@code Deque} as the stack (instead of recursion). To
 * preserve left-to-right order when popping from the top of the stack, each nested list's
 * elements are pushed in reverse order before being processed. O(n) time where n is the total
 * number of elements across all nesting levels, O(n) space for the stack in the worst case
 * (a fully linear/deeply nested chain).
 */
public final class FlattenNestedTransactionBatchesSolution {

    private FlattenNestedTransactionBatchesSolution() {
    }

    public static List<Integer> solve(List<Object> nestedBatches) {
        List<Integer> result = new ArrayList<>();
        Deque<Object> stack = new ArrayDeque<>();
        pushReversed(stack, nestedBatches);

        while (!stack.isEmpty()) {
            Object item = stack.pop();
            if (item instanceof Integer amount) {
                result.add(amount);
            } else if (item instanceof List<?> nested) {
                pushReversed(stack, nested);
            } else {
                throw new IllegalArgumentException("Unsupported element type: " + item);
            }
        }
        return result;
    }

    private static void pushReversed(Deque<Object> stack, List<?> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            stack.push(items.get(i));
        }
    }
}
