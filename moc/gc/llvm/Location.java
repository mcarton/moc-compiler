package moc.gc.llvm;

import moc.gc.ILocation;
import moc.type.ClassType;

final class Location implements ILocation {
    String name;
    ClassType classType;
    int memberNumber;

    Location(String name) {
        this(name, null, -1);
    }

    /**
     * A location for a member variable.
     */
    Location(String name, ClassType clazz, int memberNumber) {
        this.name = name;
        this.classType = clazz;
        this.memberNumber = memberNumber;
    }

    boolean isMember() {
        return classType != null;
    }

    ClassType getClassType() {
        return classType;
    }

    int getMemberNumber() {
        return memberNumber;
    }

    @Override
    public String toString() {
        return isMember() ? "self->" + name : name;
    }
}

