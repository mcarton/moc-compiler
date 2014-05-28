package moc.type;

import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Method {
    ClassType classType;
    Type returnType;
    ArrayList<Selector> selectors = new ArrayList<>();
    boolean isStatic;

    public Method(
        ClassType classType, Type returnType,
        ArrayList<Selector> selectors, boolean isStatic
    ) {
        this.classType = classType;
        this.returnType = returnType;
        this.selectors = selectors;
        this.isStatic = isStatic;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Iterable<Type> getParameterTypes() {
        return new Iterable<Type>() {
            public Iterator<Type> iterator() {
                return new ParameterTypeIterator(
                    new ParameteredSelectorIterator(selectors.iterator())
                );
            }
        };
    }

    public Iterable<Selector> getParameteredSelectors() {
        return new Iterable<Selector>() {
            public Iterator<Selector> iterator() {
                return new ParameteredSelectorIterator(selectors.iterator());
            }
        };
    }

    public List<Selector> getSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String toString() {
        String result = isStatic ? "+" : "-";
        for (Selector sel : selectors) {
            result += sel.getName() + ':';
        }
        return result;
    }

    public boolean hasNames(ArrayList<String> names) {
        if (names.size() != selectors.size()) {
            return false;
        }

        Iterator<String> nameIt = names.iterator();
        Iterator<Selector> selIt = selectors.iterator();

        while (nameIt.hasNext() && selIt.hasNext()) {
            if (!selIt.next().getName().equals(nameIt.next())) {
                return false;
            }
        }

        return true;
    }
}

/** A convenience iterator that skips selectors without parameters. */
class ParameteredSelectorIterator implements Iterator<Selector> {
    Iterator<Selector> it;
    Selector next;

    ParameteredSelectorIterator(Iterator<Selector> it) { this.it = it; }

    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        else {
            while (it.hasNext() && !(next = it.next()).hasParameter());

            if (next != null && next.hasParameter()) {
                return true;
            }
            else {
                next = null;
                return false;
            }
        }
    }

    public Selector next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Selector result = next;
        next = null;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

class ParameterTypeIterator implements Iterator<Type> {
    ParameteredSelectorIterator it;

    ParameterTypeIterator(ParameteredSelectorIterator it) { this.it = it; }

    public boolean hasNext() { return it.hasNext(); }
    public Type next() { return it.next().getParamType(); }
    public void remove() { it.remove(); }
}

