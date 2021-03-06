package moc.gc.llvm;

import java.lang.StringBuilder;
import moc.type.*;

class CasterFromVisitor implements TypeVisitor<TypeVisitor<Caster>> {
    public TypeVisitor<Caster> visit(BooleanType from) {
        return new FromBoolCaster(from);
    }
    public TypeVisitor<Caster> visit(CharacterType from) {
        return new FromCharCaster(from);
    }
    public TypeVisitor<Caster> visit(IntegerType from) {
        return new FromIntCaster(from);
    }

    public TypeVisitor<Caster> visit(ClassType from) {
        return null;
    }

    public TypeVisitor<Caster> visit(VoidType from) {
        return null;
    }

    public TypeVisitor<Caster> visit(Array from) {
        return new FromArrayCaster(from);
    }
    public TypeVisitor<Caster> visit(IdType from) {
        return new FromIdCaster(from);
    }
    public TypeVisitor<Caster> visit(NullType from) {
        return new FromNullCaster();
    }
    public TypeVisitor<Caster> visit(Pointer from) {
        return new FromPointerCaster(from);
    }
}

class DefaultCaster implements TypeVisitor<Caster> {
    public Caster visit(BooleanType to)   { return null; }
    public Caster visit(CharacterType to) { return null; }
    public Caster visit(IntegerType to)   { return null; }

    public Caster visit(ClassType to)     { return null; }

    public Caster visit(VoidType to)      { return null; }

    public Caster visit(Array to)         { return null; }
    public Caster visit(IdType to)        { return null; }
    public Caster visit(NullType to)      { return null; }
    public Caster visit(Pointer to)       { return null; }
}

class FromBoolCaster extends DefaultCaster {
    Type from;
    FromBoolCaster(BooleanType from) { this.from = from; }

    public Caster visit(BooleanType to) {
        return IdentityCaster.instance;
    }
    public Caster visit(IntegerType to) {
        return new RegularCaster("sext", from, to);
    }
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

    public Caster visit(BooleanType to) {
        return new ToBoolCaster(from, to, "0");
    }
    public Caster visit(CharacterType to) {
        return new RegularCaster("trunc", from, to);
    }
    public Caster visit(IntegerType to) {
        return IdentityCaster.instance;
    }
}

class FromNullCaster extends DefaultCaster {
    public Caster visit(BooleanType to) {
        return new Caster() {
            public Expr cast(CodeGenerator cg, Expr expr) {
                return new Expr(null, "0");
            }
        };
    }
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
    public Caster visit(IdType to) {
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

    public Caster visit(BooleanType to) {
        return new ToBoolCaster(from, to, "null");
    }
    public Caster visit(IntegerType to) {
        return new ToBoolCaster(from, to, "null");
    }
    public Caster visit(Pointer to) {
        if (from.equals(to)) {
            return IdentityCaster.instance;
        }
        else {
            return new RegularCaster("bitcast", from, to);
        }
    }
    public Caster visit(IdType to) {
        return new RegularCaster("bitcast", from, to);
    }
}

class FromIdCaster extends DefaultCaster {
    final IdType from;
    FromIdCaster(IdType from) { this.from = from; }

    public Caster visit(Pointer to) {
        if (from.equals(to)) {
            return IdentityCaster.instance;
        }
        else {
            return new RegularCaster("bitcast", from, to);
        }
    }

    public Caster visit(IdType to) {
        return IdentityCaster.instance;
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

class ToBoolCaster implements Caster {
    Type from, to;
    String cmpTo;

    public ToBoolCaster(Type from, Type to, String cmpTo) {
        this.from = from;
        this.to = to;
        this.cmpTo = cmpTo;
    }

    public Expr cast(CodeGenerator cg, Expr expr) {
        String fromName = cg.typeName(from);
        String exprCode = cg.getValue(fromName, expr);
        String tmp = cg.binaryOperator("icmp ne", fromName, exprCode, cmpTo);
        String tmp2 = cg.cast("zext", "i1", tmp, cg.typeName(to));
        return new Expr(new Location(tmp2), cg.get());
    }
}

