package ca.hullabaloo.data.tree;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Random;

public class IntHeapTest extends TestCase {
    private IntHeap h = new IntHeap();

    public void testEmptyAtCreate() {
        assertTrue(h.isEmpty());
    }

    public void testAddOne() {
        h.add(1);
        assertEquals(1, h.first());
    }

    public void testAddTwo() {
        h.add(2);
        h.add(1);
        assertEquals(1, h.first());
    }

    public void testAddingDuplicates() {
        int[] vals = {8, 6, 7, 8, 6, 7, 5, 3, 0, 9, 5, 3, 0, 9};
        checkAddAndRemove(vals);
    }

    public void testAddTen() {
        int[] vals = {5, 6, 8, 2, 1, 3, 9, 0, 4, 7};
        for (int val : vals)
            h.add(val);
        assertEquals(0, h.first());
    }

    public void testRemoveOne() {
        h.add(1);
        assertEquals(1, h.remove());
    }

    public void testRemoveTwo() {
        h.add(2);
        h.add(1);
        assertEquals(1, h.remove());
        assertEquals(2, h.remove());
    }

    public void testSize() {
        int[] vals = {8, 6, 7, 5, 3, 0, 9};
        for (int i = 0; i < vals.length; i++) {
            assertEquals(i, h.size());
            h.add(vals[i]);
        }
        assertEquals(vals.length, h.size());
    }

    public void testRandom() {
        Random r = new Random();
        int[] vals = new int[r.nextInt(3000)];
        for(int i = 0;i<vals.length;i++)
            vals[i] = r.nextInt();
        checkAddAndRemove(vals);
    }

    private void checkAddAndRemove(int[] vals) {
        for (int val : vals)
            h.add(val);
        Arrays.sort(vals);
        for (int val : vals)
            assertEquals(val, h.remove());
    }
}
