package ca.hullabaloo.data.tree;

import java.util.Arrays;

/**
 * Binary heap for integers
 */
public class IntHeap {
    private static final int INITIAL_CAPACITY = 8;

    private int[] data = new int[INITIAL_CAPACITY];
    private int size;

    /**
     * True if the heap is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * How many items in the heap
     */
    public int size() {
        return size;
    }

    public int first() {
        return data[0];
    }

    /**
     * Adds an item to the list
     */
    public void add(int val) {
        growIfNeeded();
        int idx = size;
        data[size++] = val;
        while (idx > 0) {
            idx = swapUp(idx);
        }
    }

    /**
     * removes and returns the first element in the list
     */
    public int remove() {
        int r = data[0];
        data[0] = data[--size];
        int idx = 0;
        while (idx < size) {
            idx = swapDown(idx);
        }
        return r;
    }

    /**
     * pulls an item up the tree to its proper spot
     */
    private int swapUp(int ci) {
        int pi = parentIdx(ci);
        int[] x = data;
        if (x[ci] < x[pi]) {
            swap(x, ci, pi);
            return pi;
        } else {
            return -1;
        }
    }

    /**
     * pushes an item in the tree down to its proper slot
     */
    private int swapDown(int parent) {
        int left = leftChildIdx(parent);
        int right = rightChildIdx(parent);
        int[] x = data;
        if (left < size && right < size) {
            if (x[left] < x[right]) {
                if (x[parent] > x[left]) {
                    swap(x, parent, left);
                    return left;
                }
            } else {
                if (x[parent] > x[right]) {
                    swap(x, parent, right);
                    return right;
                }
            }
        } else if (left < size) {
            if (x[parent] > x[left]) {
                swap(x, parent, left);
                return left;
            }
        } else if (right < size) {
            if (x[parent] > x[right]) {
                swap(x, parent, right);
                return right;
            }
        }
        return size;
    }

    private int leftChildIdx(int idx) {
        return 2 * idx + 1;
    }

    private int rightChildIdx(int idx) {
        return 2 * idx + 2;
    }

    private int parentIdx(int idx) {
        return (idx - 1) / 2;
    }

    private void growIfNeeded() {
        if (size == data.length)
            data = Arrays.copyOf(data, data.length * 2);
    }

    private static void swap(int x[], int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
