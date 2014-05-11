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

    String getValue(String type, moc.gc.Expr expr) {
        return machine.getValue(type, expr);
    }

    String typeName(Type type) {
        return type.visit(reprVisitor);
    }

    // instructions generation in alphabetical order:

    /** {@code <where> = alloca <type>} */
    void alloca(String where, String type) {
        indent();
        append(where);
        append(" = alloca ");
        append(type);
        append('\n');
    }

    void asm(String code) {
        indent();
        append(code);
        append('\n');
    }

    /** {@code <tmp> = <op> <type> <lhs>, <rh>} */
    String binaryOperator(String op, String type, String lhs, String rhs) {
        String tmp = machine.getTmpName();
        indent();
        append(tmp);
        append(" = ");
        append(op);
        append(' ');
        append(type);
        append(' ');
        append(lhs);
        append(", ");
        append(rhs);
        append('\n');
        return tmp;
    }

    /**
     * {@code br label %<label> }
     *
     * Note: a {@code br} ends an llvm block.
     */
    void br(String label) {
        indent();
        append("br label %");
        append(label);
        append('\n');
    }

    /**
     * {@code br i1 <cond>, label %<thenLabel>, label %<elseLabel>}
     *
     * Note: a {@code br} ends an llvm block.
     */
    void br(String cond, String thenLabel, String elseLabel) {
        indent();
        append("br i1 ");
        append(cond);
        append(", label %");
        append(thenLabel);
        append(", label %");
        append(elseLabel);
        append('\n');
    }

    /** {@code ) } */
    void callEnd() {
        append(")\n");
    }

    /** {@code <tmp> = call <returnType> @<name>( } */
    String callNonVoid(String returnType, String name) {
        String tmp = machine.getTmpName();
        indent();
        append(tmp);
        append(" = ");
        callImpl(returnType, name);
        return tmp;
    }

    /** {@code call <returnType> @<name>( } */
    void callVoid(String returnType, String name) {
        indent();
        callImpl(returnType, name);
    }

    /** {@code call <returnType> @<name>( } */
    private void callImpl(String returnType, String name) {
        append("call ");
        append(returnType);
        append(" @");
        append(name);
        append('(');
    }

    /** {@code <tmp> = <op> <from> <what> to <to>} */
    String cast(String op, String from, String what, String to) {
        String tmpCastedName = machine.getTmpName();
        indent();
        castImpl(tmpCastedName, op, from, what, to);
        return tmpCastedName;
    }

    void cast(String where, String op, String from, String what, String to) {
        indent();
        castImpl(where, op, from, what, to);
    }

    private void castImpl(String where, String op, String from, String what, String to) {
        append(where);
        append(" = ");
        append(op);
        append(' ');
        append(from);
        append(' ');
        append(what);
        append(" to ");
        append(to);
        append('\n');
    }

    /** {@code ; <comment>} */
    void comment(String comment) {
        append("; ");
        append(comment);
        append('\n');
    }

    /** {@code call void @free(i8* <what>)} */
    void free(String what) {
        indent();
        append("call void @free(i8* ");
        append(what);
        append(")\n");
    }

    /** {@code <tmp> = getelementptr <type>* <lhs>, i64 <rhs>} */
    String getelementptr(String type, String lhs, String[] rhs) {
        String tmp = machine.getTmpName();
        indent();
        append(tmp);
        append(" = getelementptr ");
        append(type);
        append("* ");
        append(lhs);
        for(int i = 0; i < rhs.length; i += 2) {
            append(", ");
            append(rhs[i]);
            append(' ');
            append(rhs[i+1]);
        }
        append('\n');
        return tmp;
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
        append(":\n");
    }

    /** {@code <tmp> = load <type>* <where> } */
    String load(String type, String what) {
        String tmpValueName = machine.getTmpName();
        indent();
        append(tmpValueName);
        append(" = load ");
        append(type);
        append("* ");
        append(what);
        append('\n');
        return tmpValueName;
    }

    /** {@code <result> = call i8* @malloc(i64 <size>)} */
    String malloc(int size) {
        String tmpPtr = machine.getTmpName();
        indent();
        append(tmpPtr);
        append(" = call i8* @malloc(i64 ");
        append(size);
        append(")\n");
        return tmpPtr;
    }

    /**
     * {@code <tmp> = bitcast i8 0 to i8 }
     *
     * There is no real no-op instruction in llvm IR code, but this is
     * optimized out by llc and used by llvm-gcc.
     */
    void noop() {
        String tmp = machine.getTmpName();
        indent();
        append(tmp);
        append(" = bitcast i8 0 to i8\n");
    }

    /**
     * Special case for "ret void".
     *
     * Note: a {@code ret} ends an llvm code block.
     * @see #ret(String, String)
     */
    void ret() {
        indent();
        append("ret void\n");
    }

    /**
     * {@code ret <type> <what> }
     *
     * Note: a {@code ret} ends an llvm code block.
     */
    void ret(String type, String what) {
        indent();
        append("ret ");
        append(type);
        append(' ');
        append(what);
        append('\n');
    }

    /** {@code <name> = internal constant [<lenght> x i8] c"<value>\00" } */
    String stringCstDeclaration(int length, String value) {
        String name = machine.getGlobalTmpName();

        declAppend(name);
        declAppend(" = internal constant [");
        declAppend(length);
        declAppend(" x i8] c\"");
        declAppend(value);
        declAppend("\\00\"\n");

        return name;
    }

    /** {@code store <type> <what>, <type>* <where> } */
    void store(String type, String what, String where) {
        indent();
        append("store ");
        append(type);
        append(' ');
        append(what);
        append(", ");
        append(type);
        append("* ");
        append(where);
        append('\n');
    }

    void unreachable() {
        indent();
        append("unreachable\n");
    }

    // function declaration
    void beginDefine(String returnType, String name) {
        append("define ");
        append(returnType);
        append(" @");
        append(name);
        append('(');
    }

    void parameter(String type, String name, boolean hasNext) {
        append(type);
        append(' ');
        append(name);
        if (hasNext) {
            append(", ");
        }
    }

    void body(String bloc) {
        append(bloc);
    }

    void endDefine() {
        append(") nounwind {\n"); // nounwind = no exceptions
    }

    void endFunction() {
        append("}\n\n");
    }

    // implementation stuff:
    void append(Object what) {
        sb.append(what);
    }

    void declAppend(Object what) {
        declarationSb.append(what);
    }

    void indent() {
        append("  ");
    }

    void skipLine() {
        append('\n');
    }
}

