package moc.gc.tam;

import moc.gc.*;

public class Expr implements moc.gc.Expr {
    Location loc;
    String code;
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

