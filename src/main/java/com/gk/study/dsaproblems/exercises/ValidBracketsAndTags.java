package com.gk.study.dsaproblems.exercises;

/**
 * E10 [Easy] Two related stack-matching validators in one class:
 * <ul>
 *   <li>{@code isValidBrackets}: is a string of {@code ()[]{}} correctly balanced and nested?</li>
 *   <li>{@code isValidTags}: is a string made ONLY of simple, unattributed XML-like tags
 *       (e.g. {@code "<a>"}, {@code "</a>"}, no free text between them) correctly balanced and
 *       nested?</li>
 * </ul>
 * Input: isValidBrackets("([{}])") &rarr; true; isValidBrackets("([)]") &rarr; false
 * Input: isValidTags("&lt;a&gt;&lt;b&gt;&lt;/b&gt;&lt;/a&gt;") &rarr; true;
 * isValidTags("&lt;a&gt;&lt;b&gt;&lt;/a&gt;&lt;/b&gt;") &rarr; false
 * Constraint: O(n) time, O(n) space; every open must be matched by the correct corresponding
 * close, most-recently-opened first.
 * Pattern: stack matching (push on open, pop-and-compare on close)
 * Collections: Deque (as a stack) + HashMap (bracket pair lookup)
 */
public class ValidBracketsAndTags {

    public static boolean isValidBrackets(String s) {
        // TODO: implement using a Deque<Character> as a stack and a HashMap of closing->opening
        // bracket pairs
        throw new UnsupportedOperationException("TODO");
    }

    public static boolean isValidTags(String s) {
        // TODO: implement using a Deque<String> as a stack of open tag names; parse each
        // "<...>" or "</...>" token and push/pop-and-compare accordingly
        throw new UnsupportedOperationException("TODO");
    }
}
