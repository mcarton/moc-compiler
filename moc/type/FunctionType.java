package moc.type;

import java.util.ArrayList;

public final class FunctionType {
    Type returnType;
    TypeList parameterTypes;

    public FunctionType(Type returnType, TypeList parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public TypeList getParameterTypes() {
        return parameterTypes;
    }

    public boolean callableWith(TypeList params) {
        return parameterTypes.constructsFrom(params);
    }

    public String toString() {
        return parameterTypes + "->" + returnType;
    }
}

