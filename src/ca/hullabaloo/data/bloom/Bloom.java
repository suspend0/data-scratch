package ca.hullabaloo.data.bloom;

import java.util.BitSet;

public class Bloom<E> {
    private static final int MIN_INITIAL_CAPACITY = 512;

    private final BitSet data;
    private final ThreadLocal<HashIter> bufs;

    public Bloom(Hash<E> hash, int minBits) {
        int dataSize = nextPowerOfTwo(minBits);
        this.data = new BitSet(dataSize);
        this.bufs = new Hashes(hash, dataSize);
    }

    public void add(E o) {
        HashIter hashes = bufs.get();
        hashes.reset(o);
        System.out.println(data.size());
        System.out.println(Integer.numberOfTrailingZeros(data.size()));
        //       System.out.println(Arrays.toString(buf));

        while (hashes.hasNext()) {
            int h = hashes.next();
            data.set(h);
            System.out.println("add h=" + h);
        }
    }

    public boolean probablyContains(E o) {
        HashIter hasher = bufs.get();
        hasher.reset(o);
        if (hasher.hasNext()) {
            do {
                int h = hasher.next();
                if (!data.get(h)) return false;
            } while (hasher.hasNext());
            return true;
        }
        return false;
    }

    private int nextPowerOfTwo(int requested) {
        // Copied from java.util.ArrayDeque#allocateElements, but it's a well-known algorithm
        int result = MIN_INITIAL_CAPACITY;
        // Find the best power of two to hold elements.
        // Tests "<=" because arrays aren't kept full.
        if (requested >= result) {
            result = requested;
            result |= (result >>> 1);
            result |= (result >>> 2);
            result |= (result >>> 4);
            result |= (result >>> 8);
            result |= (result >>> 16);
            result++;

            if (result < 0)   // Too many elements, must back off
                result >>>= 1;// Good luck allocating 2 ^ 30 elements
        }
        return result;
    }

    private static class Hashes extends ThreadLocal<HashIter> {
        private final Hash hash;
        private final int bitsPerHash;
        private final int maskPerHash;
        private final int valuesPerHash = 2;

        public Hashes(Hash hash, int dataSize) {
            this.hash = hash;
            this.bitsPerHash = Integer.numberOfTrailingZeros(dataSize);
            this.maskPerHash = dataSize - 1;
            if (this.bitsPerHash > Integer.SIZE)
                throw new IllegalArgumentException();
            if (this.valuesPerHash * bitsPerHash > hash.longSize() * Long.SIZE)
                throw new IllegalArgumentException("too few bits returned by hash function");
        }

        @Override
        protected HashIter initialValue() {
            return new HashIter(this.hash, this.valuesPerHash, this.bitsPerHash, this.maskPerHash);
        }
    }

    private static class HashIter {
        private final Hash hash;
        private final int valuesPerIter;
        private final int bitsPerHash;
        private final int maskPerHash;
        private final long[] buf;
        private int returnedValues = 0;

        public HashIter(Hash hash, int valuesPerIter, int bitsPerHash, int maskPerHash) {
            this.hash = hash;
            this.valuesPerIter = valuesPerIter;
            this.bitsPerHash = bitsPerHash;
            this.maskPerHash = maskPerHash;
            this.buf = new long[hash.longSize()];
        }

        // We're careful to only send E
        @SuppressWarnings({"unchecked"})
        public void reset(Object o) {
            this.returnedValues = 0;
            this.hash.hash(o, buf);
        }

        public boolean hasNext() {
            return this.returnedValues < this.valuesPerIter;
        }

        public int next() {
            this.returnedValues++;
            int h = (int) (buf[0] & maskPerHash);
            buf[0] >>>= bitsPerHash;
            return h;
        }

    }
}
