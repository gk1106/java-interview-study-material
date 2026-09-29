package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * H04 [Hard] Given {@code beginWord}, {@code endWord}, and a dictionary {@code wordList}, return
 * the length of the shortest transformation sequence from {@code beginWord} to {@code endWord}
 * (counting both endpoints) such that only one letter changes at each step and every
 * intermediate word must exist in {@code wordList}. Return 0 if no such sequence exists.
 * Input: beginWord="hit", endWord="cog", wordList=["hot","dot","dog","lot","log","cog"] &rarr;
 * Output: 5 ("hit" -&gt; "hot" -&gt; "dot" -&gt; "dog" -&gt; "cog")
 * Constraint: all words are the same length; {@code beginWord} itself need not be in
 * {@code wordList}, but {@code endWord} must be for any sequence to exist. O(n * L^2) time where
 * n = wordList.size(), L = word length (generating every one-letter variant of each word).
 * Pattern: BFS over an implicit word graph (edges = "differs by exactly one letter")
 * Collections: HashSet (dictionary membership + visited) + Queue (ArrayDeque)
 */
public class WordLadder {

    public static int solve(String beginWord, String endWord, List<String> wordList) {
        // TODO: implement using BFS layer by layer, starting from beginWord; a HashSet<String>
        // of the dictionary gives O(1) membership checks, and doubles as (or is paired with) the
        // visited set so no word is ever re-enqueued once reached. From each word, generate
        // every possible one-letter-changed variant (try every position x every other letter of
        // the alphabet) and enqueue any that is in the dictionary and not yet visited. The
        // answer is the BFS depth at which endWord is first reached, or 0 if the queue empties
        // first
        throw new UnsupportedOperationException("TODO");
    }
}
