package moc.gc;

/** Interface for the code representing an expression.
 *
 *  Concrete classes are machine-dependant.
 */
public interface Expr {
    String getCode();
    Location getLoc();
}

