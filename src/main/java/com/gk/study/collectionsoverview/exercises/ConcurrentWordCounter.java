package com.gk.study.collectionsoverview.exercises;

import java.util.List;
import java.util.Map;

/**
 * E02 [Easy] Count word frequency across multiple concurrent threads
 * correctly and without external locking, by choosing a map designed for
 * concurrent mutation.
 *
 * Input:  a List&lt;String&gt; of words (possibly with duplicates) and a
 *         thread count to split the counting work across.
 * Output: a Map&lt;String, Integer&gt; of word -&gt; occurrence count, correct
 *         regardless of how the words were partitioned across threads.
 *
 * Example:
 *   countWords(List.of("a", "b", "a", "c", "b", "a"), 4)
 *     -&gt; {"a": 3, "b": 2, "c": 1}
 *
 * Constraint: must be safe under concurrent increments from multiple
 * threads — no lost updates. O(n) total work across all threads.
 * Pattern: concurrent map choice (atomic compound operations)
 */
public class ConcurrentWordCounter {

    public static Map<String, Integer> countWords(List<String> words, int threadCount) {
        // TODO: implement using a ConcurrentHashMap and an atomic increment
        // operation (e.g. merge(word, 1, Integer::sum)), splitting `words`
        // across `threadCount` threads (e.g. via ExecutorService) and
        // awaiting completion before returning the result.
        throw new UnsupportedOperationException("TODO");
    }
}
