package moc.gc.llvm;

import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import moc.compiler.MOCException;
import moc.gc.*;
import moc.symbols.*;
import moc.type.*;

/**
 * The llvm machine and its generation functions
 */
public final class Machine extends AbstractMachine {
    int lastGlobalTmp = -1;
    int lastTmp = 0; // name of the last generated temporary
    int block = -1; // the block we are in
    int labelCount = 0;
    Map<String, String> binaryOperators = new HashMap<String, String>();

    SizeVisitor sizeVisitor = new SizeVisitor();
    CodeGenerator cg = new CodeGenerator(this);

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);

        binaryOperators.put( "+", "add");
        binaryOperators.put( "-", "sub");
        binaryOperators.put( "*", "mul");
        binaryOperators.put( "/", "sdiv");
        binaryOperators.put( "%", "srem");
        binaryOperators.put("||", "or");
        binaryOperators.put("&&", "and");
        binaryOperators.put("<" , "icmp slt");
        binaryOperators.put("<=", "icmp sle");
        binaryOperators.put(">" , "icmp sgt");
        binaryOperators.put(">=", "icmp sge");
        binaryOperators.put("==", "icmp eq");
        binaryOperators.put("!=", "icmp ne");
    }

    @Override
    public void writeCode(String fname, String code) throws MOCException {
        super.writeCode(fname, cg.getDeclaration() + '\n' + code);
    }

    @Override
    public String getSuffix() {
        return "ll";
    }

    // location stuffs:
    @Override
    public void beginFunction(FunctionType fun) {
        lastTmp = 0;
        ++block;

        for (Type parameter : fun.getParameterTypes()) {
            if (parameter.isArray()) {
                lastTmp += 2;
            }
        }
    }

    @Override
    public void endFunction() {
        --block;
    }

    @Override
    public void beginBlock() {
        ++block;
    }

    @Override
    public void endBlock() {
        --block;
    }

    @Override
    public Location getLocationFor(String name, Type type) {
        return new Location('%' + name + block);
    }

    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType f, ArrayList<moc.gc.Location> params,
        String name, String block
    ) {
        Type returnType = f.getReturnType();
        boolean returnsArray = returnType.isArray();
        boolean returnsVoid = returnType.isVoid() || returnsArray;
        String returnTypeName = returnsVoid ? "void" : cg.typeName(returnType);

        int returnTmp = lastTmp;
        lastTmp = 0;
        cg.beginDefine(returnTypeName, name);

        // return type if array
        Iterator<Type> it = f.getParameterTypes().iterator();
        if (returnsArray) {
            cg.parameter(
                cg.typeName(returnType) + "* noalias sret", "%__return",
                it.hasNext()
            );
        }

        // parameter names of the form "__p0", "__p1"
        int paramIt = 0;
        while (it.hasNext()) {
            Type type = it.next();
            String typename = cg.typeName(type);

            if (type.isArray()) {
                typename += '*';
            }

            cg.parameter(typename, "%__p" + ++paramIt, it.hasNext());
        }

        cg.endDefine();

        // allocate space for return value
        if (!returnsVoid) {
            cg.alloca("%__return", returnTypeName);
        }

        // allocate space for parameters
        Iterator<moc.gc.Location> locIt = params.iterator();
        it = f.getParameterTypes().iterator();
        paramIt = 0;
        while (it.hasNext()) {
            Type paramType = it.next();
            String paramName = locIt.next().toString();
            cg.alloca(paramName, cg.typeName(paramType));
            copy(paramType, "%__p"+ ++paramIt, paramName);
        }

        if (!params.isEmpty() || !returnsVoid) {
            cg.comment("end of generated code for return value and parameters");
            cg.skipLine();
        }

        cg.body(block);

        cg.br("End");
        cg.label("End");

        if (returnsVoid) {
            cg.ret();
        }
        else {
            lastTmp = returnTmp;
            String tmp = cg.load(returnTypeName, "%__return");
            cg.ret(returnTypeName, tmp);
        }

        cg.endFunction();

        return cg.get();
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr expr) {
        Type returnType = f.getReturnType();
        String typename = cg.typeName(returnType);
        String tmp = cg.getValue(typename, expr);
        copy(returnType, tmp, "%__return");
        cg.br("End");
        cg.implicitLabel(Integer.toString(++lastTmp));
        return cg.get();
    }

    @Override
    public String genInst(moc.gc.Expr expr) {
        return expr.getLoc() == null ? "" : expr.getCode();
    }

    @Override
    public String genAsm(String code) {
        cg.comment("inline asm:");
        cg.asm(code.substring(1, code.length()-1));
        cg.skipLine();
        return cg.get();
    }

    @Override
    public String genGlobalAsm(String code) {
        cg.globalComment("inline asm:");
        cg.globalAsm(code.substring(1, code.length()-1));
        cg.skipLine();
        return "";
    }

    @Override
    public String genIf(moc.gc.Expr cond, String thenCode, String elseCode) {
        String condLabel = "%Cond." + labelCount;
        String thenLabel =  "Then." + labelCount;
        String elseLabel =  "Else." + labelCount;
        String endLabel  =  "End."  + labelCount;
        ++labelCount;

        // TODO: here we cast from i64 to i1 but the condition was probably
        //       casted from i1 to i64 just before
        String tmp = getValue("i64", cond);
        cg.cast(condLabel, "trunc", "i64", tmp, "i1");

        cg.br(condLabel, thenLabel, elseCode != null ? elseLabel : endLabel);
        cg.label(thenLabel);

        cg.append(thenCode);
        cg.br(endLabel);

        if (elseCode != null) {
            cg.label(elseLabel);
            cg.append(elseCode);
            cg.br(endLabel);
        }

        cg.label(endLabel);
        return cg.get();
    }
    @Override
    public String genElse() {
        return null; // intentional
    }
    @Override
    public String genElse(String code) {
        return code; // intentional
    }

    @Override
    public String genVarDecl(Type type, moc.gc.Location loc) {
        cg.comment("declaration of " + loc + " (" + type + ')');
        cg.alloca(loc.toString(), cg.typeName(type));
        cg.skipLine();
        return cg.get();
    }
    @Override
    public String genVarDecl(Type type, moc.gc.Location loc, moc.gc.Expr expr) {
        cg.comment("declaration of " + loc + " (" + type + ')');
        String typename = cg.typeName(type);
        cg.alloca(loc.toString(), typename);
        copy(type, getValue(typename, expr), loc.toString());
        cg.skipLine();
        return cg.get();
    }

    @Override
    public Expr genInt(String txt) {
        return new Expr(null, txt);
    }
    public Expr genInt(int nb) {
        return genInt(Integer.toString(nb));
    }
    @Override
    public Expr genString(int length, String txt) {
        String name = cg.stringCstDeclaration(length, escape(txt));
        return new Expr(new Location(name), "");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr(null, Integer.toString(escapeChar(txt)));
    }
    @Override
    public Expr genYes() {
        return new Expr(null, "0");
    }
    @Override
    public Expr genNo() {
        return new Expr(null, "1");
    }
    @Override
    public Expr genNull() {
        // first `null` means the expression is constant, it is not related to
        // we are creating a null constant
        return new Expr(null, "null");
    }
    @Override
    public Expr genNew(Type type) {
        String tmpPtr = cg.malloc(type.visit(sizeVisitor));

        // cast to right pointer type
        String tmpCastedPtr = cg.cast("bitcast", "i8*", tmpPtr, cg.typeName(type) + '*');

        return new Expr(new Location(tmpCastedPtr), cg.get());
    }
    @Override
    public String genDelete(Type t, moc.gc.Expr expr) {
        String type = cg.typeName(t);
        String tmpValue = getValue(type, expr);

        // cast to i8* (~ void*)
        String tmpPtr = cg.cast("bitcast", type, tmpValue, "i8*");

        cg.free(tmpPtr);

        return cg.get();
    }

    @Override
    public Expr genCall(
        String funName, FunctionType fun,
        ArrayList<moc.gc.Expr> exprs
    ) {
        Type returnType = fun.getReturnType();
        boolean returnsArray = returnType.isArray();
        boolean returnsVoid = returnType.isVoid() || returnsArray;
        String returnTypeName = returnsVoid ? "void" : cg.typeName(returnType);

        ArrayList<String> names = new ArrayList<String>();
        Iterator<moc.gc.Expr> exprIt = exprs.iterator();
        while (exprIt.hasNext()) {
            printCode(exprIt.next());
        }

        Iterator<Type> it = fun.getParameterTypes().iterator();
        exprIt = exprs.iterator();
        while (exprIt.hasNext()) {
            names.add(getValue(cg.typeName(it.next()), exprIt.next(), false));
        }

        String tmpValueName = null;

        if (!returnsVoid) {
            tmpValueName = cg.callNonVoid(returnTypeName, funName);
        }
        else {
            if (returnsArray) {
                returnTypeName = cg.typeName(returnType);
                tmpValueName = getTmpName();
                cg.alloca(tmpValueName, returnTypeName);
            }
            cg.callVoid(funName);
        }

        it = fun.getParameterTypes().iterator();

        if (returnsArray) {
            cg.parameter(returnTypeName + '*', tmpValueName, it.hasNext());
        }

        Iterator<String> nameIt = names.iterator();
        while (it.hasNext()) {
            Type type = it.next();
            String typename = cg.typeName(type);

            if (type.isArray()) {
                typename += '*';
            }

            cg.parameter(typename, nameIt.next(), it.hasNext());
        }

        cg.callEnd();

        return new Expr(new Location(tmpValueName), cg.get());
    }
    @Override
    public Expr genSizeOf(Type type) {
        return genInt(type.visit(sizeVisitor));
    }

    @Override
    public Expr genIdent(InfoVar info) {
        return new Expr((Location)info.getLoc(), "", !info.getType().isArray());
    }
    @Override
    public Expr genAff(Type type, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        Location loc = (Location)lhs.getLoc();
        String typename = cg.typeName(type);
        printCode(lhs);
        printCode(rhs);
        getValue(typename, lhs, false); // useless but optimized out by llc and
                                        // may have consumed some temporary names
        String rhsCode = getValue(typename, rhs, false);
        copy(type, rhsCode, loc.toString());
        return new Expr(loc, cg.get());
    }
    @Override
    public moc.gc.Expr genNonAff(Type t, moc.gc.Expr expr) {
        return expr;
    }

    @Override
    public String genUsing(String name, Type type) {
        return genComment(
            "using " + name + " = " + type + " (" + type.visit(cg.reprVisitor) + ")"
        );
    }

    @Override
    public moc.gc.Expr genDeref(Type t, moc.gc.Expr expr) {
        String type = cg.typeName(t);
        String tmp = getValue(type, expr);
        return new Expr(new Location(tmp), cg.get(), true);
    }
    @Override
    public moc.gc.Expr genArrSub(Array t, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        String type = cg.typeName(t);
        printCode(lhs);
        printCode(rhs);
        String lhsCode = getValue(type, lhs);
        String rhsCode = getValue("i64", rhs);
        String tmp = cg.getelementptr(
            type, lhsCode, new String[]{"i32", "0", "i64", rhsCode}
        );
        return new Expr(new Location(tmp), cg.get(), !t.getPointee().isArray());
    }
    @Override
    public moc.gc.Expr genPtrSub(Pointer t, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        String type = cg.typeName(t.getPointee());
        printCode(lhs);
        printCode(rhs);
        String lhsCode = getValue(type + '*', lhs);
        String rhsCode = getValue("i64", rhs);
        String tmp = cg.getelementptr(
            type, lhsCode, new String[]{"i64", rhsCode}
        );
        return new Expr(new Location(tmp), cg.get(), !t.getPointee().isArray());
    }
    @Override
    public moc.gc.Expr genCast(Type from, Type to, moc.gc.Expr expr) {
        return to.visit(from.visit(new CasterFromVisitor())).cast(cg, (Expr)expr);
    }

    @Override
    public moc.gc.Expr genIntUnaryOp(String op, moc.gc.Expr expr) {
        switch (op) {
            case "+":
                return expr;
            case "-":
                return genBinaryOpImpl("i64", "sub", new Expr(null, "0"), expr);
            case "!":
                String exprCode = getValue("i64", expr);

                // compare to 0
                String tmp = cg.binaryOperator("icmp eq", "i64", exprCode, "0");

                // cast to i64
                String tmp2 = cg.cast("zext", "i1", tmp, "i64");

                return new Expr(new Location(tmp2), cg.get());
            default:
                return null;
        }
    }

    @Override
    public Expr genIntBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genBinaryOpImpl("i64", binaryOperators.get(op), lhs, rhs);
    }
    @Override
    public Expr genCharBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genBinaryOpImpl("i8", binaryOperators.get(op), lhs, rhs);
    }
    @Override
    public Expr genPtrBinaryOp(
        String op, Type pointer,
        moc.gc.Expr lhs, moc.gc.Expr rhs
    ) {
        String typename = cg.typeName(pointer);
        return genBinaryOpImpl(typename, binaryOperators.get(op), lhs, rhs);
    }

    private Expr genBinaryOpImpl(
        String type, String op, moc.gc.Expr lhs, moc.gc.Expr rhs
    ) {
        printCode(lhs);
        printCode(rhs);
        String lhsCode = getValue(type, lhs, false);
        String rhsCode = getValue(type, rhs, false);

        String tmp = genBinaryOpImpl(type, op, lhsCode, rhsCode);
        return new Expr(new Location(tmp), cg.get());
    }

    private String genBinaryOpImpl(
        String type, String op, String lhs, String rhs
    ) {
        String tmp = cg.binaryOperator(op, type, lhs, rhs);

        if(op.equals("and") || op.equals("or")) {
            // ensure than result is 0 or 1
            tmp = genBinaryOpImpl("i64", "icmp ne", tmp, "0");
        }
        else if (op.startsWith("icmp")) {
            tmp = cg.cast("zext", "i1", tmp, "i64");
        }

        return tmp;
    }

    @Override
    public String genComment(String comment) {
        cg.comment(comment);
        return cg.get();
    }

    // class stuffs:
    @Override
    public String genClass(ClassType clazz) {
        cg.classBegin(clazz.toString());

        if (clazz.getSuper() != null) {
            String superName = cg.typeName(clazz.getSuper());
            cg.classAddMember(superName, false /* TODO */);
        }

        cg.classEnd();
        return "";
    }

    // implementation stuffs:

    protected String getTmpName() {
        return "%" + ++lastTmp;
    }

    protected String getGlobalTmpName() {
        return "@" + ++lastGlobalTmp;
    }

    /**
     * Print the code for the expression if it has one.
     * Warning: the function has side effect on cg and may increment lastTmp!
     */
    protected void printCode(moc.gc.Expr expr) {
        if (expr.getLoc() != null) {
            cg.append(expr.getCode());
        }
    }

    /** {@ getValue(type, expr, false) } */
    protected String getValue(String type, moc.gc.Expr expr) {
        return getValue(type, expr, true);
    }

    /**
     * Get the value of the expression, it may be either a constant like `42` or
     * `null`, or an unnamed temporary like `%1`; if expr is not a constant
     * and printCode is true, prepend the code used to genererate the value.
     *
     * Warning: the function has side effect on cg and may increment lastTmp!
     */
    protected String getValue(String type, moc.gc.Expr expr, boolean printCode) {
        if (expr.getLoc() != null) {
            if (printCode) {
                cg.append(expr.getCode());
            }

            if (((Expr)expr).needsLoad()) {
                return cg.load(type, expr.getLoc().toString());
            }
            else {
                return expr.getLoc().toString();
            }
        }
        else {
            return expr.getCode();
        }
    }

    private void copy(Type type, String what, String where) {
        if (type.isArray()) {
            String typename = cg.typeName(type) + '*';
            String castedWhat  = cg.cast("bitcast", typename, what,  "i8*");
            String castedWhere = cg.cast("bitcast", typename, where, "i8*");
            cg.memcpy(castedWhere, castedWhat, type.visit(sizeVisitor));
        }
        else {
            cg.store(cg.typeName(type), what, where);
        }
    }

    private String escape(String unescaped) {
        StringBuffer sb = new StringBuffer(unescaped.length());

        boolean backslash = false;
        for (int i = 1; i < unescaped.length()-1; ++i) { // exludes ""
            switch (unescaped.charAt(i)) {
                case '\\':
                    if (backslash) {
                        sb.append("\\\\");
                    }
                    backslash = !backslash;
                    break;
                case 'n':
                    sb.append(backslash ? "\\0A" : "n");
                    backslash = false;
                    break;
                case 't':
                    sb.append(backslash ? "\\09" : "t");
                    backslash = false;
                    break;
                case '"':
                    sb.append(backslash ? "\\22" : "\"");
                    backslash = false;
                    break;
                default:
                    sb.append(unescaped.charAt(i));
                    backslash = false;
            }
        }

        return sb.toString();
    }

    private int escapeChar(String unescaped) {
        if (unescaped.charAt(1) == '\\') {
            switch (unescaped.charAt(2)) {
                case '\\':
                    return '\\';
                case 'n':
                    return '\n';
                case 't':
                    return '\t';
                case '"':
                    return '\"';
                default:
                    return unescaped.charAt(2);
            }
        }
        else {
            return unescaped.charAt(1);
        }
    }
}

