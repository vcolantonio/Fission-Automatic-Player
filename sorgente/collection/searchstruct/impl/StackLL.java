package collection.searchstruct.impl;

import collection.searchstruct.SearchStructure;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class StackLL<T> implements SearchStructure<T> {

    LinkedList<T> ll = new LinkedList<>();

    public StackLL(Collection<T> moves) {
        this.ll = new LinkedList<>(moves);
    }

    @Override
    public void add(T element) {
        ll.add(element);
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

}
