package moc.gc.llvm;

/**
 * An llvm expression is a optional location (see below), a code for the
 * expression and the code to be added in the header of the generated file.
 *
 * If the location is <code>null</code>, the expression is a constant
 * (<code>null</code>, <code>42</code>, <code>YES</code>, etc.).
 */
public class Expr implements moc.gc.Expr {
    Location loc;
    String code;

    public Expr(Location loc, String code) {
        this.loc = loc;
        this.code = code;
    }

    @Override
    public Location getLoc() {
        return loc;
    }

    @Override
    public String getCode() {
        return code;
    }
}

