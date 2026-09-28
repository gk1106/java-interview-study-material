package com.gk.study.queuedeque.solutions;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.NextGreaterElement}.
 * Classic monotonic (decreasing) stack of indices: scan left to right; whenever the current
 * value is greater than the value at the index on top of the stack, that index has just found
 * its next-greater element, so pop it and record the answer, repeating until the stack top's
 * value is >= the current value (or the stack empties). Push the current index last. Any index
 * still on the stack at the end never found a next-greater element, so it keeps its -1 default.
 * O(n) time because each index is pushed once and popped at most once.
 */
public class NextGreaterElementSolution {

    public static int[] nextGreaterElements(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> indices = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!indices.isEmpty() && nums[indices.peek()] < nums[i]) {
                result[indices.pop()] = nums[i];
            }
            indices.push(i);
        }
        return result;
    }
}
