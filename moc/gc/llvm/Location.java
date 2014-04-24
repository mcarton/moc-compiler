package moc.gc.llvm;

public class Location implements moc.gc.Location {
    String name;

    public Location(String name) {
        this.name = name;
    }

    @Override
    public String getRepr() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

