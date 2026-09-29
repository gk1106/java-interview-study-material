package com.gk.study.dsaproblems.solutions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.AlienDictionary}.
 *
 * <p>Every adjacent pair of words contributes at most one edge: scan both words together until
 * the first differing character, which gives {@code earlierChar -> laterChar}. If no differing
 * character is found before one word runs out, the input is only valid if the first word is the
 * shorter one (otherwise a longer word sorted before its own prefix is a contradiction, e.g.
 * ["abc", "ab"]). Edges (deduplicated with a {@code HashSet<String>} of "from|to" keys) build an
 * adjacency map plus an in-degree count per letter; Kahn's BFS topological sort then produces
 * the ordering, using a {@code PriorityQueue<Character>} instead of a plain queue so that when
 * several letters are simultaneously free, the alphabetically smallest goes first (keeping the
 * result deterministic when the input under-constrains the order). If fewer letters end up
 * placed than appear in the input, a cycle exists and {@code ""} is returned. O(C) time where C
 * is the total number of characters across all words.
 */
public final class AlienDictionarySolution {

    private AlienDictionarySolution() {
    }

    public static String solve(List<String> words) {
        Map<Character, Set<Character>> adjacency = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();
        for (String word : words) {
            for (char c : word.toCharArray()) {
                adjacency.putIfAbsent(c, new HashSet<>());
                inDegree.putIfAbsent(c, 0);
            }
        }

        for (int i = 0; i < words.size() - 1; i++) {
            String first = words.get(i);
            String second = words.get(i + 1);
            int minLength = Math.min(first.length(), second.length());
            boolean foundDifference = false;
            for (int j = 0; j < minLength; j++) {
                char from = first.charAt(j);
                char to = second.charAt(j);
                if (from != to) {
                    if (adjacency.get(from).add(to)) {
                        inDegree.merge(to, 1, Integer::sum);
                    }
                    foundDifference = true;
                    break;
                }
            }
            if (!foundDifference && first.length() > second.length()) {
                return ""; // e.g. "abc" before "ab" -> contradiction, no valid order
            }
        }

        PriorityQueue<Character> ready = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                ready.offer(entry.getKey());
            }
        }

        StringBuilder order = new StringBuilder();
        while (!ready.isEmpty()) {
            char c = ready.poll();
            order.append(c);
            for (char next : adjacency.get(c)) {
                int updated = inDegree.merge(next, -1, Integer::sum);
                if (updated == 0) {
                    ready.offer(next);
                }
            }
        }

        return order.length() == inDegree.size() ? order.toString() : "";
    }
}
