package deque;

import java.util.Iterator;

/**
 * @author Qin.JiaHao
 * @create 2021-04-13 2:59 下午
 */
public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private int size;
    private T[] items;
    private int nextFirst = 4;
    private int nextLast = 5;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
    }

    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        int newNextFirst = (capacity - size) / 2 - 1;
        int newNextLast = newNextFirst + size + 1;

        int length = items.length;
        for (int i = 0, srcPos = nextFirst + 1, destPos = newNextFirst + 1;
             i < size; i += 1, srcPos += 1, destPos += 1) {
            newItems[destPos] = items[srcPos % length];
        }

        items = newItems;
        nextFirst = newNextFirst;
        nextLast = newNextLast;
    }

    private int getLength(int length) {
        if (size == length) {
            resize(length * 2);
            return length * 2;
        }

        if (size == length / 4 && length > 8) {
            resize(length / 2);
            return length / 2;
        }

        return length;
    }

    @Override
    public void addFirst(T item) {
        int length = getLength(items.length);
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + length) % length;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        int length = getLength(items.length);
        items[nextLast] = item;
        nextLast = (nextLast + 1) % length;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        System.out.println(getString());
        System.out.println();
    }

    private String getString() {
        StringBuilder sb = new StringBuilder();

        for (T o: this) {
            sb.append(o.toString());
            sb.append(" ");
        }

        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        int length = getLength(items.length);
        int newNextFirst = (nextFirst + 1) % length;
        T item = items[newNextFirst];
        nextFirst = newNextFirst;
        size -= 1;
        return item;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }

        int length = getLength(items.length);
        int newNextLast = (nextLast - 1 + length) % length;
        T item = items[newNextLast];
        nextLast = newNextLast;
        size -= 1;
        return item;
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        int idx = (index + nextFirst + 1) % items.length;
        return items[idx];
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<?> obj = (Deque<?>) o;

        if (obj.size() != size) {
            return false;
        }

        T o1;
        Object o2;
        for (int i = 0; i < size; i++) {
            o1 = get(i);
            o2 = obj.get(i);
            if (!o1.equals(o2)) {
                return false;
            }
        }

//        Iterator<T> e1 = iterator();
//        Iterator<?> e2 = obj.iterator();
//
//        while (e1.hasNext()) {
//            T o1 = e1.next();
//            Object o2 = e2.next();
//            if (!Objects.equals(o1, o2)) {
//                return false;
//            }
//        }

        return true;
    }

    private class DequeIterator implements Iterator<T> {
        private int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            int index = (nextFirst + pos + 1) % items.length;
            pos += 1;
            return items[index];
        }
    }
}
