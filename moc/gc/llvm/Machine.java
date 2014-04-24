package moc.gc.llvm;

import java.util.ArrayList;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.util.Iterator;
import moc.gc.*;

import moc.type.*;
import moc.tds.*;

/**
 * The TAM machine and its generation functions
 */
public class Machine extends AbstractMachine {
    int lastTmp = 0; // name of the last generated temporary
    int bloc = 0; // the bloc we are in

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

    // location stuffs:
    @Override
    public void newFunction() {
        lastTmp = 0;
        bloc = 0;
    }

    @Override
    public void newBloc() {
        ++bloc;
    }

    @Override
    public Location getLocationFor(String name, DTYPE type) {
        return new Location('%' + name + bloc);
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
    public String genReturn(DFUNCTIONTYPE f, moc.gc.Expr gcexpr) {
        Expr expr = (Expr)gcexpr;

        StringBuilder sb = new StringBuilder();
        RepresentationVisitor rv = new RepresentationVisitor();

        String returnTypeRepr = f.getReturnType().visit(rv);
        String returnValueName;

        if(expr.getLoc() != null) {
            returnValueName = getTmpName();
            sb.append("    ");
            sb.append(returnValueName);
            sb.append(" = load ");
            sb.append(returnTypeRepr);
            sb.append("* ");
            sb.append(expr.getLoc().getRepr());
            sb.append('\n');
        }
        else {
            returnValueName = expr.getCode();
        }

        sb.append("    ret ");
        sb.append(returnTypeRepr);
        sb.append(' ');
        sb.append(returnValueName);
        sb.append('\n');

        return sb.toString();
    }

    @Override
    public String genVarDecl(DTYPE t, moc.gc.Location loc, moc.gc.Expr expr) {
        RepresentationVisitor rv = new RepresentationVisitor();
        StringBuilder sb = new StringBuilder(50);

        String type = t.visit(rv);

        sb.append("    ");
        sb.append(loc.getRepr());
        sb.append(" = alloca ");
        sb.append(type);
        sb.append('\n');

        sb.append("    store ");
        sb.append(type);
        sb.append(' ');
        if(expr.getLoc() != null) {
            sb.append(expr.getLoc().getRepr());
        }
        else {
            sb.append(expr.getCode());
        }
        sb.append(", ");
        sb.append(type);
        sb.append("* ");
        sb.append(loc.getRepr());
        sb.append('\n');

        return sb.toString();
    }

    @Override
    public Expr genNull() {
        // first `null` means the expression is constant, it is not related to
        // we are creating a null constant
        return new Expr(null, "null");
    }
    @Override
    public Expr genInt(String txt) {
        return new Expr(null, txt);
    }
    @Override
    public Expr genString(String txt) {
        return new Expr(new Location("%TODO"), "TODO");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr(null, txt); // TODO:string
    }

    @Override
    public Expr genIdent(String name, INFOVAR info) {
        return new Expr((Location)info.getLoc(), null);
    }

    @Override
    public String genComment(String comment) {
        return "    ; " + comment + '\n';
    }

    private String getTmpName() {
        return String.valueOf("%" + ++lastTmp);
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
