package ca.hullabaloo.data.list;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * fundamentals
 */
public class SinglyLinkedList<E> {
    private Node head = new Node(null);
    private Node tail = head;

    public void add(E element) {
        tail.next = new Node(element);
        tail = tail.next;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public Object get(int index) {
        checkIndex(index >= 0, "index < 0");
        Node current = head.next;
        for (int i = 0; i < index && current != null; i++) {
            current = current.next;
        }
        checkIndex(current != null, "index > size");
        //noinspection ConstantConditions
        return current.val;
    }

    public Iterator<E> iterator() {
        return new Iter();
    }

    public void reverse() {
        Node current = head.next;
        Node next;
        Node result = null;

        while (current != null) {
            next = current.next;
            current.next = result;
            result = current;
            current = next;
        }

        head.next = result;
    }

    private static void checkIndex(boolean cond, String msg) {
        if (!cond) throw new IndexOutOfBoundsException(msg);
    }

    private class Iter implements Iterator<E> {
        Node current = head;

        public boolean hasNext() {
            return current.next != null;
        }

        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            current = current.next;
            return current.val;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class Node {
        private final E val;

        public Node next;

        public Node(E val) {
            this.val = val;
        }

    }
}
