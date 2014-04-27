package moc.gc;

import java.util.ArrayList;
import moc.compiler.MOCException;
import moc.symbols.*;
import moc.type.*;

/**
 * This interface describes a target machine
 */
public interface IMachine {
    /**
     * Target file suffix (".tam" for example).
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
    String genDelete(Type type, Expr expr);
    Expr genCall(String funName, FunctionType fun, ArrayList<Expr> exprs);
    Expr genSizeOf(Type type);

    Expr genIdent(InfoVar info);
    Expr genAff(Type type, Location lhs, Expr rhs);
    Expr genNonAff(Type type, Expr expr);

    TypedExpr genBinaryOp(String what, Type lhsType, Expr lhs, Type rhsType, Expr rhs);
    Expr genAddInt(Expr lhs, Expr rhs);
    Expr genSubInt(Expr lhs, Expr rhs);
    Expr genOrInt(Expr lhs, Expr rhs);
    Expr genMultInt(Expr lhs, Expr rhs);
    Expr genDivInt(Expr lhs, Expr rhs);
    Expr genModInt(Expr lhs, Expr rhs);
    Expr genAndInt(Expr lhs, Expr rhs);

    String genComment(String comment);
}

