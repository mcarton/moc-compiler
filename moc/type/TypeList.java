package moc.type;

import java.util.ArrayList;
import java.util.Iterator;

public final class TypeList {
    ArrayList<Type> list = new ArrayList<Type>();

    /** Return the number of elements in the list, not the sum of the size of
     *  the types.
     */
    public int size() {
        return list.size();
    }

    public Iterator<Type> iterator() {
        return list.iterator();
    }

    public void add(Type c) {
        list.add(c);
    }

    public Type get(int i) {
        return list.get(i);
    }

    public String toString() {
        return list.toString();
    }

    /** A TypeList is constructible from another iff they have the same size and
     *  are constructible it is constructible from the other element-wise.
     */
    public boolean constructsFrom(TypeList other) {
        if (size() != other.size()) {
            return false;
        }

        for (int i = 0; i < size(); ++i) {
            if (!get(i).constructsFrom(other.get(i))) {
                return false;
            }
        }

        return true;
    }
}

