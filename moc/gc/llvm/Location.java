package moc.gc.llvm;

final class Location implements moc.gc.Location {
    String name;

    Location(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

