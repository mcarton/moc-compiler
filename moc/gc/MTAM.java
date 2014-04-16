package moc.gc;

import moc.type.*;

/**
 * The TAM machine and its generation functions
 */
public class MTAM extends AbstractMachine {
    @Override
    public String getSuffix() {
        return "tam";
    }

    // type size stuffs:
    @Override public DTYPE getCharType() {
        return new CHARACTER_t(1);
    }
    @Override public DTYPE getIntType() {
        return new INTEGER_t(1);
    }
    @Override public DTYPE getPtrType(DTYPE what) {
        return new POINTER(1, what);
    }
}

