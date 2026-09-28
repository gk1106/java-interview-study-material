package com.gk.study.dsaproblems.solutions;

import java.util.List;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.MajorityElementBoyerMoore}.
 *
 * <p>Boyer-Moore voting: keep a {@code candidate} and a {@code count}. Increment count when the
 * current element equals the candidate, otherwise decrement; when count hits zero, swap in a new
 * candidate. Because a true majority element occurs more than n/2 times, it always survives this
 * cancellation process. O(n) time, O(1) space — no auxiliary map needed.
 */
public final class MajorityElementBoyerMooreSolution {

    private MajorityElementBoyerMooreSolution() {
    }

    public static int solve(List<Integer> nums) {
        Integer candidate = null;
        int count = 0;
        for (int num : nums) {
            if (count == 0) {
                candidate = num;
            }
            count += (num == candidate) ? 1 : -1;
        }
        return candidate;
    }
}
