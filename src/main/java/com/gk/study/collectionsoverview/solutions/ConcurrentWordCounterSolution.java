package com.gk.study.collectionsoverview.solutions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Reference solution for E02 (see exercises.ConcurrentWordCounter).
 */
public class ConcurrentWordCounterSolution {

    public static Map<String, Integer> countWords(List<String> words, int threadCount) {
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        if (words.isEmpty()) {
            return counts;
        }
        int effectiveThreads = Math.max(1, Math.min(threadCount, words.size()));
        ExecutorService pool = Executors.newFixedThreadPool(effectiveThreads);
        CountDownLatch latch = new CountDownLatch(effectiveThreads);
        int chunkSize = (int) Math.ceil(words.size() / (double) effectiveThreads);

        try {
            for (int t = 0; t < effectiveThreads; t++) {
                int start = t * chunkSize;
                int end = Math.min(start + chunkSize, words.size());
                if (start >= end) {
                    latch.countDown();
                    continue;
                }
                List<String> chunk = words.subList(start, end);
                pool.submit(() -> {
                    try {
                        for (String word : chunk) {
                            counts.merge(word, 1, Integer::sum);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdown();
        }
        return counts;
    }
}
