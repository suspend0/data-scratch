package ca.hullabaloo.data.ring;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * A single-producer, multiple consumer ring buffer based on the description given in this talk
 * http://www.infoq.com/presentations/LMAX
 */
public class RingBuffer<E> {
    private static final long STARTING_INDEX = 0;
    private final E[] array;
    private final Producer producer = new Producer();
    private final Consumers consumers = new Consumers();

    private static void sleepAndYield(int loop) throws InterruptedException {
        if (loop == 0) {
            Thread.yield();
        } else {
            Thread.sleep(loop * 10);
        }
    }

    static int calculatePowerOfTwoCapacity(final int capacity) {
        int result = Integer.highestOneBit(capacity);
        if (result == capacity)
            return result;
        result <<= 1;
        if (result < 0)
            result = 1 << 30;
        return result;
    }


    public RingBuffer(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must be > 0:" + capacity);
        capacity = calculatePowerOfTwoCapacity(capacity);
        // no generic array creation in java
        @SuppressWarnings({"unchecked"})
        E[] temp = (E[]) new Object[capacity];
        this.array = temp;
    }

    public Producer producer() {
        return this.producer;
    }

    private int slot(long serial) {
        return checkedCast(serial & array.length - 1);
    }

    static int checkedCast(long value) {
        int result = (int) value;
        if (result != value)
            throw new IllegalArgumentException(value + " out of range");
        return result;
    }

    public synchronized Consumer newConsumer() {
        return this.consumers.create();
    }

    public int capacity() {
        return array.length;
    }

    public final class Producer {
        private volatile long lastProduced = STARTING_INDEX;
        private int sleeps = 0;

        public boolean putAll(E[] items) throws InterruptedException {
            for (int i = 0; i < 100; i++) {
                if (items.length > available()) {
                    sleepAndYield(i);
                } else {
                    final long lastProduced = this.lastProduced;
                    int nextSlot = slot(lastProduced + 1);
                    int len = Math.min(array.length - nextSlot, items.length);
                    System.arraycopy(items, 0, array, nextSlot, len);
                    int rem = items.length - len;
                    System.arraycopy(items, len, array, 0, rem);
                    this.lastProduced += items.length;
                    return true;
                }
            }
            return false;
        }

        public boolean put(E item) throws InterruptedException {
            long nextProduced = lastProduced + 1;
            int nextSlot = slot(nextProduced);
            for (int i = 0; i < 10; i++) {
                int consumedSlot = slot(lastConsumed());
                if (nextSlot == consumedSlot) {
                    sleepAndYield(i);
                    sleeps++;
                } else {
                    array[nextSlot] = item;
                    lastProduced = nextProduced;
                    return true;
                }
            }
            return false;
        }

        int sleeps() {
            return this.sleeps;
        }

        private int available() {
            int p = slot(this.lastProduced);
            int c = slot(this.lastConsumed());
            if (c > p) {
                return c - p;
            } else {
                return c - p + array.length;
            }
        }

        private long lastConsumed() {
            return RingBuffer.this.consumers.lastConsumed();
        }
    }

    public final class Consumer {
        private volatile long lastConsumed = STARTING_INDEX;
        private int sleeps = 0;

        public E get() throws InterruptedException {
            int consumedSlot = slot(lastConsumed);
            int producedSlot = slot(produced());
            int i = 0;
            while (consumedSlot == producedSlot) {
                sleepAndYield(i++);
                sleeps++;
                producedSlot = slot(produced());
            }
            lastConsumed += 1;
            return array[slot(lastConsumed)];
        }

        public int get(E[] buffer) throws InterruptedException {
            final long lastConsumed = this.lastConsumed;
            int consumedSlot = slot(lastConsumed);
            int producedSlot = slot(produced());
            int i = 0;
            while (consumedSlot == producedSlot) {
                sleepAndYield(i++);
                sleeps++;
                producedSlot = slot(produced());
            }
            final int count;
            if (consumedSlot < producedSlot) {
                // size = 12, consumed = 5, produced = 10, ready => {6-10} (5 items)
                count = Math.min(buffer.length, producedSlot - consumedSlot);
                System.arraycopy(array, consumedSlot + 1, buffer, 0, count);
            } else /* consumedSlot > producedSlot */ {
                // size = 12, consumed = 9, produced = 3, ready => {10-11},{0,3} (5 items)
                int count1 = array.length - 1 - consumedSlot;
                count1 = Math.min(buffer.length, count1);
                System.arraycopy(array, consumedSlot + 1, buffer, 0, count1);
                int count2 = producedSlot;
                count2 = Math.min(buffer.length - count1, count2);
                System.arraycopy(array, 0, buffer, count1, count2);
                count = count1 + count2;
            }
            this.lastConsumed = lastConsumed + count;
            return count;
        }

        public int sleeps() {
            return this.sleeps;
        }

        private long produced() {
            return RingBuffer.this.producer.lastProduced;
        }
    }

    private final class Consumers {
        // Can't figure out a way to do this statically. (Generic array creation)
        @SuppressWarnings({"unchecked"})
        private volatile Consumer[] consumers = (Consumer[])
                Array.newInstance(Consumer.class, 0);

        public Consumer create() {
            Consumer c = new Consumer();
            addToArray(c);
            return c;
        }

        private long lastConsumed() {
            Consumer[] a = this.consumers;
            int len = a.length;
            if (len == 0) return producer.lastProduced - 1;
            long minSoFar = a[0].lastConsumed;
            for (int i = a.length - 1; i > 0; i--) {
                minSoFar = Math.min(minSoFar, a[i].lastConsumed);
            }
            return minSoFar;
        }

        private void addToArray(Consumer c) {
            Consumer[] copy = Arrays.copyOf(this.consumers, this.consumers.length + 1);
            copy[copy.length - 1] = c;
            this.consumers = copy;
        }
    }
}
