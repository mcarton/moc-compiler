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

    /**
     * Return whether the verbosity is more than v.
     * Needed because comparison operator are not available in egg.
     * TODO:chicken: to be removed
     */
    boolean verbose(int v);

    // type size stuffs:
    moc.type.DTYPE getCharType();
    moc.type.DTYPE getIntType();
    moc.type.DTYPE getPtrType(moc.type.DTYPE what);
}
