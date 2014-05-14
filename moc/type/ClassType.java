package moc.type;

import java.util.Vector;

public final class ClassType extends AbstractType<ClassType> {
    ClassType superClass;
    String name;
    Vector<Attributes> attributes = new Vector<>();

    public ClassType(String name, ClassType superClass) {
        this.name = name;
        this.superClass = superClass;
    }

    public String toString() {
        return name;
    }

    public ClassType getSuper() {
        return superClass;
    }

    public void addAttribute(Type type, String name) {
        attributes.add(new Attributes(type, name));
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

    public boolean inheritsFrom(Type other) {
        return superClass != null
           && (superClass.equals(other) || superClass.inheritsFrom(other));
    }

    @Override
    public boolean constructsFrom(Type autre) {
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

