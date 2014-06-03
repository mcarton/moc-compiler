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
    boolean overrides;

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

            public String toString() {
                StringBuilder sb = new StringBuilder("[");
                String result = "[";
                boolean first = true;
                for (Type type : this) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(type);
                    first = false;
                }
                sb.append(']');
                return sb.toString();
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

    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(isStatic ? "+" : "-");
        for (Selector sel : selectors) {
            sb.append(sel.getName());
            sb.append(':');
        }
        return sb.toString();
    }

    public String toString() {
        return getName();
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

    public boolean callableWith(Iterable<Type> types) {
        Iterator<Selector> selIt = selectors.iterator();
        Iterator<Type> typeIt = types.iterator();

        while (selIt.hasNext() && typeIt.hasNext()) {
            Selector sel = selIt.next();
            Type type = typeIt.next();

            if (!sel.hasParameter() && type == null) {
                continue;
            }

            if (
                !sel.hasParameter() || type == null
            ||  !sel.getParamType().constructsFrom(type)) {
                return false;
            }
        }

        return !selIt.hasNext() && !typeIt.hasNext();
    }

    public boolean overrides(Method other) {
        if (selectors.size() != other.selectors.size()) {
            return false;
        }

        Iterator<Selector> itsSel = other.getSelectors().iterator();

        for (Selector sel : selectors) {
            if (!sel.equals(itsSel.next())) {
                return false;
            }
        }

        return overrides = true;
    }

    public boolean overrides() {
        return overrides;
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

