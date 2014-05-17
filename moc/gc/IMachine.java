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

    /**
     * Returns the verbosity of the machine.
     */
    int verbosity();

    // location stuffs:
    /** Indicates the machine we enter a function definition.
     */
    void beginFunction(FunctionType fun);

    /** Indicates the machine we exit a function definition.
     */
    void endFunction();

    /** Indicates the machine we enter a new block.
     */
    void beginBlock();

    /** Indicates the machine we exit a block.
     */
    void endBlock();

    /** Get the machine-dependant Location for a parameters named
     *  <code>name</code> with type <code>type</code>.
     */
    Location getLocationForParameter(Type type, String name);

    /** Get the machine-dependant Location for a variable named
     *  <code>name</code> with type <code>type</code>.
     */
    Location getLocationForVariable(Type type, String name);

    // string stuffs:
    int stringSize(String unescaped);

    // code generation stuffs:
    String genFunction(
        FunctionType f, ArrayList<Location> parameters, String name, String bloc
    );
    String genReturn(FunctionType f, Expr expr);

    String genBlock(String code);
    String genInst(Expr expr);
    String genBlockInst(String code);
    String genAsm(String code);
    String genUsing(String name, Type type);
    String genGlobalAsm(String code);

    String genIfInst(String code);
    String genIf(Expr cond, String thenCode, String elseCode);
    String genElseIf(String code);
    String genElse();
    String genElse(String code);

    /** Generates code for variable declaration without definition.
     */
    String genVarDecl(Type t, Location location);

    /** Generates code for variable declaration with definition.
     */
    String genVarDecl(Type t, Location location, Expr expr);

    Expr genInt(String txt);
    Expr genString(int length, String txt);
    Expr genCharacter(String txt);
    Expr genNull();

    Expr genNew(Type type);
    String genDelete(Type type, Expr expr);
    Expr genCall(String funName, FunctionType fun, ArrayList<Expr> exprs);
    Expr genSizeOf(Type type);

    Expr genIdent(InfoVar info);
    Expr genAff(Type type, Expr lhs, Expr rhs);
    Expr genNonAff(Type type, Expr expr);

    /**
     * Dereference a pointer.
     *
     * @param type The type of the pointer.
     */
    Expr genDeref(Type type, Expr expr);

    /**
     * Generates code for <code>lhs[rhs]</code>.
     */
    Expr genArrSub(Array type, Expr lhs, Expr rhs);
    Expr genPtrSub(Pointer type, Expr lhs, Expr rhs);
    Expr genParen(Expr expr);
    Expr genCast(Type from, Type to, Expr expr);

    Expr genIntUnaryOp(String op, Expr expr);

    Expr genIntBinaryOp(String op, Expr lhs, Expr rhs);
    Expr genCharBinaryOp(String op, Expr lhs, Expr rhs);
    Expr genPtrBinaryOp(String op, Type pointer, Expr lhs, Expr rhs);

    String genComment(String comment);
}

