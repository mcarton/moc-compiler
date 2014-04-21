package moc.type;

import java.util.ArrayList;
import java.util.Iterator;

public class LTYPES {
    ArrayList<DTYPE> list = new ArrayList<DTYPE>();

    public int size() {
        return list.size();
    }

    public Iterator<DTYPE> iterator() {
        return list.iterator();
    }

    public void add(DTYPE c) {
        list.add(c);
    }

    public DTYPE get(int i) {
        return list.get(i);
    }

    public String toString() {
        return list.toString();
    }

    public boolean constructsFrom(LTYPES other) {
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

