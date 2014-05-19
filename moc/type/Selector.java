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
        this.paramName= paramName;
    }
}

