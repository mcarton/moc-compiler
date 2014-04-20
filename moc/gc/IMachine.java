package moc.gc;

import moc.compiler.MOCException;

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
     * Writes the code in a file from tje source file name and the
     * suffix
     */
    void writeCode(String fileName, String code) throws MOCException;

    int verbosity();

    // type size stuffs:
    moc.type.DTYPE getCharType();
    moc.type.DTYPE getIntType();
    moc.type.DTYPE getPtrType(moc.type.DTYPE what);
    moc.type.DTYPE getArrayType(moc.type.DTYPE what, int nbElements);
    moc.type.DTYPE getStringType(String string);
}
