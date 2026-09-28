package com.gk.study.list.examples;

/**
 * Demonstrates the four core array/list DSA patterns on small inputs: two pointers (palindrome
 * check), sliding window (fixed-size max sum), prefix sum (range query), and in-place reversal.
 *
 * See notes/03-list/08-dsa-patterns-lists-arrays.md for the pattern explanations.
 */
public final class ListPatternsDemo {

    private ListPatternsDemo() {
    }

    public static void main(String[] args) {
        // Two pointers: palindrome check.
        int[] palin = {1, 2, 3, 2, 1};
        System.out.println("isPalindrome(1,2,3,2,1) = " + isPalindrome(palin));

        // Sliding window: max sum of any window of size k.
        int[] arr = {2, 1, 5, 1, 3, 2};
        System.out.println("maxSumWindow(k=3) = " + maxSumWindow(arr, 3)); // 9 -> [5,1,3]

        // Prefix sum: range sum query.
        int[] data = {2, -1, 3, 4, -2};
        int[] prefix = buildPrefixSums(data);
        System.out.println("rangeSum(1,3) = " + rangeSum(prefix, 1, 3)); // arr[1]+arr[2]+arr[3] = 6

        // In-place reversal.
        int[] toReverse = {1, 2, 3, 4, 5};
        reverseInPlace(toReverse);
        System.out.println("reversed = " + java.util.Arrays.toString(toReverse));
    }

    private static boolean isPalindrome(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        while (left < right) {
            if (arr[left] != arr[right]) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    private static int maxSumWindow(int[] arr, int k) {
        int windowSum = 0;
        for (int i = 0; i < k; i++) {
            windowSum += arr[i];
        }
        int best = windowSum;
        for (int i = k; i < arr.length; i++) {
            windowSum += arr[i] - arr[i - k];
            best = Math.max(best, windowSum);
        }
        return best;
    }

    private static int[] buildPrefixSums(int[] arr) {
        int[] prefix = new int[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            prefix[i + 1] = prefix[i] + arr[i];
        }
        return prefix;
    }

    private static int rangeSum(int[] prefix, int fromInclusive, int toInclusive) {
        return prefix[toInclusive + 1] - prefix[fromInclusive];
    }

    private static void reverseInPlace(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        while (left < right) {
            int tmp = arr[left];
            arr[left] = arr[right];
            arr[right] = tmp;
            left++;
            right--;
        }
    }
}
