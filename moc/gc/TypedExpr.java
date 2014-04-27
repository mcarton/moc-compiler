package moc.gc;

import moc.type.Type;

public class TypedExpr {
    public Expr expr;
    public Type type;

    public TypedExpr(Expr expr, Type type) {
        this.expr = expr;
        this.type = type;
    }

    public Expr getExpr() {
        return expr;
    }

    public Type getType() {
        return type;
    }
}

