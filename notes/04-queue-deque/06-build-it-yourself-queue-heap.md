# Build it yourself: MyCircularQueue, MyMinHeap, StackUsingTwoQueues

## 1. What it is

The best way to prove you actually understand circular-array queues (topic 2), binary heaps
(topic 3), and the two-queues-as-a-stack trick (topic 5) is to implement minimal, working
versions yourself: `MyCircularQueue` (fixed-capacity array-backed queue with wraparound),
`MyMinHeap<T>` (generic binary min-heap with sift-up/sift-down), and `StackUsingTwoQueues<T>` (a
LIFO stack built entirely on two FIFO queues).

## 2. How it works internally

### MyCircularQueue -- what to implement and why

Required API surface: `enqueue(int)` (returns `false` if full instead of throwing), `dequeue()`
(returns `false` if empty), `front()`/`rear()` (return `-1` if empty), `isEmpty()`, `isFull()`.
This mirrors the classic "Design Circular Queue" interview problem.

Design points:
- **Fixed-size backing array** (`int[] data`), plus a `head` index and a `count` of currently
  stored elements -- **not** a separate `tail` index. Using `count` instead of `tail` sidesteps
  the "does `head == tail` mean empty or full?" ambiguity that `ArrayDeque` (topic 2) solves
  instead by growing the array the instant it would occur. Here, capacity is fixed by design (no
  growth), so an explicit `count` is the simpler, more direct fix for the same underlying
  problem.
- **Insertion index**: `(head + count) % capacity` -- the next free slot after the current last
  element, wrapping around the end of the array.
- **`front()`/`rear()` on empty return `-1`** (matching the classic LeetCode "Design Circular
  Queue" contract) rather than throwing -- a deliberate API choice distinct from, e.g.,
  `Deque.peekFirst()` (returns `null`, works for reference types) since this variant is
  `int`-based (no boxing) and needs a primitive sentinel.

ASCII diagram -- capacity 3, after `enqueue(1) enqueue(2) enqueue(3) dequeue() dequeue()
enqueue(4) enqueue(5)`:
```
after enqueue 1,2,3:      [ 1 | 2 | 3 ]       head=0 count=3 (full)
                            ^head       ^ (insert index would be (0+3)%3=0, but full)
after dequeue, dequeue:   [ _ | _ | 3 ]       head=2 count=1
                                    ^head
after enqueue 4:          [ 4 | _ | 3 ]       head=2 count=2   (insertIndex=(2+1)%3=0)
                            ^insert     ^head
after enqueue 5:          [ 4 | 5 | 3 ]       head=2 count=3 (full again)
                                ^insert ^head
front() -> data[head=2] = 3
rear()  -> data[(head+count-1)%3] = data[(2+3-1)%3] = data[1] = 5
```

### MyMinHeap&lt;T&gt; -- what to implement and why

Required API surface: `insert(T)`, `extractMin()` (throws `NoSuchElementException` if empty),
`peek()` (throws if empty), `isEmpty()`, `size()`. Type bound: `T extends Comparable<T>`.

Design points:
- **Backing store**: a `List<T>` (an `ArrayList<T>` internally) representing the complete binary
  tree by index, exactly as described in topic 3 -- children of index `i` at `2i+1`/`2i+2`,
  parent at `(i-1)/2`. Using `List<T>` (rather than hand-rolling `Object[]` growth, already
  covered by `MyArrayList` in module 03-list) keeps the focus on the heap **algorithm**
  (sift-up/sift-down), which is the actual learning objective here.
- **`insert`**: append to the end of the list, then **sift up** -- compare with the parent;
  while the new value is smaller, swap with the parent and continue from the parent's old
  position; stop at the root or once the parent is smaller-or-equal.
- **`extractMin`**: save `heap.get(0)` to return; remove the **last** element and, if the heap
  isn't now empty, place it at index 0 and **sift down** -- repeatedly swap with whichever child
  is smaller (if smaller than the current node), until neither child is smaller or a leaf is
  reached.
- **Why swap with the *last* element, not just delete index 0 and shift everything left**:
  shifting would cost O(n) and destroy the complete-tree shape (indices must stay contiguous with
  no gaps for the `2i+1`/`2i+2` formulas to keep working); moving the last element into the
  vacated root and sifting it into place is O(log n) and preserves the shape.

### StackUsingTwoQueues&lt;T&gt; -- what to implement and why

Required API surface: `push(T)`, `pop()` (throws if empty), `top()` (throws if empty),
`isEmpty()`, `size()`.

Design points:
- Two possible designs trade cost between `push` and `pop`; this exercise uses **costly push, 
  O(1) pop/top**: `q1` always holds the stack in "pop order" (current top at its front).
- **`push(item)`**: offer `item` into `q2` first, then rotate every existing element out of
  `q1` into `q2` (in order, so they land *behind* the new item), then swap the `q1`/`q2`
  references. After this, `q1`'s front is the just-pushed item -- the new top -- in O(n).
- **`pop`/`top`**: just `q1.poll()`/`q1.peek()` -- O(1), since `q1` is always already arranged
  with the current top at the front.
- The alternative design (O(1) push, O(n) pop -- append to whichever queue is currently "active"
  on push, and on pop, drain all-but-the-last element from active to the other queue, swap, then
  return the last) is equally valid; this module picks costly-push for a single, consistent
  worked example, but recognizing **both** trade-offs exist is itself a common follow-up
  question.

ASCII diagram -- `push(1); push(2); push(3)` under the costly-push design:
```
push(1):  q2=[1]              rotate q1(empty) into q2 -> nothing to rotate
          swap -> q1=[1] q2=[]

push(2):  q2=[2]              rotate q1=[1] into q2 -> q2=[2,1]
          swap -> q1=[2,1] q2=[]

push(3):  q2=[3]              rotate q1=[2,1] into q2 -> q2=[3,2,1]
          swap -> q1=[3,2,1] q2=[]

pop() -> q1.poll() -> 3   (correct: most recently pushed comes out first, LIFO)
```

## 3. Complexity

| Operation | MyCircularQueue | MyMinHeap | StackUsingTwoQueues |
|---|---|---|---|
| insert (`enqueue`/`insert`/`push`) | O(1) | O(log n) | O(n) (rotates existing elements) |
| remove (`dequeue`/`extractMin`/`pop`) | O(1) | O(log n) | O(1) |
| peek (`front`/`rear`/`peek`/`top`) | O(1) | O(1) | O(1) |
| space | O(capacity), fixed | O(n), grows with insert | O(n) across both queues |

## 4. Example code

- Reference implementations (real, working code -- not stubs):
  `src/main/java/com/gk/study/queuedeque/solutions/MyCircularQueue.java`,
  `src/main/java/com/gk/study/queuedeque/solutions/MyMinHeap.java`,
  `src/main/java/com/gk/study/queuedeque/solutions/StackUsingTwoQueues.java`
- TODO stubs for you to attempt first:
  `src/main/java/com/gk/study/queuedeque/exercises/MyCircularQueueExercise.java`,
  `src/main/java/com/gk/study/queuedeque/exercises/MyMinHeapExercise.java`,
  `src/main/java/com/gk/study/queuedeque/exercises/StackUsingTwoQueuesExercise.java`

```java
MyCircularQueue q = new MyCircularQueue(3);
q.enqueue(1); q.enqueue(2); q.enqueue(3);
System.out.println(q.enqueue(4));  // false -- full
q.dequeue();
System.out.println(q.enqueue(4));  // true -- room again
System.out.println(q.front() + " " + q.rear());  // 2 4

MyMinHeap<Integer> heap = new MyMinHeap<>();
for (int v : new int[]{5, 3, 8, 1, 9, 2}) heap.insert(v);
System.out.println(heap.extractMin());  // 1
System.out.println(heap.extractMin());  // 2

StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
stack.push(1); stack.push(2); stack.push(3);
System.out.println(stack.pop());  // 3
System.out.println(stack.pop());  // 2
```
Expected console output:
```
false
true
2 4
1
2
3
2
```

## 5. When to use / when NOT to use

- Never use these in real production code -- `ArrayDeque`, `PriorityQueue`, and
  `java.util.concurrent`'s `ArrayBlockingQueue` are more complete, more optimized, and
  battle-tested. This exercise exists purely to force you to reason about wraparound index
  arithmetic, sift-up/sift-down mechanics, and cross-queue rotation with your own hands, which is
  exactly what "explain how ArrayDeque/PriorityQueue work" interview questions probe.
- Do build and run these when preparing for interviews at companies known to ask "implement your
  own circular buffer / heap / stack-from-queues" as a live-coding round.

## 6. Common pitfalls & gotchas

**Using `head == tail` to detect "full" in a fixed-capacity circular queue without a `count`
field** -- as covered in topic 2, that condition is ambiguous with "empty"; `MyCircularQueue`
sidesteps this entirely by tracking `count` explicitly instead of a `tail` index.

**Off-by-one in the insertion index formula** -- it must be `(head + count) % capacity`, not
`(head + count - 1) % capacity` (that would overwrite the last element) or `head % capacity`
(that would always overwrite the front).

**Forgetting to handle the "extract the last remaining heap element" edge case** -- when
`extractMin()` is called on a 1-element heap, there is no "last element to move into the vacated
root" other than the one being removed itself; `heap.remove(heap.size()-1)` followed by "if the
heap still has elements, sift down" correctly skips the sift-down step when the heap is now
empty (no root to fix).

**Rotating in the wrong order during `push` on `StackUsingTwoQueues`** -- the new item must be
offered into `q2` **before** rotating `q1`'s existing elements into `q2`, so it ends up at the
front (the new top); rotating first and offering the new item last would put it at the *back*
(the new bottom) instead, silently breaking LIFO order.

## 7. Interview questions

- [Basic] Why does `MyCircularQueue` use a `count` field instead of a `tail` index? → With a
  fixed capacity and no growth, `head == tail` would be ambiguous between "empty" and "full" (a
  fully wrapped-around queue can land back at the same indices as an empty one); tracking the
  logical element count directly avoids the ambiguity without needing `ArrayDeque`'s
  eager-growth trick. → Follow-up: *Could you instead keep both `tail` and a boolean `isFull`
  flag?* Yes -- functionally equivalent, just a different way of resolving the same ambiguity;
  a `count` field is simply the more common/idiomatic choice.
- [Basic] Why does `MyMinHeap` move the *last* element into the root during `extractMin`, rather
  than shifting every remaining element left by one? → Shifting would cost O(n) and, more
  importantly, would break the complete-binary-tree shape the array representation depends on
  for the `2i+1`/`2i+2` index formulas to remain valid; moving the last element into the root and
  sifting it down is O(log n) and preserves the shape. → Follow-up: *Why specifically the last
  element, and not any other?* Removing the last element never leaves a "hole" in the middle of
  the array -- the tree stays complete (no missing nodes before the new end) no matter which
  value ends up needing to sift down afterward.
- [Basic] In `StackUsingTwoQueues`'s costly-push design, why is `pop()` O(1) despite `push()`
  being O(n)? → Because all the rearrangement work happens during `push` -- by the time `pop` is
  called, `q1` is already arranged with the current top at its front, so `pop` is just
  `q1.poll()`, no rearrangement needed at removal time. → Follow-up: *Is the total work across n
  pushes and n pops the same either way (costly-push vs. costly-pop)?* Not necessarily equal in
  every sequence, but both designs are O(n) worst-case per single costly operation; which is
  "better" depends on whether your workload pushes or pops more frequently relative to the other.
- [Intermediate] Walk through `MyMinHeap.insert()`'s sift-up step by step. → Append the new
  value at the end of the backing list (this is the next available position in the complete
  tree); compute its parent index as `(i-1)/2`; while the new value is smaller than its parent,
  swap them and set `i` to the parent's index, repeating; stop when either the root is reached
  (`i == 0`) or the parent is no longer larger. → Follow-up: *What's the maximum number of swaps
  a single insert can cause?* O(log n) -- bounded by the tree's height, since each swap moves the
  element up exactly one level.
- [Intermediate] What would go wrong if `MyCircularQueue.enqueue` computed the insertion index as
  `count % capacity` instead of `(head + count) % capacity`? → It would ignore where the queue's
  logical front currently is after any prior dequeues -- once `head` has advanced past 0 (from
  earlier dequeues), `count % capacity` would insert at the wrong physical slot, potentially
  overwriting a still-occupied element or leaving a gap. → Follow-up: *Would this bug always be
  caught by a test that only enqueues without ever dequeuing first?* No -- it would only surface
  once at least one `dequeue()` has advanced `head` away from 0, which is exactly why the
  `wrapAroundReusesFreedSlots`-style test (dequeue some, then enqueue more) matters.
- [Intermediate] Why does `StackUsingTwoQueues.push` offer the new item into `q2` *before*
  rotating `q1`'s contents into `q2`, rather than after? → A queue is FIFO, so whatever is
  offered first comes out first; offering the new item first means it ends up at the **front**
  of `q2` (and thus, after the swap, at the front of the new `q1` -- i.e. the new stack top).
  Rotating `q1` first would put the new item at the **back**, making it the new stack *bottom*
  instead, which is backwards. → Follow-up: *Could you fix an "offer after rotate" mistake by
  changing which queue you read from on `pop`, instead of fixing the push order?* Not cleanly --
  you'd need to track which end holds the "top" and it would flip every push, adding
  complexity/bugs; fixing the push order is simpler and is the standard textbook solution.
- [Advanced] Compare the "costly push" and "costly pop" designs for `StackUsingTwoQueues` -- when
  would you actually prefer each? → Costly-push (used in this module) front-loads the
  rearrangement cost onto every `push`, keeping `pop`/`top` O(1) -- good when reads (`pop`/`top`)
  dominate writes. Costly-pop instead appends cheaply to one "active" queue on every push (O(1))
  and, on `pop`, drains all-but-the-last element from the active queue into the other queue, then
  returns the last (O(n)) -- good when pushes dominate pops. Neither changes the amortized bound
  meaningfully across a mixed workload; the choice matters mainly for which single operation's
  worst-case latency you want to minimize. → Follow-up: *Is there a design that makes both push
  and pop O(1) using only two plain FIFO queues (no other data structure)?* Not with just two
  plain queues and no extra bookkeeping (like an auxiliary size counter or index) -- a queue
  fundamentally can't produce LIFO order in O(1) both ways without paying the reversal cost
  somewhere; this is precisely why `ArrayDeque` (topic 2), not a queue-pair, is the right
  real-world tool for an actual O(1)-both-ways stack.
- [Advanced] Both `MyCircularQueue` and the real `ArrayDeque` solve the same underlying "how do I
  tell empty from full when head could equal tail" problem, but differently -- `MyCircularQueue`
  with an explicit `count`, `ArrayDeque` by growing eagerly. What's the trade-off? → An explicit
  `count` field costs 4 extra bytes per instance and one increment/decrement per operation, but
  works at any fixed capacity without needing to grow; `ArrayDeque`'s eager-growth approach avoids
  the extra field but requires the structure to be resizable (impossible for a genuinely
  fixed-capacity queue, where growing would violate the whole point of having a capacity limit).
  → Follow-up: *Could `ArrayDeque`'s approach (grow instead of track count) work for
  `MyCircularQueue` if it didn't need a hard capacity ceiling?* Yes -- if unbounded growth were
  acceptable, you could drop `count` and grow like `ArrayDeque` does; the fixed-capacity
  requirement (central to the "Design Circular Queue" problem this class mirrors) is exactly what
  rules that out here.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|---|---|---|---|
| B01 | Build it yourself | Implement a fixed-capacity circular queue from scratch | Circular array (head + count) | `exercises/MyCircularQueueExercise.java` |
| B02 | Build it yourself | Implement a generic binary min-heap from scratch | Binary heap (sift-up/sift-down) | `exercises/MyMinHeapExercise.java` |
| B03 | Build it yourself | Implement a LIFO stack using only two FIFO queues | Two queues (rotate on push) | `exercises/StackUsingTwoQueuesExercise.java` |

**B01 -- MyCircularQueue**
- Implement: `enqueue(int)` (false if full), `dequeue()` (false if empty), `front()`/`rear()`
  (-1 if empty), `isEmpty()`, `isFull()`.
- Constraint: every operation O(1) time, O(capacity) space.
- <details><summary>Hint</summary>Use an `int[] data`, a `head` index, and a `count` of
  currently stored elements; insertion index is `(head + count) % capacity`.</details>

**B02 -- MyMinHeap&lt;T&gt;**
- Implement: `insert(T)`, `extractMin()`, `peek()`, `isEmpty()`, `size()`, for
  `T extends Comparable<T>`.
- Constraint: `insert`/`extractMin` O(log n), `peek` O(1).
- <details><summary>Hint</summary>Back it with a `List<T>`; on insert, append then sift up
  (compare/swap with parent at `(i-1)/2`); on extractMin, swap the root with the last element,
  remove the old last slot, then sift down (compare/swap with the smaller child at
  `2i+1`/`2i+2`).</details>

**B03 -- StackUsingTwoQueues&lt;T&gt;**
- Implement: `push(T)`, `pop()`, `top()`, `isEmpty()`, `size()`.
- Constraint: pick either "costly push, O(1) pop" or "O(1) push, costly pop" -- this module's
  reference solution uses costly-push.
- <details><summary>Hint</summary>On push: offer the new item into the second queue first, then
  rotate every element out of the first queue into the second (preserving order), then swap the
  two queue references so the newest item ends up at the front.</details>

Solutions are the real implementations in the `solutions` package
(`solutions/MyCircularQueue.java`, `solutions/MyMinHeap.java`,
`solutions/StackUsingTwoQueues.java`) -- attempt the stubs in `exercises/` first.

## 9. Quick recap

- `MyCircularQueue`: fixed array + `head` + `count` (not `tail`) sidesteps the empty-vs-full
  ambiguity that a bare `head == tail` check would create.
- `MyMinHeap`: array/list-backed complete binary tree; insert = append + sift-up, extractMin =
  move-last-to-root + sift-down, both O(log n) because they're bounded by tree height.
- `StackUsingTwoQueues` (costly-push design): rotate everything through a second queue on every
  push so the newest item always ends up at the front, making pop/top O(1).
- All three exist to make the internals from topics 2, 3, and 5 concrete by hand -- never use
  them over the real JDK classes in production.
- Recognizing which cost you're willing to pay where (push vs. pop, count field vs. eager
  growth) is itself a common interview follow-up across all three.
