package deque;

/**
 * @author Qin.JiaHao
 * @create 2021-04-14 2:59 下午
 */
public interface Deque<T> {
    void addFirst(T item);

    void addLast(T item);

    default boolean isEmpty() {
        return size() == 0;
    };

    int size();

    void printDeque();

    T removeFirst();

    T removeLast();

    T get(int index);
}
