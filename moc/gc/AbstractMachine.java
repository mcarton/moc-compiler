package moc.gc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import moc.compiler.MOCException;
import moc.type.*;

/**
 * Common stuffs of machines.
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
    public final boolean hasWarning(String name) {
        return allWarnings || warnings.contains(name);
    }

    @Override
    public final int verbosity() {
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
            pw.print(genComment("Generated code for " + fname + "."));
            pw.print(genComment("Do not modify by hand\n"));
            pw.print(code);
            pw.close();
        }
        catch (FileNotFoundException e) {
            throw new MOCException(e.getMessage());
        }
    }

    public final int stringSize(String unescaped) {
        int size = 1; // includes \0

        boolean backslash = false;
        for (int i = 1; i < unescaped.length()-1; ++i) { // exludes ""
            if (unescaped.charAt(i) == '\\') {
                if (backslash) {
                    ++size;
                }
                backslash = !backslash;
            }
            else {
                ++size;
                backslash = false;
            }
        }

        return size;
    }

    @Override
    public String genBlock(String code) {
        return code;
    }
    @Override
    public String genBlockInst(String code) {
        return code;
    }

    @Override
    public String genIfInst(String code) {
        return code;
    }
    @Override
    public String genElseIf(String code) {
        return code;
    }

    /**
     * Default implementation that returns an empty string.
     */
    @Override
    public String genUsing(String name, Type type) {
        return genComment("using " + name + " = " + type);
    }

    /** Default implementation that returns the given expression. */
    @Override
    public IExpr genParen(IExpr expr) {
        return expr;
    }
}

