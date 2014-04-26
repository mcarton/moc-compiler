package moc.gc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import moc.compiler.MOCException;
import moc.type.*;

/**
 * This class describes a target machine.
 */
public abstract class AbstractMachine implements IMachine {
    ArrayList<String> warnings;
    boolean allWarnings;
    int verbosity;

    public AbstractMachine(int v, ArrayList<String> w) {
        verbosity = v;
        warnings = w;
        allWarnings = warnings.contains("all");
    }

    @Override
    public boolean hasWarning(String name) {
        return allWarnings || warnings.contains(name);
    }

    @Override
    public int verbosity() {
        return verbosity;
    }

    /**
     * Writes the code in a file from the name of the source file and the
     * suffix.
     */
    @Override
    public void writeCode(String fname, String code) throws MOCException {
        try {
            // pre-checked at startup
            int pt = fname.lastIndexOf('.');
            String name = fname.substring(0, pt);
            String asmName = name + "." + getSuffix();
            System.out.println("Writing code in " + asmName);
            PrintWriter pw = new PrintWriter(new FileOutputStream(asmName));
            pw.print("; Generated code for ");
            pw.print(fname);
            pw.print(".\n; Do not modify by hand\n\n");
            pw.print(code);
            pw.close();
        }
        catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }

    @Override
    public Type getNullType() {
        return new NullType();
    }

    @Override
    public Type getStringType(String string) {
        int nbChar = string.length()
                +1 /* includes '\0' */
                -2 /* excludes "" */;
        return getArrayType(getCharType(), nbChar);
    }
}

