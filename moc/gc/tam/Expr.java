package moc.gc.tam;

import moc.gc.*;

public class Expr implements moc.gc.Expr {
    String code;
    Location loc;
    boolean isAddress;

    public Expr(String code) {
        this.loc = null;
        this.code = code;
        this.isAddress = false;
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

