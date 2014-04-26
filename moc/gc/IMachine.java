package moc.gc;

import moc.compiler.MOCException;
import moc.type.*;
import moc.tds.*;

/**
 * This interface describes a target machine
 */
public interface IMachine {
    /**
     * Target file suffix (.tam for example).
     */
    String getSuffix();

    /**
     * Return whether the warning name was given.
     */
    boolean hasWarning(String name);

    /**
     * Writes the code in a file from the source file name and the
     * suffix
     */
    void writeCode(String fileName, String code) throws MOCException;

    int verbosity();

    // location stuffs:
    void newFunction();
    void newBloc();
    Location getLocationFor(String name, DTYPE type);

    // type stuffs:
    DTYPE getCharType();
    DTYPE getIntType();
    DTYPE getPtrType(moc.type.DTYPE what);
    DTYPE getArrayType(moc.type.DTYPE what, int nbElements);
    DTYPE getStringType(String string);

    // code generation stuffs:
    String genFunction(DFUNCTIONTYPE f, String name, String bloc);
    String genReturn(DFUNCTIONTYPE f, Expr expr);
    String genVarDecl(DTYPE t, Location location);
    String genVarDecl(DTYPE t, Location location, Expr expr);

    Expr genInt(String txt);
    Expr genString(String txt);
    Expr genCharacter(String txt);
    Expr genNull();
    Expr genNew(DTYPE type);
    String genDelete(DTYPE type, Location loc);

    Expr genIdent(INFOVAR info);
    Expr genAff(DTYPE type, Location lhs, Expr rhs);
    Expr genNonAff(DTYPE type, Expr expr);

    Expr genAdd(Expr lhs, Expr rhs);

    String genComment(String comment);
}
