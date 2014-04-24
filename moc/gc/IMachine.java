package moc.gc;

import moc.compiler.MOCException;
import moc.type.*;

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

    // type stuffs:
    DTYPE getCharType();
    DTYPE getIntType();
    DTYPE getPtrType(moc.type.DTYPE what);
    DTYPE getArrayType(moc.type.DTYPE what, int nbElements);
    DTYPE getStringType(String string);

    // code generation stuffs:
    String genFunction(DFUNCTIONTYPE f, String name, String bloc);
    String genVarDecl(DTYPE t, String name, String val);
    String genCst(String txt);
    String genString(String txt);
    String genCharacter(String txt);
    String genNull();
    String genComment(String comment);// TODO:code: incomplete
}
