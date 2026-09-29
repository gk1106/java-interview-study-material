package com.gk.study.dsaproblems.solutions;

import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.MedianOfDataStream}.
 *
 * <p>Two heaps split the stream into a lower half and an upper half: {@code lowerMaxHeap} (a
 * max-heap) holds the smaller half, {@code upperMinHeap} (a min-heap) holds the larger half, and
 * they're kept balanced so their sizes never differ by more than 1. Every {@code addNum} pushes
 * onto one heap and then, if needed, moves that heap's top across to the other so the invariant
 * (every lower-half value &lt;= every upper-half value) holds. {@code findMedian} is then O(1):
 * either heap tops average together (even total count) or the larger heap's top is the median
 * outright (odd total count). O(log n) per {@code addNum}, O(1) per {@code findMedian}.
 */
public final class MedianOfDataStream {

    private final PriorityQueue<Integer> lowerMaxHeap = new PriorityQueue<>(Collections.reverseOrder());
    private final PriorityQueue<Integer> upperMinHeap = new PriorityQueue<>();

    public void addNum(int num) {
        if (lowerMaxHeap.isEmpty() || num <= lowerMaxHeap.peek()) {
            lowerMaxHeap.offer(num);
        } else {
            upperMinHeap.offer(num);
        }

        // rebalance so sizes never differ by more than 1
        if (lowerMaxHeap.size() > upperMinHeap.size() + 1) {
            upperMinHeap.offer(lowerMaxHeap.poll());
        } else if (upperMinHeap.size() > lowerMaxHeap.size() + 1) {
            lowerMaxHeap.offer(upperMinHeap.poll());
        }
    }

    public double findMedian() {
        if (lowerMaxHeap.isEmpty() && upperMinHeap.isEmpty()) {
            throw new IllegalStateException("No numbers have been added yet");
        }
        if (lowerMaxHeap.size() == upperMinHeap.size()) {
            return (lowerMaxHeap.peek() + upperMinHeap.peek()) / 2.0;
        }
        return lowerMaxHeap.size() > upperMinHeap.size() ? lowerMaxHeap.peek() : upperMinHeap.peek();
    }
}
