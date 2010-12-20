package ca.hullabaloo.data.ring;

import junit.framework.TestCase;

import java.util.concurrent.*;

public class RingBufferSoakTest extends TestCase {
    private ExecutorService exec;

    public void testTwoConsumers() throws Exception {
        final int CAPACITY = 100 * 1000;
        final int RUN_SIZE = 20 * 1000 * 1000;
        final RingBuffer<Integer> rb = new RingBuffer<Integer>(CAPACITY);

        class C implements Callable<R> {
            private String name;

            public C(String name) {
                this.name = name;
            }

            public R call() throws Exception {
                Thread.currentThread().setName(name);
                RingBuffer<Integer>.Consumer c = rb.newConsumer();
                assertEquals(0, c.get().intValue());
                R data = new R().start();
                for (int i = 1; i < RUN_SIZE; i++) {
                    data.ops++;
                    assertEquals(i, c.get().intValue());
                }
                data.stop();
                data.sleeps = c.sleeps();
                return data;
            }
        }

        class P implements Callable<R> {
            private String name;

            public P(String name) {
                this.name = name;
            }

            public R call() throws Exception {
                Thread.currentThread().setName(name);
                RingBuffer<Integer>.Producer p = rb.producer();
                R data = new R().start();
                for (int i = 0; i < RUN_SIZE; i++) {
                    data.ops++;
                    p.put(i);
                }
                data.stop();
                data.sleeps = p.sleeps();
                return data;
            }
        }

        Future<R> c1 = exec.submit(new C("consumer1"));
        Future<R> c2 = exec.submit(new C("consumer2"));
        Future<R> p = exec.submit(new P("producer"));
        stats("consumer 1", c1.get(1, TimeUnit.MINUTES));
        stats("consumer 2", c2.get(1, TimeUnit.MINUTES));
        stats("producer", p.get(1, TimeUnit.MINUTES));
    }

    class R {
        long elapsed;
        long ops;
        long sleeps;

        R start() {
            elapsed = System.nanoTime();
            return this;
        }

        void stop() {
            elapsed = System.nanoTime() - elapsed;
        }
    }

    public void testTwoBulkConsumers() throws Exception {
        final int CAPACITY = 100 * 1000;
        final int RUN_SIZE = 20 * 1000 * 1000;
        final RingBuffer<Integer> rb = new RingBuffer<Integer>(CAPACITY);

        class C implements Callable<R> {
            private String name;

            public C(String name) {
                this.name = name;
            }

            public R call() throws Exception {
                Thread.currentThread().setName(name);
                R result = new R().start();
                RingBuffer<Integer>.Consumer c = rb.newConsumer();
                Integer[] buffer = new Integer[500];
                assertEquals(0, c.get().intValue());
                result.elapsed = System.nanoTime();
                for (int i = 1; i < RUN_SIZE;) {
                    int got = c.get(buffer);
                    result.ops++;
                    for (int j = 0; j < got; j++)
                        assertEquals(i++, buffer[j].intValue());
                }
                result.stop();
                result.sleeps = c.sleeps();
                return result;
            }
        }

        class P implements Callable<R> {
            private String name;

            public P(String name) {
                this.name = name;
            }

            public R call() throws Exception {
                Thread.currentThread().setName(name);
                RingBuffer<Integer>.Producer p = rb.producer();
                Integer[] buffer = new Integer[500];
                R result = new R().start();
                int j = 0;
                for (int i = 0; i < RUN_SIZE; i++) {
                    buffer[j++] = i;
                    if (j == buffer.length) {
                        result.ops++;
                        p.putAll(buffer);
                        j = 0;
                    }
                }
                result.stop();
                result.sleeps = p.sleeps();
                return result;
            }
        }

        Future<R> c1 = exec.submit(new C("consumer1"));
        Future<R> c2 = exec.submit(new C("consumer2"));
        Future<R> p = exec.submit(new P("producer"));
        stats("producer", p.get(1, TimeUnit.MINUTES));
        stats("consumer 1", c1.get(1, TimeUnit.MINUTES));
        stats("consumer 2", c2.get(1, TimeUnit.MINUTES));
    }

    private static void stats(String label, R result) {
        System.out.printf("%s completed in %d msecs, %d ops with %d sleeps\n",
                label, TimeUnit.NANOSECONDS.toMillis(result.elapsed),
                result.ops, result.sleeps);
    }

    public void setUp() {
        this.exec = Executors.newCachedThreadPool();
    }

    public void tearDown() {
        this.exec.shutdown();
    }
}
