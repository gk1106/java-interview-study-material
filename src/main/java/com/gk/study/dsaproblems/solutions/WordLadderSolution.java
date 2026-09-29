package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.WordLadder}.
 *
 * <p>BFS layer by layer over the implicit graph where an edge connects two dictionary words that
 * differ by exactly one letter. A {@code HashSet<String>} of the dictionary gives O(1) membership
 * checks and, once a word is dequeued/visited, it is removed from that same set so it is never
 * revisited (folding "dictionary" and "visited" into one structure). From each word, every
 * possible one-letter variant is generated (each position &times; each other letter of the
 * alphabet) and any variant still present in the set is enqueued. Since BFS explores in
 * non-decreasing distance order, the first time {@code endWord} is dequeued is the shortest
 * transformation sequence length. O(n * L^2 * 26) time where n = wordList.size(), L = word
 * length.
 */
public final class WordLadderSolution {

    private WordLadderSolution() {
    }

    public static int solve(String beginWord, String endWord, List<String> wordList) {
        Set<String> dictionary = new HashSet<>(wordList);
        if (!dictionary.contains(endWord)) {
            return 0;
        }
        dictionary.remove(beginWord);

        Queue<String> queue = new ArrayDeque<>();
        queue.offer(beginWord);
        int steps = 1;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                String word = queue.poll();
                if (word.equals(endWord)) {
                    return steps;
                }
                char[] chars = word.toCharArray();
                for (int pos = 0; pos < chars.length; pos++) {
                    char original = chars[pos];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == original) {
                            continue;
                        }
                        chars[pos] = c;
                        String candidate = new String(chars);
                        if (dictionary.remove(candidate)) {
                            queue.offer(candidate);
                        }
                    }
                    chars[pos] = original;
                }
            }
            steps++;
        }
        return 0;
    }
}
