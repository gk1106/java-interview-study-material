package com.gk.study.dsaproblems.exercises;

/**
 * M12 [Medium] Given a string, return the length of the longest palindrome that can be built
 * using its characters (case-sensitive), rearranged in any order.
 * Input: "abccccdd" &rarr; Output: 7 (e.g. "dccaccd" — every character with an even count is
 * used in full, plus one character with an odd count in the middle)
 * Constraint: O(n) time.
 * Pattern: frequency map
 * Collections: HashMap&lt;Character, Integer&gt;
 */
public class LongestPalindromeByRearranging {

    public static int solve(String s) {
        // TODO: implement using a HashMap<Character, Integer> frequency count; every character
        // contributes its count rounded down to the nearest even number (pairs go on both sides
        // of the palindrome), and if at least one character had an odd count, add exactly 1 more
        // for a single middle character
        throw new UnsupportedOperationException("TODO");
    }
}
