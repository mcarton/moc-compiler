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

    /** Get the machine-dependant ILocation for a parameters named
     *  <code>name</code> with type <code>type</code>.
     */
    ILocation getLocationForParameter(Type type, String name);

    /** Get the machine-dependant ILocation for a variable named
     *  <code>name</code> with type <code>type</code>.
     */
    ILocation getLocationForVariable(Type type, String name);

    // string stuffs:
    int stringSize(String unescaped);

    // code generation stuffs:
    String genFunction(
        FunctionType f, ArrayList<ILocation> parameters, String name, String bloc
    );
    String genReturn(FunctionType f, IExpr expr);

    String genBlock(String code);
    String genInst(IExpr expr);
    String genBlockInst(String code);
    String genAsm(String code);
    String genUsing(String name, Type type);
    String genGlobalAsm(String code);

    String genIfInst(String code);
    String genIf(IExpr cond, String thenCode, String elseCode);
    String genElseIf(String code);
    String genElse();
    String genElse(String code);

    /** Generates code for variable declaration without definition.
     */
    String genVarDecl(Type t, ILocation location);

    /** Generates code for variable declaration with definition.
     */
    String genVarDecl(Type t, ILocation location, IExpr expr);

    IExpr genInt(String txt);
    IExpr genString(int length, String txt);
    IExpr genCharacter(String txt);
    IExpr genNull();

    IExpr genNew(Type type);
    IExpr genNew(IExpr size, Type type);
    String genDelete(Type type, IExpr expr);
    IExpr genCall(String funName, FunctionType fun, ArrayList<IExpr> exprs);
    IExpr genSizeOf(Type type);

    IExpr genIdent(InfoVar info);
    IExpr genAff(Type type, IExpr lhs, IExpr rhs);
    IExpr genNonAff(Type type, IExpr expr);

    /**
     * Dereference a pointer.
     *
     * @param type The type of the pointer.
     */
    IExpr genDeref(Type type, IExpr expr);

    /**
     * Generates code for <code>lhs[rhs]</code>.
     */
    IExpr genArrSub(Array type, IExpr lhs, IExpr rhs);
    IExpr genPtrSub(Pointer type, IExpr lhs, IExpr rhs);
    IExpr genParen(IExpr expr);
    IExpr genCast(Type from, Type to, IExpr expr);

    IExpr genIntUnaryOp(String op, IExpr expr);

    IExpr genIntBinaryOp(String op, IExpr lhs, IExpr rhs);
    IExpr genCharBinaryOp(String op, IExpr lhs, IExpr rhs);
    IExpr genPtrBinaryOp(String op, Type pointer, IExpr lhs, IExpr rhs);

    String genComment(String comment);
}

