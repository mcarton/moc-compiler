package moc.gc.llvm;

import moc.gc.ILocation;

final class Location implements ILocation {
    String name;

    Location(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

