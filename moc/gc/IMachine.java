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

    /** Indicates the machine we enter a method definition.
     */
    void beginMethod(Method meth);

    /** Indicates the machine we exit a method definition.
     */
    void endMethod();

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

    /** Get the machine-dependant ILocation for an attribute named
     *  <code>name</code> with type <code>type</code> in the current class.
     */
    ILocation getLocationForAttribute(ClassType clazz, Type type, String name);

    // string stuffs:
    int stringSize(String unescaped);

    // code generation stuffs:
    String genFunction(
        FunctionType f, ArrayList<ILocation> parameters, String name, String block
    );
    String genMethod(Method method, ArrayList<ILocation> parameters, String block);
    String genReturn(Type returnType, IExpr expr);

    /** Code for a block of code.
     */
    String genBlock(String code);

    /** Code for an expression.
     */
    String genInst(Type type, IExpr expr);

    /** Code for a instruction that is a block.
     */
    String genBlockInst(String code);

    /** Code for inline assembly (quotes are included in `code`).
     */
    String genAsm(String code);

    /** Code for a `using` instruction.
     */
    String genUsing(String name, Type type);

    /** Code for inline assembly that is outside a function.
     */
    String genGlobalAsm(String code);

    /** Code for an `if/else if/else` instruction.
     */
    String genIfInst(String code);

    /** Code for an `if/else` instruction.
     *
     * @param elseCode null if there is no `else` block.
     */
    String genIf(IExpr cond, String thenCode, String elseCode);

    /** Code for an `else if` in an `if` instruction.
     */
    String genElseIf(String code);

    /** Code for a missing `else` in an `if` instruction.
     */
    String genElse();

    /** Code for an `else` in an `if` instruction.
     */
    String genElse(String code);

    String genWhile(IExpr cond, String block);

    /** Generate code for variable declaration without definition.
     */
    String genVarDecl(Type t, ILocation location);

    /** Generate code for variable declaration with definition.
     */
    String genVarDecl(Type t, ILocation location, IExpr expr);

    IExpr genInt(String txt);
    IExpr genString(int length, String txt);
    IExpr genCharacter(String txt);
    IExpr genNull();
    IExpr genYes();
    IExpr genNo();

    /** Generate code for the `self` variable in a class of type `type`. */
    IExpr genSelf(Type type);

    /** Generate code for the `super` variable in a class of type `type`. */
    IExpr genSuper(Type type);

    /** Allocate space for a variable of type `type`.
     */
    IExpr genNew(Type type);

    /** Allocate space for a dynamic array.
     */
    IExpr genNewArray(IExpr size, Type type);

    /** Free space allocated by a call to either for of the `new` operator.
     */
    String genDelete(Type type, IExpr expr);

    /** Call a function.
     */
    IExpr genCall(String funName, FunctionType fun, ArrayList<IExpr> params);

    /** Call a method on a instance.
     */
    IExpr genCall(
        Method method, Pointer type, IExpr instance, ArrayList<IExpr> params
    );

    /** Call a static method. */
    IExpr genCall(Method method, ArrayList<IExpr> params);

    /** Return the size of the given type.
     */
    IExpr genSizeOf(Type type);

    IExpr genIdent(InfoVar info);

    /** Code for an affectation.
     */
    IExpr genAff(Type type, IExpr lhs, IExpr rhs);

    /** Code for an expression that is not an affectation.
     */
    IExpr genNonAff(Type type, IExpr expr);

    /**
     * Dereference a pointer.
     *
     * @param type The type of the pointer to dereference.
     */
    IExpr genDeref(Type type, IExpr expr);

    /**
     * Generates code for <code>lhs[rhs]</code> where `lhs` is an array.
     */
    IExpr genArrSub(Array type, IExpr lhs, IExpr rhs);

    /**
     * Generates code for <code>lhs[rhs]</code> where `lhs` is a pointer.
     */
    IExpr genPtrSub(Pointer type, IExpr lhs, IExpr rhs);

    /** Code for a parenthesised expression.
     */
    IExpr genParen(IExpr expr);

    /**
     * Code for a implicit or explicit cast. The function is called only if the
     * cast is possible.
     */
    IExpr genCast(Type from, Type to, IExpr expr);

    IExpr genIntUnaryOp(String op, IExpr expr);

    IExpr genIntBinaryOp(String op, IExpr lhs, IExpr rhs);
    IExpr genCharBinaryOp(String op, IExpr lhs, IExpr rhs);
    IExpr genPtrBinaryOp(String op, Type pointer, IExpr lhs, IExpr rhs);

    String genComment(String comment);

    String genClass(ClassType clazz, String methods);
}

