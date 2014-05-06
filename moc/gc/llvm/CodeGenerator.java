package moc.gc.llvm;

import java.lang.StringBuilder;
import moc.type.Type;

final class CodeGenerator {
    Machine machine;
    StringBuilder sb;
    StringBuilder declarationSb;

    RepresentationVisitor reprVisitor = new RepresentationVisitor();

    CodeGenerator(Machine machine) {
        this.machine = machine;
        this.sb = new StringBuilder();

        String declarations =
                "declare i8* @malloc(i64)\n"
              + "declare void @free(i8*)\n";

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

    String typeName(Type type) {
        return type.visit(reprVisitor);
    }

    // instructions generation in alphabetical order:
    void alloca(String where, String what) {
        indent(sb);
        sb.append(where);
        sb.append(" = alloca ");
        sb.append(what);
        sb.append('\n');
    }

    String binaryOperator(String op, String type, String lhs, String rhs) {
        // <tmp> = <op> <type> <lhs>, <rh>
        String tmp = machine.getTmpName();
        indent(sb);
        sb.append(tmp);
        sb.append(" = ");
        sb.append(op);
        sb.append(' ');
        sb.append(type);
        sb.append(' ');
        sb.append(lhs);
        sb.append(", ");
        sb.append(rhs);
        sb.append('\n');
        return tmp;
    }

    String cast(String op, String from, String what, String to) {
        // <tmp> = <op> <from> <what> to <to>
        String tmpCastedName = machine.getTmpName();
        indent(sb);
        sb.append(tmpCastedName);
        sb.append(" = ");
        sb.append(op);
        sb.append(' ');
        sb.append(from);
        sb.append(' ');
        sb.append(what);
        sb.append(" to ");
        sb.append(to);
        sb.append('\n');
        return tmpCastedName;
    }

    void comment(String comment) {
        sb.append("; ");
        sb.append(comment);
        sb.append('\n');
    }

    void free(String what) {
        indent(sb);
        sb.append("call void @free(i8* ");
        sb.append(what);
        sb.append(")\n");
    }

    String getValue(String type, moc.gc.Expr expr) {
        return machine.getValue(type, expr, sb);
    }

    String load(String type, String what) {
        // <tmp> = load <type>* <where>
        String tmpValueName = machine.getTmpName();
        indent(sb);
        sb.append(tmpValueName);
        sb.append(" = load ");
        sb.append(type);
        sb.append("* ");
        sb.append(what);
        sb.append('\n');
        return tmpValueName;
    }

    String malloc(int size) {
        // <result> = call i8* @malloc(i64 <size>)
        String tmpPtr = machine.getTmpName();
        indent(sb);
        sb.append(tmpPtr);
        sb.append(" = call i8* @malloc(i64 ");
        sb.append(size);
        sb.append(")\n");
        return tmpPtr;
    }

    /** Special case for "ret void". @see #ret(String, String) */
    void ret() {
        indent(sb);
        sb.append("ret void");
    }

    void ret(String type, String what) {
        // ret <type> <what>

        indent(sb);
        sb.append("ret ");
        sb.append(type);
        sb.append(' ');
        sb.append(what);
        sb.append('\n');
    }

    String stringCstDeclaration(int length, String value) {
        String name = machine.getGlobalTmpName();

        // <name> = internal constant [<lenght> x i8] c"<value>\00"
        indent(declarationSb);
        declarationSb.append(name);
        declarationSb.append(" = internal constant [");
        declarationSb.append(length);
        declarationSb.append(" x i8] c\"");
        declarationSb.append(value);
        declarationSb.append("\\00\"\n");

        return name;
    }

    void store(String type, String what, String where) {
        // store <type> <what>, <type>* <where>
        indent(sb);
        sb.append("store ");
        sb.append(type);
        sb.append(' ');
        sb.append(what);
        sb.append(", ");
        sb.append(type);
        sb.append("* ");
        sb.append(where);
        sb.append('\n');
    }

    // implementation stuff:
    void indent(StringBuilder sb) {
        sb.append("    ");
    }
}
