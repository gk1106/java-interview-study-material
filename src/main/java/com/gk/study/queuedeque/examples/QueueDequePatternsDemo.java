package com.gk.study.queuedeque.examples;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Self-contained illustrations of the five classic queue/deque DSA patterns covered in
 * notes/04-queue-deque/05-dsa-patterns-queue-deque.md. These are teaching illustrations, not the
 * graded exercises -- the exercises (in exercises/, solved in solutions/) practice the same
 * patterns on separate problems.
 */
public final class QueueDequePatternsDemo {

    private QueueDequePatternsDemo() {
    }

    public static void main(String[] args) {
        stackViaDequeDemo();
        System.out.println();
        bfsViaQueueDemo();
        System.out.println();
        monotonicDequeSlidingWindowMaxDemo();
        System.out.println();
        topKWithHeapDemo();
        System.out.println();
        mergeKSortedListsWithHeapDemo();
    }

    /** Pattern 1: use a Deque as a stack to check balanced brackets. */
    private static void stackViaDequeDemo() {
        System.out.println("== Pattern 1: stack via Deque -- valid parentheses ==");
        for (String s : List.of("({[]})", "(]", "(()")) {
            System.out.println("  isValid(\"" + s + "\") -> " + isValid(s));
        }
    }

    private static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        Map<Character, Character> closeToOpen = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.isEmpty() || stack.pop() != closeToOpen.get(c)) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    /** Pattern 2: BFS with a queue -- shortest number of steps in an unweighted grid. */
    private static void bfsViaQueueDemo() {
        System.out.println("== Pattern 2: BFS with a queue -- shortest path in a grid ==");
        int[][] grid = {
                {0, 0, 1, 0},
                {1, 0, 1, 0},
                {0, 0, 0, 0},
                {0, 1, 1, 0},
        };
        System.out.println("  shortest path length from (0,0) to (3,3): " + shortestPath(grid));
    }

    private static int shortestPath(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0, 0}); // row, col, distance
        visited[0][0] = true;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            int dist = current[2];
            if (r == rows - 1 && c == cols - 1) {
                return dist;
            }
            for (int[] d : directions) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc] && grid[nr][nc] == 0) {
                    visited[nr][nc] = true;
                    queue.offer(new int[]{nr, nc, dist + 1});
                }
            }
        }
        return -1;
    }

    /** Pattern 3: monotonic deque -- sliding window maximum in O(n) total. */
    private static void monotonicDequeSlidingWindowMaxDemo() {
        System.out.println("== Pattern 3: monotonic deque -- sliding window maximum ==");
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;
        System.out.println("  nums=" + Arrays.toString(nums) + " k=" + k);
        System.out.println("  window maxima -> " + Arrays.toString(slidingWindowMax(nums, k)));
    }

    private static int[] slidingWindowMax(int[] nums, int k) {
        int[] result = new int[nums.length - k + 1];
        Deque<Integer> indices = new ArrayDeque<>(); // holds indices, values strictly decreasing
        for (int i = 0; i < nums.length; i++) {
            while (!indices.isEmpty() && nums[indices.peekLast()] < nums[i]) {
                indices.pollLast();
            }
            indices.offerLast(i);
            if (indices.peekFirst() <= i - k) {
                indices.pollFirst();
            }
            if (i >= k - 1) {
                result[i - k + 1] = nums[indices.peekFirst()];
            }
        }
        return result;
    }

    /** Pattern 4: top-K with a heap -- top K frequent elements in O(n log k). */
    private static void topKWithHeapDemo() {
        System.out.println("== Pattern 4: top-K with a heap -- top K frequent elements ==");
        int[] nums = {1, 1, 1, 2, 2, 3, 4, 4, 4, 4};
        int k = 2;
        System.out.println("  nums=" + Arrays.toString(nums) + " k=" + k);
        System.out.println("  top " + k + " frequent -> " + topKFrequent(nums, k)
                + "  (heap only guarantees TOP-k membership, not a fully sorted result)");
    }

    private static List<Integer> topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int n : nums) {
            counts.merge(n, 1, Integer::sum);
        }
        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) {
                minHeap.poll(); // evict the current smallest-count entry, keeping only the top k
            }
        }
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : minHeap) {
            result.add(entry.getKey());
        }
        return result;
    }

    /** Pattern 5: merge K sorted lists with a heap -- O(n log k) instead of O(n*k). */
    private static void mergeKSortedListsWithHeapDemo() {
        System.out.println("== Pattern 5: merge K sorted lists with a heap ==");
        List<List<Integer>> lists = List.of(
                List.of(1, 4, 7),
                List.of(2, 5, 8, 9),
                List.of(0, 3, 6));
        System.out.println("  lists=" + lists);
        System.out.println("  merged -> " + mergeKSortedLists(lists));
    }

    private static List<Integer> mergeKSortedLists(List<List<Integer>> lists) {
        // heap of [value, listIndex, elementIndex]
        PriorityQueue<int[]> heap = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        for (int i = 0; i < lists.size(); i++) {
            if (!lists.get(i).isEmpty()) {
                heap.offer(new int[]{lists.get(i).get(0), i, 0});
            }
        }
        List<Integer> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            int[] top = heap.poll();
            result.add(top[0]);
            int listIndex = top[1];
            int nextElementIndex = top[2] + 1;
            if (nextElementIndex < lists.get(listIndex).size()) {
                heap.offer(new int[]{lists.get(listIndex).get(nextElementIndex), listIndex, nextElementIndex});
            }
        }
        return result;
    }
}
