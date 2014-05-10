package moc.gc.llvm;

import java.lang.StringBuilder;
import moc.type.*;

class CasterFromVisitor implements TypeVisitor<TypeVisitor<Caster>> {
    public TypeVisitor<Caster> visit(IntegerType from) {
        return new FromIntCaster(from);
    }
    public TypeVisitor<Caster> visit(CharacterType from) {
        return new FromCharCaster(from);
    }

    public TypeVisitor<Caster> visit(VoidType from) {
        return null;
    }

    public TypeVisitor<Caster> visit(NullType from) {
        return new FromNullCaster();
    }
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
    Type from;
    FromCharCaster(CharacterType from) { this.from = from; }

    public Caster visit(CharacterType to) {
        return IdentityCaster.instance;
    }
    public Caster visit(IntegerType to) {
        return new RegularCaster("sext", from, to);
    }
}

class FromIntCaster extends DefaultCaster {
    Type from;
    FromIntCaster(IntegerType from) { this.from = from; }

    public Caster visit(IntegerType to) {
        return IdentityCaster.instance;
    }
    public Caster visit(CharacterType to) {
        return new RegularCaster("trunc", from, to);
    }
}

class FromNullCaster extends DefaultCaster {
    public Caster visit(IntegerType to) {
        return new Caster() {
            public Expr cast(CodeGenerator cg, Expr expr) {
                return new Expr(null, "0");
            }
        };
    }

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
    public Caster visit(Pointer to) {
        return new RegularCaster("bitcast", new Pointer(from), to);
    }
}

class FromPointerCaster extends DefaultCaster {
    final Pointer from;
    FromPointerCaster(Pointer from) { this.from = from; }

    public Caster visit(IntegerType to) {
        return new RegularCaster("ptrtoint", from, to);
    }
    public Caster visit(Pointer to) {
        return new RegularCaster("bitcast", from, to);
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

class RegularCaster implements Caster {
    String castType;
    Type from, to;

    RegularCaster(String castType, Type from, Type to) {
        this.castType = castType;
        this.from = from;
        this.to = to;
    }

    public Expr cast(CodeGenerator cg, Expr expr) {
        String fromName = cg.typeName(from);
        String exprCode = cg.getValue(fromName, expr);
        String name = cg.cast(castType, fromName, exprCode, cg.typeName(to));
        return new Expr(new Location(name), cg.get());
    }
}

