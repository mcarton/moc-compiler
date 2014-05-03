package moc.type;

public interface TypeVisitor<R> {
    public R visit(IntegerType what);
    public R visit(CharacterType what);

    public R visit(VoidType what);
    public R visit(NullType what);

    public R visit(Pointer what);
    public R visit(Array what);
}

