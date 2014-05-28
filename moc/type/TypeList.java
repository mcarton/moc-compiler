package moc.type;

import java.util.ArrayList;
import java.util.ListIterator;

public final class TypeList implements Iterable<Type> {
    ArrayList<Type> list = new ArrayList<Type>();

    /** Return the number of elements in the list, not the sum of the size of
     *  the types.
     */
    public int size() {
        return list.size();
    }

    public ListIterator<Type> iterator() {
        return list.listIterator();
    }

    public ListIterator<Type> iterator(int index) {
        return list.listIterator(index);
    }

    public void add(Type c) {
        list.add(c);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Type get(int i) {
        return list.get(i);
    }

    public String toString() {
        return list.toString();
    }
}

