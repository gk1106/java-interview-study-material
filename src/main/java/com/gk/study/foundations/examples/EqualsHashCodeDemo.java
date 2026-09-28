package com.gk.study.foundations.examples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Demonstrates the equals/hashCode contract, and what breaks when hashCode() is forgotten. */
public class EqualsHashCodeDemo {

    public static void main(String[] args) {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);
        System.out.println("p1.equals(p2) = " + p1.equals(p2)
                + ", p1.hashCode()==p2.hashCode() = " + (p1.hashCode() == p2.hashCode()));

        Set<Point> points = new HashSet<>();
        points.add(new Point(0, 0));
        points.add(new Point(1, 1));
        points.add(new Point(0, 0)); // duplicate by value
        System.out.println("HashSet dedup size (expect 2 unique points among 3, one duplicate) = " + points.size());

        Map<BrokenPoint, String> brokenMap = new HashMap<>();
        brokenMap.put(new BrokenPoint(0, 0), "origin");
        System.out.println("Broken point (no hashCode override) lost in HashMap: lookup after re-creating equal key = "
                + brokenMap.get(new BrokenPoint(0, 0)));

        Map<Point, String> fixedMap = new HashMap<>();
        fixedMap.put(new Point(0, 0), "origin");
        System.out.println("Fixed point found in HashMap: lookup after re-creating equal key = \""
                + fixedMap.get(new Point(0, 0)) + "\"");
    }

    static final class Point {
        private final int x;
        private final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point p)) return false;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /** Deliberately broken: equals() overridden, hashCode() left at Object's identity default. */
    static final class BrokenPoint {
        private final int x;
        private final int y;

        BrokenPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BrokenPoint p)) return false;
            return x == p.x && y == p.y;
        }
        // hashCode() intentionally NOT overridden -> BUG demonstration
    }
}
