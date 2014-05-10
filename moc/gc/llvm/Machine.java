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
    int bloc = -1; // the bloc we are in
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
    public void beginFunction() {
        lastTmp = 0;
        ++bloc;
    }

    @Override
    public void endFunction() {
        --bloc;
    }

    @Override
    public void beginBloc() {
        ++bloc;
    }

    @Override
    public void endBloc() {
        --bloc;
    }

    @Override
    public Location getLocationFor(String name, Type type) {
        return new Location('%' + name + bloc);
    }

    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType f, ArrayList<moc.gc.Location> params,
        String name, String bloc
    ) {
        cg.beginDefine(cg.typeName(f.getReturnType()), name);

        // parameter names of the form "__p0", "__p1"
        Iterator<Type> it = f.getParameterTypes().iterator();
        int paramIt = 0;
        while (it.hasNext()) {
            cg.parameter(cg.typeName(it.next()), "%__p" + ++paramIt, it.hasNext());
        }

        cg.endDefine();

        // allocate space for parameters
        Iterator<moc.gc.Location> locIt = params.iterator();
        it = f.getParameterTypes().iterator();
        paramIt = 0;
        while (it.hasNext()) {
            String paramType = cg.typeName(it.next());
            String paramName = locIt.next().toString();
            cg.alloca(paramName, paramType);
            cg.store(paramType, "%__p"+ ++paramIt, paramName);
        }

        cg.body(bloc);

        if (f.getReturnType() instanceof VoidType) {
            cg.ret();
        }

        cg.unreachable(); // this is kind of a hack in case the last
                          // instruction is an if-else instruction
        cg.endFunction();

        return cg.get();
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr expr) {
        String returnType = cg.typeName(f.getReturnType());
        cg.ret(returnType, getValue(returnType, expr));
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
        return cg.get();
    }

    @Override
    public String genGlobalAsm(String code) {
        cg.globalAsm(code.substring(1, code.length()-1));
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
        cg.br(condLabel, thenLabel, elseLabel);
        cg.label(thenLabel);
        cg.append(thenCode);
        cg.label(elseLabel);
        cg.append(elseCode);
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
        cg.alloca(loc.toString(), cg.typeName(type));
        return cg.get();
    }
    @Override
    public String genVarDecl(Type t, moc.gc.Location loc, moc.gc.Expr expr) {
        String type = cg.typeName(t);
        cg.alloca(loc.toString(), type);
        cg.store(type, getValue(type, expr), loc.toString());
        return cg.get();
    }

    @Override
    public Expr genNull() {
        // first `null` means the expression is constant, it is not related to
        // we are creating a null constant
        return new Expr(null, "null");
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
        String returnType = cg.typeName(fun.getReturnType());

        ArrayList<String> names = new ArrayList<String>();
        Iterator<Type> it = fun.getParameterTypes().iterator();
        Iterator<moc.gc.Expr> exprIt = exprs.iterator();
        while (exprIt.hasNext()) {
            names.add(getValue(cg.typeName(it.next()), exprIt.next()));
        }

        String tmpValueName = null;

        if (!(fun.getReturnType() instanceof VoidType)) {
            tmpValueName = cg.callNonVoid(returnType, funName);
        }
        else {
            cg.callVoid(returnType, funName);
        }

        it = fun.getParameterTypes().iterator();
        Iterator<String> nameIt = names.iterator();
        while (it.hasNext()) {
            cg.parameter(cg.typeName(it.next()), nameIt.next(), it.hasNext());
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
        String tmp = cg.load(cg.typeName(info.getType()), info.getLoc().toString());
        return new Expr(new Location(tmp), cg.get());
    }
    @Override
    public Expr genAff(Type t, moc.gc.Location loc, moc.gc.Expr rhs) {
        String type = cg.typeName(t);
        String rhsCode = getValue(type, rhs);
        cg.store(type, getTmpName(), rhsCode);
        return new Expr((Location)rhs.getLoc(), cg.get());
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
    public Expr genSubInt(moc.gc.Expr expr) {
        return genBinaryOpImpl("i64", "sub", new Expr(null, "0"), expr);
    }
    @Override
    public Expr genNotInt(moc.gc.Expr expr) {
        String exprCode = getValue("i64", expr);

        // compare to 0
        String tmp = cg.binaryOperator("icmp eq", "i64", exprCode, "0");

        // cast to i64
        String tmp2 = cg.cast("zext", "i1", tmp, "i64");

        return new Expr(new Location(tmp2), cg.get());
    }
    @Override
    public moc.gc.Expr genDeref(Type t, moc.gc.Expr expr) {
        String type = cg.typeName(((Pointer)t).getPointee());
        String exprCode = getValue(type, expr);
        String tmp = cg.load(type, exprCode);
        return new Expr(new Location(tmp), cg.get());
    }
    @Override
    public moc.gc.Expr genArrSub(Type t, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        String type = cg.typeName(t);
        String lhsCode = getValue(type, lhs);
        String rhsCode = getValue("i64", rhs);
        String tmp = cg.getelementptr(
            type, lhsCode, new String[]{"i32", "0", "i64", rhsCode}
        );
        String tmp2 = cg.load(cg.typeName(((Array)t).getPointee()), tmp);
        return new Expr(new Location(tmp2), cg.get());
    }
    @Override
    public moc.gc.Expr genCast(Type from, Type to, moc.gc.Expr expr) {
        return to.visit(from.visit(new CasterFromVisitor())).cast(cg, (Expr)expr);
    }

    @Override
    public Expr genIntBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genBinaryOpImpl("i64", binaryOperators.get(op), lhs, rhs);
    }
    @Override
    public Expr genCharBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genBinaryOpImpl("i8", binaryOperators.get(op), lhs, rhs);
    }

    private Expr genBinaryOpImpl(
        String type, String op, moc.gc.Expr lhs, moc.gc.Expr rhs
    ) {
        String lhsCode = getValue(type, lhs);
        String rhsCode = getValue(type, rhs);

        String tmp = cg.binaryOperator(op, type, lhsCode, rhsCode);

        if(op.equals("&&") || op.equals("||")) {
            // TODO:code: ensure 0 or 1
        }
        else if (op.startsWith("icmp")) {
            tmp = cg.cast("zext", "i1", tmp, "i64");
        }

        return new Expr(new Location(tmp), cg.get());
    }

    @Override
    public String genComment(String comment) {
        cg.comment(comment);
        return cg.get();
    }

    protected String getTmpName() {
        return "%" + ++lastTmp;
    }

    protected String getGlobalTmpName() {
        return "@" + ++lastGlobalTmp;
    }

    /**
     * Get the value of the expression, it may be either a constant like `42` or
     * `null`, or an unnamed temporary like `%1`; if expr is not a constant,
     * prepend the code used to genererate the value.
     *
     * Warning: the function has side effect on cg and may increment lastTmp!
     */
    protected String getValue(String type, moc.gc.Expr expr) {
        if (expr.getLoc() != null) {
            cg.append(expr.getCode());
            return expr.getLoc().toString();
        }
        else {
            return expr.getCode();
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

    protected void indent(StringBuilder sb) {
        if (bloc >= 0) {
            sb.append("    ");
        }
    }
}

