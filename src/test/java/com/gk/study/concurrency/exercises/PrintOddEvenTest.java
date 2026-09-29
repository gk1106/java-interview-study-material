package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link PrintOddEven} exercise stub. EXPECTED TO FAIL until implemented.
 *
 * <p>Every case asserts an exact ascending sequence -- correctness here is a property of the
 * synchronization itself (strict alternation), not timing, so the expected result is exact and
 * deterministic, not a range or "eventually consistent" check. Bounded by {@code @Timeout} so a
 * broken (deadlocking) implementation fails fast.
 */
class PrintOddEvenTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void alternatesStrictlyForSmallN() {
        PrintOddEven printer = new PrintOddEven();
        assertThat(printer.run(6)).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void handlesOddUpperBound() {
        PrintOddEven printer = new PrintOddEven();
        assertThat(printer.run(5)).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void handlesSingleElement() {
        PrintOddEven printer = new PrintOddEven();
        assertThat(printer.run(1)).containsExactly(1);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void handlesZeroAsEmpty() {
        PrintOddEven printer = new PrintOddEven();
        assertThat(printer.run(0)).isEmpty();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void handlesLargerN() {
        PrintOddEven printer = new PrintOddEven();
        int n = 200;
        List<Integer> expected = IntStream.rangeClosed(1, n).boxed().toList();
        assertThat(printer.run(n)).containsExactlyElementsOf(expected);
    }
}
