package moc.gc.tam;

import java.util.ArrayList;
import moc.gc.*;
import moc.symbols.*;
import moc.type.*;
/**
 * The TAM machine and its generation functions.
 */
public class Machine extends AbstractMachine {
    SizeVisitor sizeVisitor = new SizeVisitor();

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
    }

    @Override
    public String getSuffix() {
        return "tam";
    }

    // location stuffs:
    @Override
    public void newFunction() {
    }

    @Override
    public void newBloc() {
    }

    @Override
    public Location getLocationFor(String name, Type type) {
        return null;
    }
 
    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType f, ArrayList<moc.gc.Location> parameters,
        String name, String bloc
    ) {
        return bloc; // TODO:code
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr expr) {
        return ""; // TODO:code
    }

    @Override
    public String genVarDecl(Type t, moc.gc.Location loc) {
        StringBuilder sb = new StringBuilder(50);

//      String type = t.visit(typeVisitor);

        sb.append("    ");
        sb.append("LOADL ");
        sb.append(t.visit(sizeVisitor));
        sb.append('\n');
        sb.append("    ");
        sb.append("SUBR Malloc");
        sb.append('\n');
        return sb.toString();
    }
    
    @Override
    public String genVarDecl(Type t, moc.gc.Location loc, moc.gc.Expr expr) {
        StringBuilder sb = new StringBuilder(50);

//      String type = t.visit(typeVisitor);

        sb.append("    ");
        sb.append("LOADL ");
        sb.append(t.visit(sizeVisitor));
        sb.append('\n');
        sb.append("    ");
        sb.append("SUBR Malloc");
        sb.append('\n'); 
        sb.append("expr.getCode()"); 
        sb.append('\n'); 
        return sb.toString();
    }

    @Override
    public Expr genInt(String txt) {
        return new Expr("\tLOADL " + txt + "\n");
    }
    public Expr genInt(int nb) {
        return genInt(Integer.toString(nb));
    }
    @Override
    public Expr genString(int length, String txt) {
        return new Expr("\tLOADL " + txt + "\n");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr("\tLOADL " + txt + "\n");
    }
    @Override
    public Expr genNull() {
        return new Expr("\tSUBR MVoid \n");
    }
    @Override
    public Expr genNew(Type t) {
        return null; // TODO:code
    }
    @Override
    public String genDelete(Type t, moc.gc.Expr expr) {
        return ""; // TODO:code
    }

    @Override
    public Expr genCall(
        String funName, FunctionType fun,
        ArrayList<moc.gc.Expr> exprs
    ) {
        return null; // TODO:code
    }
    @Override
    public Expr genSizeOf(Type type) {
        return genInt(type.visit(sizeVisitor));
    }

    @Override
    public Expr genIdent(InfoVar info) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genAff(Type t, moc.gc.Location loc, moc.gc.Expr gcrhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genNonAff(Type t, moc.gc.Expr expr) {
        // TODO:code
        return null;
    }

    @Override
    public Expr genSubInt(moc.gc.Expr expr) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genNotInt(moc.gc.Expr expr) {
        // TODO:code
        return null;
    }
    @Override
    public moc.gc.Expr genDeref(moc.gc.Expr expr) {
        return expr; // TODO:code
    }
    @Override
    public moc.gc.Expr genArrSub(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public moc.gc.Expr genCast(Type from, Type to, moc.gc.Expr expr) {
        return expr; // TODO:code
    }

    @Override
    public Expr genAddInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genSubInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genOrInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genMultInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genDivInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genModInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genAndInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }

    @Override
    public String genComment(String comment) {
        return("; " + comment);
    }
}

/** A visitor to get the size of types.
 */
class SizeVisitor implements TypeVisitor<Integer> {
    public Integer visit(IntegerType what)   { return 1; }
    public Integer visit(CharacterType what) { return 1; }

    public Integer visit(VoidType what)      { return 0; }
    public Integer visit(NullType what)      { return 1; }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }
    public Integer visit(Pointer what)       { return 1; }
}

