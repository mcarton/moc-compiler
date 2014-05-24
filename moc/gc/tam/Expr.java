package moc.gc.tam;

import moc.gc.IExpr;

public class Expr implements IExpr {
    String code;
    boolean isAddress;

    public Expr(String code) {
        this(code, false);
    }

    public Expr(String code, boolean isAddress) {
        this.code = code;
        this.isAddress = isAddress;
    }

    public boolean isAddress() {
        return isAddress;
    }

    @Override
    public String getCode() {
        return code;
    }
}

