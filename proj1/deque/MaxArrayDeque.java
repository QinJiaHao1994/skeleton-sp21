package deque;

import java.util.Comparator;

/**
 * @author Qin.JiaHao
 * @create 2021-04-14 2:46 下午
 */
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> myComparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        myComparator = c;
    }

    public T max() {
        return getMaxItem((Comparator<T>) myComparator);
    }

    public T max(Comparator<T> c) {
        return getMaxItem((Comparator<T>) c);
    }

    private T getMaxItem(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }

        T maxItem = get(0);
        for (T item: this) {
            int cmp = c.compare(maxItem, item);
            if (cmp < 0) {
                maxItem = item;
            }
        }

        return maxItem;
    }
}
