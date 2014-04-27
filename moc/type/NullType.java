package moc.type;

/** The type of the NULL constant. May be assigned to any pointer but unlike C,
 *  it is not just `(void*)0`.
 */
public class NullType extends AbstractType<NullType> {
    public NullType() {
        this.size = 0;
    }

    public String toString() {
        return "Null";
    }

    @Override
    public boolean constructsFrom(Type other) {
        return other instanceof NullType;
    }

    @Override
    public boolean comparableWith(Type other, String operator) {
        return other instanceof NullType || other instanceof Pointer;
    }

    @Override
    public boolean testable() {
        return true;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

