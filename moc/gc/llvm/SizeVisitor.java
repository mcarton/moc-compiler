package moc.gc.llvm;

import moc.type.*;

/** A visitor to get the size of types.
 */
public class SizeVisitor implements TypeVisitor<Integer> {
    public Integer visit(IntegerType what)   { return 8; }
    public Integer visit(CharacterType what) { return 1; }

    public Integer visit(VoidType what)      { return 0; }
    public Integer visit(NullType what)      { return 8; }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }
    public Integer visit(Pointer what)       { return 8; }
}

