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

    /** {@code <where> = alloca <type>} */
    void alloca(String where, String type) {
        indent(sb);
        sb.append(where);
        sb.append(" = alloca ");
        sb.append(type);
        sb.append('\n');
    }

    /** {@code <tmp> = <op> <type> <lhs>, <rh>} */
    String binaryOperator(String op, String type, String lhs, String rhs) {
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

    /** {@code ) } */
    void callEnd() {
        sb.append(")\n");
    }

    /** {@code <tmp> = call <returnType> @<name>( } */
    String callNonVoid(String returnType, String name) {
        String tmp = machine.getTmpName();
        indent(sb);
        sb.append(tmp);
        sb.append(" = ");
        callImpl(returnType, name);
        return tmp;
    }

    /** {@code call <returnType> @<name>( } */
    void callVoid(String returnType, String name) {
        indent(sb);
        callImpl(returnType, name);
    }

    /** {@code call <returnType> @<name>( } */
    private void callImpl(String returnType, String name) {
        sb.append("call ");
        sb.append(returnType);
        sb.append(" @");
        sb.append(name);
        sb.append('(');
    }

    /** {@code <tmp> = <op> <from> <what> to <to>} */
    String cast(String op, String from, String what, String to) {
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

    /** {@code ; <comment>} */
    void comment(String comment) {
        sb.append("; ");
        sb.append(comment);
        sb.append('\n');
    }

    /** {@code call void @free(i8* <what>)} */
    void free(String what) {
        indent(sb);
        sb.append("call void @free(i8* ");
        sb.append(what);
        sb.append(")\n");
    }

    /** {@code <tmp> = getelementptr <type>* <lhs>, i64 <rhs>} */
    String getelementptr(String type, String lhs, String[] rhs) {
        String tmp = machine.getTmpName();
        indent(sb);
        sb.append(tmp);
        sb.append(" = getelementptr ");
        sb.append(type);
        sb.append("* ");
        sb.append(lhs);
        for(int i = 0; i < rhs.length; i += 2) {
            sb.append(", ");
            sb.append(rhs[i]);
            sb.append(' ');
            sb.append(rhs[i+1]);
        }
        sb.append('\n');
        return tmp;
    }

    String getValue(String type, moc.gc.Expr expr) {
        return machine.getValue(type, expr, sb);
    }

    /** {@code <tmp> = load <type>* <where> } */
    String load(String type, String what) {
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

    /** {@code <result> = call i8* @malloc(i64 <size>)} */
    String malloc(int size) {
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
        sb.append("ret void\n");
    }

    /** {@code ret <type> <what> } */
    void ret(String type, String what) {

        indent(sb);
        sb.append("ret ");
        sb.append(type);
        sb.append(' ');
        sb.append(what);
        sb.append('\n');
    }

    /** {@code <name> = internal constant [<lenght> x i8] c"<value>\00" } */
    String stringCstDeclaration(int length, String value) {
        String name = machine.getGlobalTmpName();

        declarationSb.append(name);
        declarationSb.append(" = internal constant [");
        declarationSb.append(length);
        declarationSb.append(" x i8] c\"");
        declarationSb.append(value);
        declarationSb.append("\\00\"\n");

        return name;
    }

    /** {@code store <type> <what>, <type>* <where> } */
    void store(String type, String what, String where) {
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

    // function declaration
    void beginDefine(String returnType, String name) {
        sb.append("define ");
        sb.append(returnType);
        sb.append(" @");
        sb.append(name);
        sb.append('(');
    }

    void parameter(String type, String name, boolean hasNext) {
        sb.append(type);
        sb.append(' ');
        sb.append(name);
        if (hasNext) {
            sb.append(", ");
        }
    }

    void body(String bloc) {
        sb.append(bloc);
    }

    void endDefine() {
        sb.append(") nounwind {\n"); // nounwind = no exceptions
    }

    void endFunction() {
        sb.append("}\n\n");
    }

    // implementation stuff:
    void indent(StringBuilder sb) {
        sb.append("    ");
    }
}

