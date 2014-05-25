package moc.type;

public interface TypeVisitor<R> {
    public R visit(BooleanType what);
    public R visit(CharacterType what);
    public R visit(IntegerType what);

    public R visit(ClassType what);

    public R visit(VoidType what);

    public R visit(Array what);
    public R visit(IdType what);
    public R visit(NullType what);
    public R visit(Pointer what);
}

