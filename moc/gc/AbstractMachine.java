package moc.gc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import moc.compiler.MOCException;

/**
 * This class describes a target machine.
 */
public abstract class AbstractMachine implements IMachine {
    ArrayList<String> warnings;
    int verbosity;

    AbstractMachine(int v, ArrayList<String> w) {
        verbosity = v;
        warnings = w;
    }

    @Override
    public boolean hasWarning(String name) {
        return warnings.contains(name);
    }

    @Override
    public boolean verbose(int v) {
        return verbosity >= v;
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
            pw.print("; Generated code for " + fname + ".\n; Do not modify by hand\n" + code);
            pw.close();
        }
        catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }
}

