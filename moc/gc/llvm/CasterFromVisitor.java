package moc.gc.llvm;

import java.lang.StringBuilder;
import moc.type.*;

public class CasterFromVisitor implements TypeVisitor<TypeVisitor<Caster>> {
    public TypeVisitor<Caster> visit(IntegerType from)   { return new FromIntCaster(); }
    public TypeVisitor<Caster> visit(CharacterType from) { return new FromCharCaster(); }

    public TypeVisitor<Caster> visit(VoidType from)      { return null; }

    public TypeVisitor<Caster> visit(NullType from)      { return new FromNullCaster(); }
    public TypeVisitor<Caster> visit(Array from) {
        return new FromArrayCaster(from);
    }
    public TypeVisitor<Caster> visit(Pointer from) {
        return new FromPointerCaster(from);
    }
}

class DefaultCaster implements TypeVisitor<Caster> {
    public Caster visit(IntegerType to)   { return null; }
    public Caster visit(CharacterType to) { return null; }
    public Caster visit(VoidType to)      { return null; }
    public Caster visit(NullType to)      { return null; }
    public Caster visit(Array to)         { return null; }
    public Caster visit(Pointer to)       { return null; }
}

class FromCharCaster extends DefaultCaster {
    public Caster visit(CharacterType to) {
        return IdentityCaster.instance;
    }
    public Caster visit(IntegerType to) {
        return new Caster() {
            public Expr cast(CodeGenerator cg, Expr expr) {
                String exprCode = cg.getValue("i8", expr);
                String name = cg.cast("sext", "i8", exprCode, "i64");
                return new Expr(new Location(name), cg.get());
            }
        };
    }
}

class FromIntCaster extends DefaultCaster {
    public Caster visit(IntegerType to) {
        return IdentityCaster.instance;
    }
    public Caster visit(CharacterType to) {
        return new Caster() {
            public Expr cast(CodeGenerator cg, Expr expr) {
                String exprCode = cg.getValue("i64", expr);
                String name = cg.cast("trunc", "i64", exprCode, "i8");
                return new Expr(new Location(name), cg.get());
            }
        };
    }
}

class FromNullCaster extends DefaultCaster {
    public Caster visit(NullType to) {
        return IdentityCaster.instance;
    }
    public Caster visit(Pointer to) {
        return IdentityCaster.instance;
    }
}

class FromArrayCaster extends DefaultCaster {
    final Array from;
    FromArrayCaster(Array from) { this.from = from; }

    public Caster visit(Array to) {
        return IdentityCaster.instance;
    }
    public Caster visit(final Pointer to) {
        /* TODO:cast */
        return IdentityCaster.instance;
    }
}

class FromPointerCaster extends DefaultCaster {
    final Pointer from;
    FromPointerCaster(Pointer from) { this.from = from; }

    public Caster visit(final Pointer to) {
        return new Caster() {
            public Expr cast(CodeGenerator cg, Expr expr) {
                String fromName = cg.typeName(from);
                String exprCode = cg.getValue(fromName, expr);
                String name = cg.cast("bitcast", fromName, exprCode, cg.typeName(to));
                return new Expr(new Location(name), cg.get());
            }
        };
    }
}

interface Caster {
    public Expr cast(CodeGenerator cg, Expr expr);
}

enum IdentityCaster implements Caster {
    instance;

    public Expr cast(CodeGenerator cg, Expr expr) {
        return expr;
    }
}

