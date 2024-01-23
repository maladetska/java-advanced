package info.kgeorgiy.ja.okorochkova.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> data;
    private Comparator<? super E> comparator = null;

    public ArraySet() {
        data = new ArrayList<>();
    }

    public ArraySet(Collection<? extends E> data, Comparator<? super E> comparator) {
        if (data.isEmpty()) {
            this.data = new ArrayList<>();
        } else {
            SortedSet<E> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(Objects.requireNonNull(data));
            this.data = new ArrayList<>(treeSet);
        }
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> data) {
        this.data = data.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(new TreeSet<>(Objects.requireNonNull(data)));
    }

    public ArraySet(Comparator<? super E> comparator) {
        data = new ArrayList<>();
        this.comparator = comparator;
    }

    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        this.data = data;
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.size() == 0;
    }

    private E getElement(int i) {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot bring an element: ArraySet is empty");
        }
        return data.get(i);
    }

    @Override
    public E first() {
        return getElement(0);
    }

    @Override
    public E last() {
        return getElement(size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return !(Collections.binarySearch(data, (E) Objects.requireNonNull(o), comparator) < 0);
    }

    public int findIndex(E item) {
        int pos = Collections.binarySearch(data, Objects.requireNonNull(item), comparator);
        return pos >= 0 ? pos : -pos - 1;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    private SortedSet<E> subSetPattern(final int leftIndex, final int rightIndex) {
        if (isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(leftIndex, rightIndex), comparator);
    }

    @Override
    public SortedSet<E> subSet(final E item1, final E item2) {
        int leftIndex = findIndex(item1);
        int rightIndex = findIndex(item2);
        if (comparator.compare(item1, item2) <= 0) {
            return subSetPattern(leftIndex, rightIndex);
        }
        throw new IllegalArgumentException("Wrong elements");
    }

    @Override
    public SortedSet<E> headSet(final E item) {
        return subSetPattern(0, findIndex(item));
    }

    @Override
    public SortedSet<E> tailSet(final E item) {
        return subSetPattern(findIndex(item), size());
    }
}
