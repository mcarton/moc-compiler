package moc.gc.tam;

import java.util.ArrayList;
import moc.type.*;
import moc.gc.*;
import moc.tds.*;

/**
 * The TAM machine and its generation functions
 */
public class Machine extends AbstractMachine {
    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
    }

    @Override
    public String getSuffix() {
        return "tam";
    }

    // type stuffs:
    @Override public DTYPE getCharType() {
        return new CHARACTER_t(1);
    }
    @Override public DTYPE getIntType() {
        return new INTEGER_t(1);
    }
    @Override public DTYPE getPtrType(DTYPE what) {
        return new POINTER(1, what);
    }
    @Override public DTYPE getArrayType(DTYPE what, int nbElements) {
        return new ARRAY(what, nbElements);
    }

    // location stuffs:
    @Override
    public void newFunction() {
    }

    @Override
    public void newBloc() {
    }

    @Override
    public Location getLocationFor(String name, DTYPE type) {
        return null;
    }
 
    // code generation stuffs:
    @Override
    public String genFunction(DFUNCTIONTYPE f, String name, String bloc) {
        return bloc; // TODO:code
    }

    @Override
    public String genReturn(DFUNCTIONTYPE f, moc.gc.Expr expr) {
        return ""; // TODO:code
    }

    @Override
    public String genVarDecl(DTYPE t, moc.gc.Location loc) {
        return ""; // TODO:code
    }
    @Override
    public String genVarDecl(DTYPE t, moc.gc.Location loc, moc.gc.Expr expr) {
        return ""; // TODO:code
    }

    @Override
    public Expr genInt(String txt) {
        return new Expr("\tLOADL " + txt + "\n");
    }
    @Override
    public Expr genString(String txt) {
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
    public Expr genNew(DTYPE t) {
        return null; // TODO:code
    }
    @Override
    public String genDelete(DTYPE t, moc.gc.Location loc) {
        return ""; // TODO:code
    }

    @Override
    public Expr genIdent(INFOVAR info) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genAff(DTYPE t, moc.gc.Location loc, moc.gc.Expr gcrhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genNonAff(DTYPE t, moc.gc.Expr expr) {
        // TODO:code
        return null;
    }

    @Override
    public Expr genAdd(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }

    @Override
    public String genComment(String comment) {
        return("; " + comment);
    }
}

