package com.gk.study.dsaproblems.exercises;

/**
 * M01 [Medium] Given a string of characters, return the length of the longest substring that
 * contains no repeating characters.
 * Input: "abcabcbb" &rarr; Output: 3 (the substring "abc")
 * Constraint: O(n) time; the window's left edge should jump directly past a repeat instead of
 * shrinking one step at a time.
 * Pattern: variable sliding window
 * Collections: HashMap (char &rarr; last-seen index)
 */
public class LongestSubstringWithoutRepeating {

    public static int solve(String s) {
        // TODO: implement using a HashMap<Character, Integer> of each character's last-seen
        // index; when a repeat is found inside the current window, move the window's left edge
        // to (lastSeenIndex + 1) instead of advancing it one character at a time
        throw new UnsupportedOperationException("TODO");
    }
}
