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

    /**
     * @inheritDoc
     *
     * @return The type of the string using {@link #getArrayType}.
     */
    @Override
    public Type getStringType(String string) {
        int nbChar = string.length()
                +1 /* includes '\0' */
                -2 /* excludes "" */;
        return getArrayType(getCharType(), nbChar);
    }

    @Override
    public final TypedExpr genUnaryOp(String what, Type type, Expr expr) {
        // TODO:MOC: booleans with not
        if(type instanceof IntegerType) {
            if(what.equals("+")) {
                return new TypedExpr(genAddInt(expr), getIntType());
            }
            else if(what.equals("-")) {
                return new TypedExpr(genSubInt(expr), getIntType());
            }
            else if(what.equals("!")) {
                return new TypedExpr(genNotInt(expr), getIntType());
            }
        }
        return null;
    }

    /** Default implementation that returns the given expression. */
    @Override
    public Expr genAddInt(Expr expr) {
        return expr;
    }

    @Override
    public final TypedExpr genBinaryOp(
        String what, Type lhsType, Expr lhs, Type rhsType, Expr rhs
    ) {
        // TODO:MOC: booleans with or and and
        if(lhsType instanceof IntegerType && rhsType instanceof IntegerType) {
            if(what.equals("+")) {
                return new TypedExpr(genAddInt(lhs, rhs), getIntType());
            }
            else if(what.equals("-")) {
                return new TypedExpr(genSubInt(lhs, rhs), getIntType());
            }
            else if(what.equals("or")) {
                return new TypedExpr(genOrInt(lhs, rhs), getIntType());
            }
            else if(what.equals("*")) {
                return new TypedExpr(genMultInt(lhs, rhs), getIntType());
            }
            else if(what.equals("/")) {
                return new TypedExpr(genDivInt(lhs, rhs), getIntType());
            }
            else if(what.equals("%")) {
                return new TypedExpr(genModInt(lhs, rhs), getIntType());
            }
            else if(what.equals("&&")) {
                return new TypedExpr(genAndInt(lhs, rhs), getIntType());
            }
        }
        return null;
    }
}

