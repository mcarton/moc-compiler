package moc.gc.llvm;

import moc.type.*;

/** A visitor to get the size of types.
 */
final public class SizeVisitor implements TypeVisitor<Integer> {
    public Integer visit(CharacterType what) { return 1; }
    public Integer visit(BooleanType what)   { return 1; }
    public Integer visit(IntegerType what)   { return 8; }

    public Integer visit(ClassType what) {
        return -1; // TODO:class
    }

    public Integer visit(VoidType what)      { return 0; }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }
    public Integer visit(IdType what)        { return 8; }
    public Integer visit(NullType what)      { return 8; }
    public Integer visit(Pointer what)       { return 8; }
}

