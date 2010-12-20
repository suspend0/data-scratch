package ca.hullabaloo.data.bloom;

public class JavaObjectHash<T> implements Hash<T> {
    public int longSize() {
        return 1;
    }

    public void hash(T object, long[] result) {
        int h1 = object.hashCode();
        int h2 = h1;
        h2 += (h2 << 15) ^ 0xffffcd7d;
        h2 ^= (h2 >>> 10);
        h2 += (h2 << 3);
        h2 ^= (h2 >>> 6);
        h2 += (h2 << 2) + (h2 << 14);
        result[0] = (long) h1 | ((long) h2 << 32);
    }
}
