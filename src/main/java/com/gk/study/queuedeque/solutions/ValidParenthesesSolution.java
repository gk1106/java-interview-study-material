package com.gk.study.queuedeque.solutions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.ValidParentheses}.
 * Uses a {@link Deque} as a stack: push every opening bracket; on a closing bracket, the stack
 * must be non-empty and its popped top must be the matching opener, otherwise the string is
 * invalid. A non-empty stack at the end means unmatched openers remain.
 */
public class ValidParenthesesSolution {

    private static final Map<Character, Character> CLOSE_TO_OPEN = Map.of(')', '(', ']', '[', '}', '{');

    public static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.isEmpty() || stack.pop() != CLOSE_TO_OPEN.get(c)) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }
}
