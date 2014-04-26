package moc.gc.tam;

import java.util.ArrayList;
import moc.type.*;
import moc.gc.*;
import moc.symbols.*;

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
    @Override public Type getCharType() {
        return new CharacterType(1);
    }
    @Override public Type getIntType() {
        return new IntegerType(1);
    }
    @Override public Type getPtrType(Type what) {
        return new Pointer(1, what);
    }
    @Override public Type getArrayType(Type what, int nbElements) {
        return new Array(what, nbElements);
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
        return ""; // TODO:code
    }
    @Override
    public String genVarDecl(Type t, moc.gc.Location loc, moc.gc.Expr expr) {
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
    public Expr genNew(Type t) {
        return null; // TODO:code
    }
    @Override
    public String genDelete(Type t, moc.gc.Location loc) {
        return ""; // TODO:code
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
    public Expr genAdd(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }

    @Override
    public String genComment(String comment) {
        return("; " + comment);
    }
}

