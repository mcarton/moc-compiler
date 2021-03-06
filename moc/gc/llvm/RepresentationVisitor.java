package moc.gc.llvm;

import moc.type.*;

/** A visitor to get the llvm representation of types.
 */
final class RepresentationVisitor implements TypeVisitor<String> {
    public String visit(BooleanType what)   { return "i8"; }
    public String visit(CharacterType what) { return "i8"; }
    public String visit(IntegerType what)   { return "i64"; }

    public String visit(ClassType what) {
        return "%class." + what.getName();
    }

    public String visit(VoidType what)      { return "void"; }

    public String visit(Array what) {
        return "["
            + what.getNbElements()
            + " x "
            + what.getPointee().visit(this)
            + "]";
    }
    public String visit(IdType what)        { return "%mocc.id"; }
    public String visit(NullType what)      { return "i8*"; }
    public String visit(Pointer what) {
        return what.getPointee().visit(this) + "*";
    }
}

