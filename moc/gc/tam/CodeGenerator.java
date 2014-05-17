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

        String declarations =
            "CALL (SB) function_main\n"
        +   "HALT\n"
        ;

        this.declarationSb = new StringBuilder(declarations);
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

    void asm(String code) {
        indent();
        append(code);
        skipLine();
    }

    void comment(String txt) {
        append("; ");
        append(txt);
        skipLine();
    }

    void function(String name) {
        append("\nfunction_");
        append(name);
        append(":\n");
    }

    void load(int size, String loc) {
        indent();
        append("LOAD (");
        append(size);
        append(") ");
        append(loc);
        skipLine();
    }

    void loadi(int size) {
        indent();
        append("LOADI ");
        append(size);
        skipLine();
    }

    void loadl(Object what) {
        indent();
        append("LOADL ");
        append(what);
        skipLine();
    }

    void globalAsm(String code) {
        declAppend(code);
        declAppend('\n');
    }

    void globalComment(String comment) {
        declAppend("; ");
        declAppend(comment);
        declAppend('\n');
    }

    void pop(int d, int n) {
        indent();
        append("POP (");
        append(d);
        append(") ");
        append(n);
        skipLine();
    }

    void push(int size) {
        indent();
        append("PUSH ");
        append(size);
        skipLine();
    }

    void ret(int param_size, int return_size) {
        indent();
        append("RETURN (");
        append(return_size);
        append(") ");
        append(param_size);
        skipLine();
    }

    void subr(String op) {
        indent();
        append("SUBR ");
        append(op);
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

