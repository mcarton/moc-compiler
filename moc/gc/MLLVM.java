package moc.gc;

import java.util.ArrayList;
import moc.type.*;

/**
 * The TAM machine and its generation functions
 */
public class MLLVM extends AbstractMachine {
    public MLLVM(int verbosity, ArrayList<String> warnings) {
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
        return new ARRAY(what.getSize()*nbElements, what, nbElements);
    }
}

