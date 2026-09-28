package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.ValidBracketsAndTags}.
 *
 * <p>Both validators reduce to the same shape: push on an "open" token, and on a "close" token
 * pop the stack and verify it matches; the string is valid iff the stack is empty at the end
 * (every open was matched, and no unmatched close was seen). O(n) time, O(n) space.
 */
public final class ValidBracketsAndTagsSolution {

    private static final Map<Character, Character> BRACKET_PAIRS =
            Map.of(')', '(', ']', '[', '}', '{');

    private ValidBracketsAndTagsSolution() {
    }

    public static boolean isValidBrackets(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (BRACKET_PAIRS.containsKey(c)) {
                if (stack.isEmpty() || !stack.pop().equals(BRACKET_PAIRS.get(c))) {
                    return false;
                }
            } else {
                return false; // unsupported character
            }
        }
        return stack.isEmpty();
    }

    public static boolean isValidTags(String s) {
        Deque<String> stack = new ArrayDeque<>();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) != '<') {
                return false; // only tag tokens are allowed in this simplified format
            }
            int close = s.indexOf('>', i);
            if (close < 0) {
                return false; // unterminated tag
            }
            String token = s.substring(i + 1, close);
            if (token.startsWith("/")) {
                String tagName = token.substring(1);
                if (stack.isEmpty() || !stack.pop().equals(tagName)) {
                    return false;
                }
            } else {
                stack.push(token);
            }
            i = close + 1;
        }
        return stack.isEmpty();
    }
}
