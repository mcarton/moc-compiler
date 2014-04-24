package moc.gc;

import java.util.ArrayList;
import moc.type.*;

/**
 * The TAM machine and its generation functions
 */
public class MTAM extends AbstractMachine {
    public MTAM(int verbosity, ArrayList<String> warnings) {
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
 
    // code generation stuffs:
    @Override
    public String genFunction(DFUNCTIONTYPE f, String name, String bloc) {
        return bloc; // TODO:code
    }

    @Override
    public String genVarDecl(DTYPE t, String name, String val) {
        return val;
    }

    @Override
    public String genCst(String txt) {
        return "\tLOADL " + txt + "\n";
    }
    @Override
    public String genString(String txt) {
        return "\tLOADL " + txt + "\n";
    }
    @Override
    public String genCharacter(String txt) {
        return "\tLOADL " + txt + "\n";
    }
    @Override
    public String genNull() {
        return "\tSUBR MVoid \n";
    }
    @Override
    public String genComment(String comment) {
        return("; " + comment);
    }
}

