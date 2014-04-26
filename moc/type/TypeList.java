package moc.type;

import java.util.ArrayList;
import java.util.Iterator;

public class TypeList {
    ArrayList<Type> list = new ArrayList<Type>();

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

    public boolean constructsFrom(TypeList other) {
        if(size() != other.size()) {
            return false;
        }

        for(int i = 0; i < size(); ++i) {
            if(!get(i).constructsFrom(other.get(i))) {
                return false;
            }
        }

        return true;
    }
}

