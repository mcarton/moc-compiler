package moc.gc.llvm;

import java.util.ArrayList;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.util.Iterator;
import moc.gc.*;

import moc.type.*;

/**
 * The TAM machine and its generation functions
 */
public class Machine extends AbstractMachine {
    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
    }

    @Override
    public String getSuffix() {
        return "ll";
    }

    // type size stuffs:
    @Override public DTYPE getCharType() {
        return new CHARACTER_t(1);
    }
    @Override public DTYPE getIntType() {
        return new INTEGER_t(8);
    }
    @Override public DTYPE getPtrType(DTYPE what) {
        return new POINTER(8, what);
    }
    @Override public DTYPE getArrayType(DTYPE what, int nbElements) {
        return new ARRAY(what, nbElements);
    }

    // code generation stuffs:
    @Override
    public String genFunction(DFUNCTIONTYPE f, String name, String bloc) {
        RepresentationVisitor rv = new RepresentationVisitor();
        StringBuilder sb = new StringBuilder(name.length() + bloc.length());

        sb.append("define ");
        sb.append(f.getReturnType().visit(rv));
        sb.append(" @");
        sb.append(name);
        sb.append('(');

        Iterator<DTYPE> it = f.getParameterTypes().iterator();
        while(it.hasNext()) {
            sb.append(it.next().visit(rv));
            sb.append(' ');
            sb.append("%name");
            if(it.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(") nounwind {\n"); // nounwind = no exceptions
        sb.append(bloc);
        sb.append("}\n\n");

        return sb.toString();
    }

    @Override
    public String genVarDecl(DTYPE t, String name, String val) {
        RepresentationVisitor rv = new RepresentationVisitor();
        StringBuilder sb = new StringBuilder(name.length() + val.length());

        String type = t.visit(rv);

        sb.append("    %");
        sb.append(name);
        sb.append(" = alloca ");
        sb.append(type);
        sb.append('\n');

        sb.append("    store ");
        sb.append(type);
        sb.append(' ');
        sb.append(val);
        sb.append(", ");
        sb.append(type);
        sb.append("* %");
        sb.append(name);
        sb.append('\n');

        return sb.toString();
    }

    @Override
    public String genNull() {
        return "null";
    }
    @Override
    public String genInt(String txt) {
        return txt;
    }
    @Override
    public String genString(String txt) {
        return "TODO";
    }
    @Override
    public String genCharacter(String txt) {
        return txt; // TODO:string
    }

    @Override
    public String genComment(String comment) {
        return "    ; " + comment + '\n';
    }
}

class RepresentationVisitor implements TypeVisitor<String> {
    public String visit(DTYPE what) {
        throw new UnsupportedOperationException(
            "This visitor does not support the type of " + what
        );
    }

    public String visit(INTEGER_t what)    { return "i64"; }
    public String visit(CHARACTER_t what)  { return "i8"; }

    public String visit(VOID_t what)       { return "void"; }
    public String visit(NULL_t what)       { return "i8*"; }

    public String visit(ARRAY what) {
        return "["
            + what.getNbElements()
            + " x "
            + what.getPointee().visit(this)
            + "]";
    }
    public String visit(POINTER what) {
        return what.getPointee().visit(this) + "*";
    }

}
