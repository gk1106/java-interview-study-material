package com.gk.study.concurrency.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PrintOddEvenSolutionTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void alternatesStrictlyForSmallN() {
        PrintOddEvenSolution printer = new PrintOddEvenSolution();
        assertThat(printer.run(6)).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void handlesOddUpperBound() {
        PrintOddEvenSolution printer = new PrintOddEvenSolution();
        assertThat(printer.run(5)).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void handlesSingleElement() {
        PrintOddEvenSolution printer = new PrintOddEvenSolution();
        assertThat(printer.run(1)).containsExactly(1);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void handlesZeroAsEmpty() {
        PrintOddEvenSolution printer = new PrintOddEvenSolution();
        assertThat(printer.run(0)).isEmpty();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void handlesLargerN() {
        PrintOddEvenSolution printer = new PrintOddEvenSolution();
        int n = 200;
        List<Integer> expected = IntStream.rangeClosed(1, n).boxed().toList();
        assertThat(printer.run(n)).containsExactlyElementsOf(expected);
    }
}
