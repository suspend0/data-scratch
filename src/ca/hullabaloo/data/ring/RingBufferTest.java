package ca.hullabaloo.data.ring;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class RingBufferTest extends TestCase {
    private ExecutorService exec = Executors.newCachedThreadPool();
    private RingBuffer<String> rb;

    public void setUp() {
        rb = new RingBuffer<String>(10);
    }

    public void tearDown() {
        exec.shutdown();
    }

    public void testOneItem() throws Exception {
        RingBuffer<String>.Consumer c = rb.newConsumer();
        String expected = "hi";
        rb.producer().put(expected);
        String actual = c.get();
        assertEquals(expected, actual);
    }

    public void testTwoConsumers() throws Exception {
        String[] expected = {"hi", "there", "man"};
        rb.producer().putAll(expected);
        RingBuffer<String>.Consumer c1 = rb.newConsumer();
        RingBuffer<String>.Consumer c2 = rb.newConsumer();
        String[] actual1 = getAll(c1, expected.length);
        String[] actual2 = getAll(c2, expected.length);
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    public void testTwoConcurrentConsumers() throws Exception {
        String[] items = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
        Future<String[]> r1 = exec.submit(new DoConsume(rb, items.length));
        Future<String[]> r2 = exec.submit(new DoConsume(rb, items.length));

        rb.producer().putAll(items);
        assertEquals(items, r1.get(1, TimeUnit.SECONDS));
        assertEquals(items, r2.get(1, TimeUnit.SECONDS));
    }

    public void testBulkGet() throws Exception {
        String[] firstTwo = {"a","b"};
        String[] middleFour = {"c", "d", "e", "f"};
        String[] lastThree = {"g", "h", "i"};

        String[] buffer = new String[4];

        RingBuffer<String>.Producer p = rb.producer();
        RingBuffer<String>.Consumer c = rb.newConsumer();
        p.putAll(firstTwo);
        p.putAll(middleFour);
        p.putAll(lastThree);

        // get the first two, just to offset
        assertEquals(firstTwo[0],c.get());
        assertEquals(firstTwo[1],c.get());

        // get the next 4
        int got1 = c.get(buffer);
        assertEquals(middleFour, buffer);
        assertEquals(buffer.length, got1);

        // get the last 3, same buffer
        int got2 = c.get(buffer);
        assertEquals(lastThree, Arrays.copyOf(buffer,3));
        assertEquals(3, got2);
    }

    public void testMoreBulkGet() throws Exception {
        final RingBuffer<String>.Producer p = rb.producer();
        final RingBuffer<String>.Consumer c = rb.newConsumer();
        final String[] buffer = new String[4];

        class PutAndCheckGet {
            PutAndCheckGet(String...items) throws InterruptedException {
                p.putAll(items);
                List<String> d = new ArrayList<String>(Arrays.asList(items));
                while(!d.isEmpty()) {
                    int expectedCount = Math.min(d.size(),buffer.length);
                    List<String> expected = d.subList(0,expectedCount);
                    int actualCount = c.get(buffer);
                    List<String> actual = Arrays.asList(buffer).subList(0,actualCount);
                    assertEquals(expectedCount,actualCount);
                    assertEquals(expected,actual);
                    // shortens the main list
                    expected.clear();
                }
            }
        }

        new PutAndCheckGet("a","b");
        new PutAndCheckGet("c","d");
        // loop producer pointer around
        String[] bunch = new String[rb.capacity()-2];
        for(int i = 0; i < bunch.length;i++)
            bunch[i] = String.format("bunch#%03d",i);
        new PutAndCheckGet(bunch);
        new PutAndCheckGet("e","f","g","h","i","j");
    }

    public void testX() {
        int size = 16;

        doTestDiff(size, 0, 12, 12);
        doTestDiff(size, 14, 3, 5);
        doTestDiff(1 << 30, (1 << 30) - 2, 3, 5);
        doTestDiff(1 << 30, (1 << 30) - 2, (1 << 30) - 3, (1 << 30) - 1);
    }

    private void doTestDiff(int capacity, int lower, int upper, int diff) {
        int r;
        if (upper > lower) r = upper - lower;
        else r = upper - lower + capacity;
        assertEquals(diff, r);
    }

    public void testFull() throws Exception {
        RingBuffer<String>.Producer p = rb.producer();
        RingBuffer<String>.Consumer c = rb.newConsumer();
        // one less than capacity b/c consumer takes a slot regardless
        String[] items = new String[rb.capacity()-1];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.format("item#%04d", i);
            p.put(items[i]);
        }
        String[] actual = getAll(c, items.length);
        assertEquals(items, actual);
    }

    public void testDoubleCapacity() throws Exception {
        RingBuffer<String>.Producer p = rb.producer();
        RingBuffer<String>.Consumer c = rb.newConsumer();
        for (int i = 0; i < 2 * rb.capacity(); i++) {
            String expected = String.format("itemA#%04d", i);
            p.put(expected);
            String actual = c.get();
            assertEquals(expected, actual);
        }
    }

    private static String[] getAll(RingBuffer<String>.Consumer c, int count) throws Exception {
        String[] result = new String[count];
        for (int i = 0; i < result.length; i++) {
            result[i] = c.get();
        }
        return result;
    }

    private static void assertEquals(String[] a, String[] b) {
        assertEquals(Arrays.asList(a), Arrays.asList(b));
    }

    public void testPowerOfTwo13() {
        doTestNextPowerOfTwo(13, 16);
    }

    public void testPowerOfTwo16() {
        doTestNextPowerOfTwo(16, 16);
    }

    public void testPowerOfTwoMAX() {
        doTestNextPowerOfTwo(Integer.MAX_VALUE, 1 << 30);
    }

    private void doTestNextPowerOfTwo(int value, int expected) {
        System.out.println("value: " + value + " expected=" + expected);
        int actual = RingBuffer.calculatePowerOfTwoCapacity(value);
        assertEquals(0, actual & (actual - 1));
        assertEquals(expected, actual);
    }

    private static class DoConsume implements Callable<String[]> {
        private final RingBuffer<String>.Consumer c;
        private final int count;

        public DoConsume(RingBuffer<String> rb, int count) {
            this.c = rb.newConsumer();
            this.count = count;
        }

        public String[] call() throws Exception {
            return getAll(c, count);
        }
    }
}
