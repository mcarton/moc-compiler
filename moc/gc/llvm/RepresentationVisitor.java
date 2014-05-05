package moc.gc.llvm;

import moc.type.*;

/** A visitor to get the llvm representation of types.
 */
class RepresentationVisitor implements TypeVisitor<String> {
    public String visit(IntegerType what)   { return "i64"; }
    public String visit(CharacterType what) { return "i8"; }

    public String visit(VoidType what)      { return "void"; }
    public String visit(NullType what)      { return "i8*"; }

    public String visit(Array what) {
        return "["
            + what.getNbElements()
            + " x "
            + what.getPointee().visit(this)
            + "]";
    }
    public String visit(Pointer what) {
        return what.getPointee().visit(this) + "*";
    }
}

