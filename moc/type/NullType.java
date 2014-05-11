package moc.type;

/** The type of the NULL constant. May be assigned to any pointer but unlike C,
 *  it is not just <code>(void*)0</code>.
 */
public final class NullType extends AbstractType<NullType> {
    public NullType() {
    }

    public String toString() {
        return "Null";
    }

    @Override
    public boolean testable() {
        return true;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return true;
    }

    @Override
    public boolean isNull() {
        return true;
    }
}

