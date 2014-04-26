package moc.gc;

import moc.compiler.MOCException;
import moc.type.*;
import moc.symbols.*;
import java.util.ArrayList;

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
    Location getLocationFor(String name, Type type);

    // type stuffs:
    Type getNullType();
    Type getCharType();
    Type getIntType();
    Type getPtrType(moc.type.Type what);
    Type getArrayType(moc.type.Type what, int nbElements);
    Type getStringType(String string);

    // code generation stuffs:
    String genFunction(
        FunctionType f, ArrayList<Location> parameters, String name, String bloc
    );
    String genReturn(FunctionType f, Expr expr);
    String genVarDecl(Type t, Location location);
    String genVarDecl(Type t, Location location, Expr expr);

    Expr genInt(String txt);
    Expr genString(String txt);
    Expr genCharacter(String txt);
    Expr genNull();
    Expr genNew(Type type);
    String genDelete(Type type, Location loc);

    Expr genIdent(InfoVar info);
    Expr genAff(Type type, Location lhs, Expr rhs);
    Expr genNonAff(Type type, Expr expr);

    Expr genAdd(Expr lhs, Expr rhs);

    String genComment(String comment);
}
