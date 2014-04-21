package moc.type;

public interface TypeVisitor<R> {
    public R visit(DTYPE what);

    public R visit(INTEGER_t what);
    public R visit(CHARACTER_t what);

    public R visit(VOID_t what);
    public R visit(NULL_t what);

    public R visit(POINTER what);
    public R visit(ARRAY what);
}

