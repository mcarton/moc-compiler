package moc.gc.tam;

import java.lang.StringBuilder;
import moc.type.Type;

final class CodeGenerator {
    Machine machine;
    StringBuilder sb;
    StringBuilder declarationSb;

    CodeGenerator(Machine machine) {
        this.machine = machine;
        this.sb = new StringBuilder();
        this.declarationSb = new StringBuilder();
    }

    void reset() {
        sb.setLength(0);
    }

    String get() {
        String code = sb.toString();
        reset();
        return code;
    }

    String getDeclaration() {
        return declarationSb.toString();
    }

    // instructions generation in alphabetical order:

    void comment(String txt) {
        append("; ");
        append(txt);
        skipLine();
    }

    void load(int size, String loc) {
        indent();
        append("LOAD (");
        append(size);
        append(") ");
        append(loc);
        skipLine();
    }

    void loadl(Object what) {
        indent();
        append("LOADL ");
        append(what);
        skipLine();
    }

    void subr(String op) {
        indent();
        append("SUBR ");
        append(op);
        skipLine();
    }

    void push(int size) {
        indent();
        append("PUSH ");
        append(size);
        skipLine();
    }

    // implementation stuff:
    void append(Object what) {
        sb.append(what);
    }

    void declAppend(Object what) {
        declarationSb.append(what);
    }

    void indent() {
        append("    ");
    }

    void skipLine() {
        append('\n');
    }
}

