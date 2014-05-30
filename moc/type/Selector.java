package moc.type;

public class Selector {
    String name;
    Type paramType;
    String paramName;

    public Selector(String name) {
        this.name = name;
    }

    public Selector(String name, Type paramType, String paramName) {
        this.name = name;
        this.paramType = paramType;
        this.paramName = paramName;
    }

    public boolean hasParameter() {
        return paramType != null;
    }

    public String getName() {
        return name;
    }

    public Type getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public boolean equals(Selector other) {
        return name.equals(other.name)
            && (paramType == null && other.paramType == null
                || paramType.equals(other.paramType)
                    && paramName.equals(other.paramName));
    }
}

