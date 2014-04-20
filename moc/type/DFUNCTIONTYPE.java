package moc.type;

import java.util.ArrayList;

public class DFUNCTIONTYPE {
    DTYPE returnType;
    LTYPES parameterTypes;

    public DFUNCTIONTYPE(DTYPE returnType, LTYPES parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public DTYPE getReturnType() {
        return returnType;
    }

    public LTYPES getParameterTypes() {
        return parameterTypes;
    }

    public boolean callableWith(LTYPES params) {
        return parameterTypes.constructsFrom(params);
    }

    public String toString() {
        return parameterTypes + "->" + returnType;
    }
}

