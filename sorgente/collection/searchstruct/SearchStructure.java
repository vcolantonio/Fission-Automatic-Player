package collection.searchstruct;

import java.util.Collection;

public interface SearchStructure<T> extends Iterable<T> {

    void add(T element);

    T next();

    int size();

    boolean isEmpty();

    Collection<T> toCollection();

}
