package moc.gc.llvm;

import moc.gc.IExpr;

/**
 * An llvm expression is a optional location (see below), a code for the
 * expression and the code to be added in the header of the generated file.
 *
 * If the location is <code>null</code>, the expression is a constant
 * (<code>null</code>, <code>42</code>, <code>YES</code>, etc.).
 */
final class Expr implements IExpr {
    Location loc;
    String code;
    boolean needsLoad = false;

    Expr(Location loc, String code) {
        this.loc = loc;
        this.code = code;
    }

    Expr(Location loc, String code, boolean needsLoad) {
        this(loc, code);
        this.needsLoad = needsLoad;
    }

    @Override
    public Location getLoc() {
        return loc;
    }

    @Override
    public String getCode() {
        return code;
    }

    public boolean needsLoad() {
        return needsLoad;
    }
}

