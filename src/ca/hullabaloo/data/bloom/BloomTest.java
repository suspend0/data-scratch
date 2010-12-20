package ca.hullabaloo.data.bloom;

import junit.framework.TestCase;

public class BloomTest extends TestCase {
    private final Bloom<String> b = new Bloom<String>(new JavaObjectHash<String>(), 1200);

    public void testEmptyDoesNotContain() {
        assertFalse(b.probablyContains("x"));
    }

    public void testAdd() {
        b.add("foo");
        assertTrue(b.probablyContains("foo"));
    }

    public void testFoo() {
        int v = new Object().hashCode();
        int l = 2048 - 1;
        System.out.println(toBinaryString(v));
        System.out.println(toBinaryString(l));
        System.out.println(toBinaryString(v & l));
        System.out.println(toBinaryString(v >>> 11));
    }

    String toBinaryString(int v) {
        String s = Integer.toBinaryString(v);
        while (s.length() < Integer.SIZE)
            s = "0" + s;
        return s;
    }
}
