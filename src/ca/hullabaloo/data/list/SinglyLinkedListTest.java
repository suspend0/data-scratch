package ca.hullabaloo.data.list;

import junit.framework.TestCase;

import java.util.Iterator;

public class SinglyLinkedListTest extends TestCase {
    private SinglyLinkedList<String> list = new SinglyLinkedList<String>();

    public void testIsEmptyAtConstruction() {
        assertTrue(list.isEmpty());
    }

    public void testIsNotEmptyAfterAdd() {
        list.add("A");
        assertFalse(list.isEmpty());
    }

    public void testGetOne() {
        list.add("A");
        assertEquals("A", list.get(0));
    }

    public void testEmptyHasEmptyIterator() {
        assertFalse(list.iterator().hasNext());
    }

    public void testSingleItemIteration() {
        list.add("A");
        Iterator<String> iter = list.iterator();
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertFalse(iter.hasNext());
    }

    public void testMultipleItemIteration() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        Iterator<String> iter = list.iterator();
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("B", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("C", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("D", iter.next());
        assertFalse(iter.hasNext());
    }

    public void testAddAndGetMultiples() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
    }

    public void testReverseWhenEmpty() {
        list.reverse();
        assertTrue(list.isEmpty());
    }

    public void testReverseWithSingleItem() {
        list.add("A");
        list.reverse();
        assertEquals("A", list.get(0));
    }

    public void testReverseWithTwoItems() {
        list.add("A");
        list.add("B");
        list.reverse();
        assertEquals("B", list.get(0));
        assertEquals("A", list.get(1));
    }

    public void testReverseWithMultipleItems() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.reverse();
        assertEquals("D", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("B", list.get(2));
        assertEquals("A", list.get(3));
    }

    public void testGetWhenEmptyThrowsException() {
        try {
            list.get(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void testGetOutsideOfRangeThrowsException() {
        try {
            list.add("A");
            list.get(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
    }
}
