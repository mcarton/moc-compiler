package moc.gc.llvm;

import moc.type.*;

/** A visitor to get the size of types.
 */
final public class SizeVisitor extends moc.type.SizeVisitor {
    protected int baseClassSize() { return 8; /* vtable */ }
    public Integer visit(CharacterType what) { return 1; }
    public Integer visit(BooleanType what)   { return 1; }
    public Integer visit(IntegerType what)   { return 8; }
    public Integer visit(IdType what)        { return 8; }
    public Integer visit(NullType what)      { return 8; }
    public Integer visit(Pointer what)       { return 8; }
}

