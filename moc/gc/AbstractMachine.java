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
    public final TypedExpr genUnaryOp(String what, Type type, Expr expr) {
        // TODO:MOC: booleans with not
        if (type instanceof IntegerType) {
            if (what.equals("+")) {
                return new TypedExpr(genAddInt(expr), new IntegerType());
            }
            else if (what.equals("-")) {
                return new TypedExpr(genSubInt(expr), new IntegerType());
            }
            else if (what.equals("!")) {
                return new TypedExpr(genNotInt(expr), new IntegerType());
            }
        }
        return null;
    }

    /** Default implementation that returns the given expression. */
    @Override
    public Expr genAddInt(Expr expr) {
        return expr;
    }

    /** Default implementation that returns the given expression. */
    @Override
    public Expr genParen(Expr expr) {
        return expr;
    }

    @Override
    public final TypedExpr genBinaryOp(
        String what, Type lhsType, Expr lhs, Type rhsType, Expr rhs
    ) {
        // TODO:MOC: booleans with or and and
        if (lhsType instanceof IntegerType && rhsType instanceof IntegerType) {
            if (what.equals("+")) {
                return new TypedExpr(genAddInt(lhs, rhs), new IntegerType());
            }
            else if (what.equals("-")) {
                return new TypedExpr(genSubInt(lhs, rhs), new IntegerType());
            }
            else if (what.equals("or")) {
                return new TypedExpr(genOrInt(lhs, rhs), new IntegerType());
            }
            else if (what.equals("*")) {
                return new TypedExpr(genMultInt(lhs, rhs), new IntegerType());
            }
            else if (what.equals("/")) {
                return new TypedExpr(genDivInt(lhs, rhs), new IntegerType());
            }
            else if (what.equals("%")) {
                return new TypedExpr(genModInt(lhs, rhs), new IntegerType());
            }
            else if (what.equals("&&")) {
                return new TypedExpr(genAndInt(lhs, rhs), new IntegerType());
            }
        }
        return null;
    }
}

