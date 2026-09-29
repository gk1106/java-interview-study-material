package com.gk.study.dsaproblems.solutions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.SlidingWindowMedian}.
 *
 * <p>The two-heap median structure from {@link MedianOfDataStream}, extended with lazy deletion:
 * a {@code HashMap<Integer, Integer>} tracks how many pending removals each value owes (a value
 * slides out of the window before either heap can efficiently remove it from the middle). Since
 * stale, not-yet-pruned entries can be buried inside a heap, {@code lowerMaxHeap.size()} /
 * {@code upperMinHeap.size()} are NOT reliable counts of valid elements — this implementation
 * tracks the true (logical) size of each half itself, and only prunes a heap's top when that top
 * is the exact value being removed (it may be buried below other valid elements, in which case
 * pruning happens later once it eventually surfaces). O(n log k) time, O(k) space.
 */
public final class SlidingWindowMedianSolution {

    private SlidingWindowMedianSolution() {
    }

    public static double[] solve(int[] nums, int k) {
        PriorityQueue<Integer> lowerMaxHeap = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<Integer> upperMinHeap = new PriorityQueue<>();
        Map<Integer, Integer> pendingRemoval = new HashMap<>();
        int[] sizes = {0, 0}; // sizes[0] = logical lowerMaxHeap size, sizes[1] = logical upperMinHeap size

        double[] result = new double[nums.length - k + 1];

        for (int i = 0; i < nums.length; i++) {
            insert(nums[i], lowerMaxHeap, upperMinHeap, sizes);

            if (i >= k) {
                remove(nums[i - k], lowerMaxHeap, upperMinHeap, pendingRemoval, sizes);
            }
            rebalance(lowerMaxHeap, upperMinHeap, pendingRemoval, sizes);

            if (i >= k - 1) {
                result[i - k + 1] = sizes[0] == sizes[1]
                        ? (lowerMaxHeap.peek() + upperMinHeap.peek()) / 2.0
                        : lowerMaxHeap.peek();
            }
        }
        return result;
    }

    private static void insert(int num, PriorityQueue<Integer> lowerMaxHeap,
            PriorityQueue<Integer> upperMinHeap, int[] sizes) {
        if (lowerMaxHeap.isEmpty() || num <= lowerMaxHeap.peek()) {
            lowerMaxHeap.offer(num);
            sizes[0]++;
        } else {
            upperMinHeap.offer(num);
            sizes[1]++;
        }
    }

    private static void remove(int num, PriorityQueue<Integer> lowerMaxHeap,
            PriorityQueue<Integer> upperMinHeap, Map<Integer, Integer> pendingRemoval, int[] sizes) {
        pendingRemoval.merge(num, 1, Integer::sum);
        if (num <= lowerMaxHeap.peek()) {
            sizes[0]--;
            if (num == lowerMaxHeap.peek()) {
                prune(lowerMaxHeap, pendingRemoval);
            }
        } else {
            sizes[1]--;
            if (num == upperMinHeap.peek()) {
                prune(upperMinHeap, pendingRemoval);
            }
        }
    }

    private static void rebalance(PriorityQueue<Integer> lowerMaxHeap,
            PriorityQueue<Integer> upperMinHeap, Map<Integer, Integer> pendingRemoval, int[] sizes) {
        if (sizes[0] > sizes[1] + 1) {
            upperMinHeap.offer(lowerMaxHeap.poll());
            sizes[0]--;
            sizes[1]++;
            prune(lowerMaxHeap, pendingRemoval);
        } else if (sizes[1] > sizes[0]) {
            lowerMaxHeap.offer(upperMinHeap.poll());
            sizes[1]--;
            sizes[0]++;
            prune(upperMinHeap, pendingRemoval);
        }
    }

    private static void prune(PriorityQueue<Integer> heap, Map<Integer, Integer> pendingRemoval) {
        while (!heap.isEmpty() && pendingRemoval.getOrDefault(heap.peek(), 0) > 0) {
            int top = heap.poll();
            pendingRemoval.merge(top, -1, Integer::sum);
        }
    }
}
