package com.gk.study.concurrency.examples;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Demonstrates thenApply vs thenCompose (map vs flatMap), thenCombine of two independently
 * running futures, allOf/anyOf, and exceptionally/handle recovering from a failed stage. Every
 * async call uses an explicit, bounded, shutdown-in-finally ExecutorService rather than the
 * shared ForkJoinPool.commonPool(), and every blocking wait uses a bounded get(timeout, unit).
 *
 * See notes/09-multithreading-concurrency/07-completablefuture.md
 */
public final class CompletableFutureDemo {

    private CompletableFutureDemo() {
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            thenApplyVsThenComposeDemo(pool);
            thenCombineDemo(pool);
            allOfAnyOfDemo(pool);
            exceptionallyAndHandleDemo(pool);
        } finally {
            shutdownQuietly(pool);
        }
    }

    private static void thenApplyVsThenComposeDemo(ExecutorService pool) throws Exception {
        System.out.println("--- thenApply (map) vs thenCompose (flatMap) ---");
        CompletableFuture<String> userCityFuture = CompletableFuture
                .supplyAsync(() -> "user-42", pool)
                .thenCompose(userId -> fetchCityAsync(userId, pool))   // flattened, not nested
                .thenApply(String::toUpperCase);                       // pure transform

        System.out.println("thenApply vs thenCompose: flattened result (no nested future) = "
                + userCityFuture.get(2, TimeUnit.SECONDS));
        System.out.println();
    }

    private static CompletableFuture<String> fetchCityAsync(String userId, ExecutorService pool) {
        return CompletableFuture.supplyAsync(() -> "london", pool);
    }

    private static void thenCombineDemo(ExecutorService pool) throws Exception {
        System.out.println("--- thenCombine: two independent futures combined ---");
        CompletableFuture<Integer> priceFuture = CompletableFuture.supplyAsync(() -> 100, pool);
        CompletableFuture<Integer> stockFuture = CompletableFuture.supplyAsync(() -> 42, pool);
        CompletableFuture<String> quote = priceFuture.thenCombine(stockFuture,
                (price, stock) -> "price=" + price + ", stock=" + stock);
        System.out.println("thenCombine: " + quote.get(2, TimeUnit.SECONDS));
        System.out.println();
    }

    private static void allOfAnyOfDemo(ExecutorService pool) throws Exception {
        System.out.println("--- allOf / anyOf ---");
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 10, pool);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> 20, pool);
        CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> 30, pool);

        CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
        all.get(2, TimeUnit.SECONDS);
        List<Integer> results = Stream.of(f1, f2, f3).map(CompletableFuture::join).toList();
        System.out.println("allOf: all 3 tasks completed, results=" + results);

        CompletableFuture<String> fast = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(10);
            return "fast-task";
        }, pool);
        CompletableFuture<String> slow = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(500);
            return "slow-task";
        }, pool);
        Object firstDone = CompletableFuture.anyOf(fast, slow).get(2, TimeUnit.SECONDS);
        System.out.println("anyOf: fastest of 2 tasks completed first, result=" + firstDone);
        System.out.println();
    }

    private static void exceptionallyAndHandleDemo(ExecutorService pool) throws Exception {
        System.out.println("--- exceptionally / handle recovering from a failure ---");
        CompletableFuture<String> recovered = CompletableFuture
                .<String>supplyAsync(() -> {
                    throw new RuntimeException("downstream lookup failed");
                }, pool)
                .exceptionally(ex -> "unknown@example.com");   // recovers -- chain continues normally
        System.out.println("exceptionally: recovered from failure with fallback value = "
                + recovered.get(2, TimeUnit.SECONDS));

        CompletableFuture<String> handled = CompletableFuture
                .supplyAsync(() -> {
                    throw new RuntimeException("boom");
                }, pool)
                .handle((result, ex) -> ex != null ? "handled-failure:" + ex.getMessage() : "handled-success:" + result);
        System.out.println("handle: " + handled.get(2, TimeUnit.SECONDS));

        try {
            CompletableFuture<String> unrecovered = CompletableFuture.supplyAsync(() -> {
                throw new IllegalStateException("unrecovered failure");
            }, pool);
            unrecovered.get(2, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            System.out.println("unrecovered failure at get(): ExecutionException wraps -> "
                    + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void shutdownQuietly(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
