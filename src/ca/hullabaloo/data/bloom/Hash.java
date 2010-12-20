package ca.hullabaloo.data.bloom;

public interface Hash<E> {
    public int longSize();
    public void hash(E object, long[] result);
}
