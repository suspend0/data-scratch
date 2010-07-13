package ca.hullabaloo.data.tree;

import java.nio.charset.Charset;
import java.util.Arrays;

public class PatriciaTrie<V> {
    private static final Charset utf8 = Charset.forName("UTF-8");

    private static final byte[] EMPTY = {};
    private static final Node[] NO_CHILDREN = {};
    private Node root = new Node();

    public PatriciaTrie() {
    }

    public boolean isEmpty() {
        return root.children.length == 0;
    }

    public void put(String key, V value) {
        byte[] bytes = key.getBytes(utf8);
        Node n = insert(root, 0, bytes, 0);
        n.value = value;
    }

    private enum Rel {
        EQ, LT, GT;

        static Rel of(byte a, byte b) {
            return a == b ? EQ : (a > b ? GT : LT);
        }
    }

    private Node insert(Node current, int edgeConsumed, byte[] key, int keyConsumed) {
        INSERT:
        while (true) {
            // match as much as you can from the current edge
            for (int edgeLen = current.edge.length, keyLen = key.length;
                 edgeConsumed < edgeLen && keyConsumed < keyLen;
                 edgeConsumed++, keyConsumed++) {
                if (key[keyConsumed] != current.edge[edgeConsumed])
                    break;
            }

            boolean edgeCompletelyConsumed = edgeConsumed == current.edge.length;
            boolean keyCompletelyConsumed = keyConsumed == key.length;

            // if consumed entire edge BUT key has bytes left, find child edge
            if (edgeCompletelyConsumed && !keyCompletelyConsumed) {
                for (int i = 0, N = current.children.length; i < N; i++) {
                    Node child = current.children[i];
                    switch (Rel.of(key[keyConsumed], child.edge[0])) {
                        case LT:
                            return current.insertEdge(i, tail(key, keyConsumed));
                        case GT:
                            break;
                        case EQ:
                            current = child;
                            edgeConsumed = 1;
                            keyConsumed++;
                            continue INSERT;
                    }
                }
                return current.appendEdge(tail(key, keyConsumed));
            }

            // if we consumed the key but not the edge, split
            if (keyCompletelyConsumed && !edgeCompletelyConsumed) {
                return current.split(edgeConsumed);
            }

            // if we couldn't consume the entire edge, split
            if (!edgeCompletelyConsumed) {
                return current.splitAndAdd(edgeConsumed, tail(key, keyConsumed));
            }

            // if consumed BOTH entire edge AND key, we found existing
            if (edgeCompletelyConsumed && keyCompletelyConsumed) {
                return current;
            }

            throw new AssertionError();
        }
    }

    private byte[] tail(byte[] bytes, int skip) {
        assert (skip < bytes.length);
        return Arrays.copyOfRange(bytes, skip, bytes.length);
    }

    public Object get(String key) {
        Node first = find(root, 0, key.getBytes(utf8), 0);
        return first == null ? null : first.value;
    }

    private Node find(Node current, int edgeConsumed, byte[] key, int keyConsumed) {
        FIND:
        while (true) {
            // match as much as you can from the current edge
            for (int N = Math.min(current.edge.length, key.length);
                 edgeConsumed < N;
                 edgeConsumed++, keyConsumed++) {
                if (key[keyConsumed] != current.edge[edgeConsumed])
                    break;
            }

            boolean edgeCompletelyConsumed = edgeConsumed == current.edge.length;
            boolean keyCompletelyConsumed = keyConsumed == key.length;

            // if consumed entire edge BUT key has bytes left, find child edge
            if (edgeCompletelyConsumed && !keyCompletelyConsumed) {
                // TODO: binary search
                for (int i = 0, N = current.children.length; i < N; i++) {
                    Node child = current.children[i];
                    switch (Rel.of(key[keyConsumed], child.edge[0])) {
                        case LT:
                            // all children GT me
                            return null;
                        case GT:
                            // look next
                            break;
                        case EQ:
                            // look down
                            current = child;
                            edgeConsumed = 1;
                            keyConsumed++;
                            continue FIND;
                    }
                }
                // no possible children found
                return null;
            }

            // if we couldn't consume the entire edge, not found
            if (!edgeCompletelyConsumed) {
                return null;
            }

            // if consumed BOTH entire edge AND key, we found existing
            if (edgeCompletelyConsumed && keyCompletelyConsumed) {
                return current;
            }

            throw new AssertionError();
        }
    }

    public String debug() {
        StringBuilder buf = new StringBuilder();
        debug(buf, 0, root);
        return buf.toString();
    }

    private void debug(StringBuilder buf, int level, Node current) {
        char nl = '\n';
        for (int i = 0; i < level; i++) buf.append(' ').append(' ');
        buf.append(current.edge.length == 0 ? "-" : new String(current.edge, utf8));
        buf.append(" (").append(current.value).append(')');
        buf.append(nl);
        for (Node n : current.children) {
            debug(buf, level + 1, n);
        }
    }

    private static class Node {
        private byte[] edge = EMPTY;
        private Object value;
        private Node[] children = NO_CHILDREN;

        public String toString() {
            return new String(edge, utf8);
        }

        public Node appendEdge(byte[] newEdge) {
            Node n = new Node();
            n.edge = newEdge;
            children = Arrays.copyOf(children, children.length + 1);
            children[children.length - 1] = n;
            return n;
        }

        public Node insertEdge(int childPos, byte[] newEdge) {
            Node n = new Node();
            n.edge = newEdge;
            Node[] existing = children;
            children = new Node[children.length + 1];
            System.arraycopy(existing, 0, children, 0, childPos);
            System.arraycopy(existing, childPos, children, childPos + 1, existing.length - childPos);
            children[childPos] = n;
            return n;
        }

        public Node splitAndAdd(int splitAt, byte[] newEdge) {
            assert newEdge.length > 0;

            Node child = new Node();
            child.edge = newEdge;

            Node split = new Node();
            split.edge = Arrays.copyOfRange(this.edge, splitAt, this.edge.length);

            this.edge = Arrays.copyOf(this.edge, splitAt);
            split.children = this.children;

            split.value = this.value;
            this.value = null;

            switch (Rel.of(child.edge[0], split.edge[0])) {
                case LT:
                    this.children = new Node[]{child, split};
                    break;
                case GT:
                    this.children = new Node[]{split, child};
                    break;
                case EQ:
                    throw new AssertionError();
            }

            return child;
        }

        public Node split(int splitAt) {
            Node split = new Node();
            split.edge = Arrays.copyOfRange(this.edge, splitAt, this.edge.length);

            this.edge = Arrays.copyOf(this.edge, splitAt);
            split.children = this.children;

            split.value = this.value;
            this.value = null;

            this.children = new Node[]{split};

            return this;
        }
    }
}
