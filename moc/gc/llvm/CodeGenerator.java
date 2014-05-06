package moc.gc.llvm;

import java.lang.StringBuilder;
import moc.type.Type;

class CodeGenerator {
    Machine machine;
    StringBuilder sb;

    RepresentationVisitor reprVisitor = new RepresentationVisitor();
    SizeVisitor sizeVisitor = new SizeVisitor();

    public CodeGenerator(Machine machine, StringBuilder sb) {
        this.machine = machine;
        this.sb = sb;
    }

    public void reset() {
        sb.setLength(0);
    }

    public String get() {
        String code = sb.toString();
        reset();
        return code;
    }

    // instructions generation in alphabetical order:
    public String bitcast(Type from, Expr what, Type to) {
        return castImpl("bitcast", typeName(from), what, typeName(to));
    }

    public String sext(String from, Expr what, String to) {
        return castImpl("sext", from, what, to);
    }

    public String trunc(String from, Expr what, String to) {
        return castImpl("trunc", from, what, to);
    }

    // implementation stuff:
    private String typeName(Type type) {
        return type.visit(reprVisitor);
    }

    private String castImpl(String op, String from, Expr what, String to) {
        // <tmp> = <op> <from> <what> to <to>
        String whatValue = machine.getValue(from, what, sb);
        String tmpCastedName = machine.getTmpName();

        machine.indent(sb);
        sb.append(tmpCastedName);
        sb.append(" = ");
        sb.append(op);
        sb.append(' ');
        sb.append(from);
        sb.append(' ');
        sb.append(whatValue);
        sb.append(" to ");
        sb.append(to);
        sb.append('\n');
        return tmpCastedName;
    }
}

