package com.gk.study.list.examples;

import com.gk.study.list.solutions.MyArrayList;
import com.gk.study.list.solutions.MySinglyLinkedList;

/**
 * Demonstrates the from-scratch {@link MyArrayList} and {@link MySinglyLinkedList}
 * implementations built in notes/03-list/09-build-it-yourself-mylist.md.
 */
public final class BuildYourOwnListDemo {

    private BuildYourOwnListDemo() {
    }

    public static void main(String[] args) {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("A");
        list.add("B");
        list.add(1, "X");
        System.out.println(list);          // [A, X, B]
        System.out.println(list.get(2));   // B

        MySinglyLinkedList<Integer> ll = new MySinglyLinkedList<>();
        ll.addLast(1);
        ll.addLast(2);
        ll.addFirst(0);
        System.out.println(ll);            // [0, 1, 2]
    }
}
