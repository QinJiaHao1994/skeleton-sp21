package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author Qin.JiaHao
 * @create 2021-04-13 2:59 下午
 */
public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel =  new Node(null);
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        new Node(item, sentinel, sentinel.next);
        size += 1;
    }

    @Override
    public void addLast(T item) {
        new Node(item, sentinel.prev, sentinel);
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

        T first = sentinel.next.remove();
        size -= 1;
        return first;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }

        T last = sentinel.prev.remove();
        size -= 1;
        return last;
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        Node curr = sentinel;

        while (index >= 0) {
            curr = curr.next;
            index -= 1;
        }

        return curr.item;
    }

    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node curr, int index) {
        if (index == 0) {
            return curr.item;
        }

        return getRecursiveHelper(curr.next, index - 1);
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

        Iterator<T> e1 = iterator();
        Iterator<?> e2 = obj.iterator();

        while (e1.hasNext()) {
            T o1 = e1.next();
            Object o2 = e2.next();
            if (!Objects.equals(o1, o2)) {
                return false;
            }
        }

        return true;
    }

    private class DequeIterator implements Iterator<T> {
        private Node curr = sentinel;

        @Override
        public boolean hasNext() {
            return curr.next != sentinel;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            curr = curr.next;
            return curr.item;
        }
    }

    private class Node {
        private Node prev;
        private Node next;
        private T item;

        Node(T value) {
            item = value;
            prev = this;
            next = this;
        }

        Node(T value, Node p, Node n) {
            item = value;
            prev = p;
            next = n;
            prev.next = this;
            next.prev = this;
        }

        private T remove() {
            prev.next = next;
            next.prev = prev;
            return item;
        }
    }

}
