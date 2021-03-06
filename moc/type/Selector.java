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

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (other instanceof Selector) {
            Selector otherSelector = (Selector)other;
            return name.equals(otherSelector.name)
                && (paramType == null && otherSelector.paramType == null
                    || paramType.equals(otherSelector.paramType)
                        && paramName.equals(otherSelector.paramName));
        }
        else {
            return false;
        }
    }
}

