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
    
    void call(String reg, String fun) {
        indent();
        append("CALL (");
        append(reg);
        append(") ");
        append(fun);
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

    void jump(String etiq) {
        indent();
        append("JUMP ");
        append(etiq);
        skipLine();
    }

    void jumpif(int cmp, String etiq) {
        indent();
        append("JUMPIF (");
        append(cmp);
        append(") ");
        append(etiq);
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

    void label(String name) {
        append(name);
        append(':');
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

    void loada(String loc) {
        indent();
        append("LOADA ");
        append(loc);
        skipLine();
    }

    void loadi(int size) {
        indent();
        append("LOADI (");
        append(size);
        append(")");
        skipLine();
    }

    void loadl(Object what) {
        indent();
        append("LOADL ");
        append(what);
        skipLine();
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

    void ret(int returnSize, int paramSize) {
        indent();
        append("RETURN (");
        append(paramSize);
        append(") ");
        append(returnSize);
        skipLine();
    }

    void storei(int n) {
        indent();
        append("STOREI (");
        append(n);
        append(")");
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

