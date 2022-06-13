package collection.searchstruct.impl;

import collection.searchstruct.SearchStructure;

import java.util.*;
import java.util.function.Consumer;

public class OrderedStack<T extends Comparable<T>> implements SearchStructure<T> {

    LinkedList<T> ll = new LinkedList<>();
    boolean reverse = false;

    public OrderedStack(Collection<T> moves, boolean reverse) {
        this.ll = new LinkedList<>(moves);
        this.reverse = reverse;

        if(reverse)
            Collections.sort(ll, (el1, el2) -> el1.compareTo(el2));

        else {
            Collections.sort(ll, (el1, el2) -> el2.compareTo(el1));
        }
    }

    @Override
    public void add(T element) {
        ll.add(element);
        if(reverse)
            Collections.sort(ll, (el1, el2) -> el1.compareTo(el2));
        else
            Collections.sort(ll, (el1, el2) -> el2.compareTo(el1));
    }

    @Override
    public T next() {
        return ll.pop();
    }

    @Override
    public int size() {
        return ll.size();
    }

    @Override
    public boolean isEmpty() {
        return ll.size() == 0;
    }

    @Override
    public Collection<T> toCollection() {
        return Collections.unmodifiableCollection(ll);
    }

    @Override
    public String toString() {
        return "StackLL{" +
                "ll=" + ll +
                '}';
    }

    @Override
    public Iterator<T> iterator() {
        return ll.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        SearchStructure.super.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return SearchStructure.super.spliterator();
    }
}
