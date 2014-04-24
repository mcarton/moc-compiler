package moc.gc.llvm;

public class Expr implements moc.gc.Expr {
    String code;
    Location loc;

    /**
     * Constructs an llvm.Expr, if name is `null`, the expression is a constant
     * (`null`, `42`, `YES`, etc.).
     */
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

