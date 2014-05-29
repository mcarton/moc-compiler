package moc.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ClassType extends AbstractType<ClassType> {
    ClassType superClass;
    String name;
    ArrayList<Attributes> attributes = new ArrayList<>();
    ArrayList<Method> methods = new ArrayList<>();

    public ClassType(String name, ClassType superClass) {
        this.name = name;
        this.superClass = superClass;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public ClassType getSuper() {
        return superClass;
    }

    public void addAttribute(Type type, String name) {
        attributes.add(new Attributes(type, name));
    }

    public void addMethod(Method method) {
        methods.add(method);
    }

    public Type getAttributeType(String name) {
        for (Attributes att : attributes) {
            if (att.name.equals(name)) {
                return att.type;
            }
        }

        if (superClass != null) {
            return superClass.getAttributeType(name);
        }

        return null;
    }

    public List<Attributes> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Get class own methods, without its parents'.
     */
    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public Method getClassMethod(ArrayList<String> names) {
        return getMethod(true, names);
    }

    public Method getInstanceMethod(ArrayList<String> names) {
        return getMethod(false, names);
    }

    public Method getMethod(boolean isStatic, ArrayList<String> names) {
        for (Method method : methods) {
            if (method.isStatic() == isStatic && method.hasNames(names)) {
                return method;
            }
        }

        if (superClass != null) {
            return superClass.getMethod(isStatic, names);
        }

        return null;
    }

    /**
     * A class inherits from itself, its parent, and its parent's parents
     * recursively.
     */
    public boolean inheritsFrom(Type other) {
        return equals(other) ||
            (superClass != null
                && (superClass.equals(other) || superClass.inheritsFrom(other)));
    }

    @Override
    public boolean constructsFrom(Type other) {
        return false;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return other == this;
    }

    @Override
    public boolean isClass() {
        return true;
    }
}

