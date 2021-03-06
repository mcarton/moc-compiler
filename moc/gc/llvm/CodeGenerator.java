package moc.gc.llvm;

import java.lang.StringBuilder;
import moc.gc.IExpr;
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
            + "declare void @free(i8*)\n"
            + "declare void @llvm.memcpy.p0i8.p0i8.i64(i8* nocapture, i8* nocapture readonly, i64, i32, i1)\n"
            + "\n"
            + "%mocc.method = type { i8*, void (...)* }\n"
            + "%mocc.vtable = type { %mocc.method** }\n"
            + "\n"
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

    String getValue(String type, IExpr expr) {
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

    /** {@code <tmp> = call <returnType> <name>( } */
    String callNonVoid(String returnType, String name) {
        String tmp = machine.getTmpName();
        indent();
        append(tmp);
        append(" = ");
        callImpl(returnType, name);
        return tmp;
    }

    /** {@code call void <name>( } */
    void callVoid(String name) {
        indent();
        callImpl("void", name);
    }

    /** {@code call <type> <name>( } */
    void callVoid(String type, String name) {
        indent();
        callImpl(type, name);
    }

    /** {@code call <returnType> <name>( } */
    private void callImpl(String returnType, String name) {
        append("call ");
        append(returnType);
        append(' ');
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

    /** {@code <where> = <op> <from> <what> to <to>} */
    void cast(String where, String op, String from, String what, String to) {
        indent();
        castImpl(where, op, from, what, to);
    }

    /** {@code <where> = <op> <from> <what> to <to>} */
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

    /** {@code <name> = type } and an opening bracket. */
    void classBegin(String name) {
        declAppend(name);
        declAppend(" = type { ");
    }

    /** {@code <type> } optionally followed by an comma. */
    void classAddMember(String type, boolean hasNext) {
        declAppend(type);
        if (hasNext) {
            declAppend(", ");
        }
    }

    /** Closing bracket. */
    void classEnd() {
        declAppend(" }\n");
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

    /** {@code <where> = getelementptr <type>* <lhs>, i64 <rhs>} */
    void getelementptr(String where, String type, String lhs, String[] rhs) {
        indent();
        append(where);
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
    }

    /** {@code <tmp> = getelementptr <type>* <lhs>, i64 <rhs>} */
    String getelementptr(String type, String lhs, String[] rhs) {
        String tmp = machine.getTmpName();
        getelementptr(tmp, type, lhs, rhs);
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

    /** {@code ; <label>: <name> } */
    void implicitLabel(String name) {
        append("\n; <label>:");
        append(name);
        append(":\n");
    }

    /** {@code <name>: } */
    void label(String name) {
        append('\n');
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
    String malloc(String size) {
        String tmpPtr = machine.getTmpName();
        indent();
        append(tmpPtr);
        append(" = call i8* @malloc(i64 ");
        append(size);
        append(")\n");
        return tmpPtr;
    }

    /** {@code call void @llvm.memcpy.p0i8.p0i8.i64(i8* <where>, i8* <what>, i64 <size>, i32 0, i1 false) }
     */
    void memcpy(String where, String what, int size) {
        indent();
        append("call void @llvm.memcpy.p0i8.p0i8.i64(i8* ");
        append(where);
        append(", i8* ");
        append(what);
        append(", i64 ");
        append(size);
        append(", i32 0, i1 false)\n"); // 0 alignment means not aligned
    }

    /**
     * {@code
     *     @ptr.<name> = internal constant %mocc.method {
     *         i8* bitcast ([<size> x i8]* @name.<name> to i8*),
     *         void (...)* bitcast(<type> @method.<name> to void (...)*)
     *     }
     * }
     */
    void methodCstDeclaration(String type, String name, int size) {
        declAppend("@ptr.");
        declAppend(name);
        declAppend(" = internal constant %mocc.method {\n");
        declAppend("    i8* bitcast ([");
        declAppend(size);
        declAppend(" x i8]* @name.");
        declAppend(name);
        declAppend(" to i8*),\n");
        declAppend("    void (...)* bitcast(");
        declAppend(type);
        declAppend(" @method.");
        declAppend(name);
        declAppend(" to void (...)*)");
        declAppend("\n}\n");
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
        stringCstDeclaration(name, length, value);
        return name;
    }

    /** {@code <where> = internal constant [<length> x i8] c"<value>\00" } */
    void stringCstDeclaration(String where, int length, String value) {
        declAppend(where);
        declAppend(" = internal constant [");
        declAppend(length);
        declAppend(" x i8] c\"");
        declAppend(value);
        declAppend("\\00\"\n");
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

    /** {@code @vtable.class.<name> = internal constant [<size> x %mocc.method*] [ } */
    void vtableBegin(String name, int size) {
        declAppend("@vtable.");
        declAppend(name);
        declAppend(" = internal constant [");
        declAppend(size);
        declAppend(" x %mocc.method*] [\n");
    }

    void vtableAdd(String mangledName) {
        declAppend("    %mocc.method* @ptr.");
        declAppend(mangledName);
        declAppend(",\n");
    }

    void vtableEnd() {
        declAppend("    %mocc.method* null\n]\n");
    }

    /** {@code
     *      @vtablePtr.<name> = internal constant %mocc.method** bitcast (
     *          [<size> x %mocc.method*]* @vtable.<name> to %mocc.method**
     *      )
     *  }
     */
    void vtablePtr(String name, int size) {
        declAppend("@vtablePtr.");
        declAppend(name);
        declAppend(" = internal constant %mocc.method** bitcast ([");
        declAppend(size);
        declAppend(" x %mocc.method*]* @vtable.");
        declAppend(name);
        declAppend(" to %mocc.method**)\n\n");
    }

    // function declaration

    /** {@code define <returnType> <name> (} */
    void beginDefine(String returnType, String name) {
        append("define ");
        append(returnType);
        append(' ');
        append(name);
        append('(');
    }

    /** {@code <type> <name>} optionally preceded by a comma */
    void parameter(boolean hasPrevious, String type, String name) {
        if (hasPrevious) {
            append(", ");
        }
        append(type);
        append(' ');
        append(name);
    }

    void body(String block) {
        append(block);
    }

    /** {@code ) nounwind } followed by an opening bracket */
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

