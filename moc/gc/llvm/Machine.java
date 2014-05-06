package moc.gc.llvm;

import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Iterator;
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

    SizeVisitor sizeVisitor = new SizeVisitor();

    CodeGenerator cg = new CodeGenerator(this);

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
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
        StringBuilder sb = new StringBuilder(name.length() + bloc.length());

        sb.append("define ");
        sb.append(cg.typeName(f.getReturnType()));
        sb.append(" @");
        sb.append(name);
        sb.append('(');

        // parameter names of the form "__p0", "__p1"
        Iterator<Type> it = f.getParameterTypes().iterator();
        int paramIt = 0;
        while (it.hasNext()) {
            sb.append(cg.typeName(it.next()));
            sb.append(" %__p");
            sb.append(++paramIt);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(") nounwind {\n"); // nounwind = no exceptions

        // allocate space for parameters
        // %1 = alloca i32, align 4
        // store i32 %__p1, i32* %1, align 4
        Iterator<moc.gc.Location> locIt = params.iterator();
        it = f.getParameterTypes().iterator();
        paramIt = 0;
        while (it.hasNext()) {
            String paramType = cg.typeName(it.next());
            String paramName = locIt.next().toString();
            indent(sb);
            sb.append(paramName);
            sb.append(" = alloca ");
            sb.append(paramType);
            sb.append('\n');
            indent(sb);
            sb.append("store ");
            sb.append(paramType);
            sb.append(" %__p");
            sb.append(++paramIt);
            sb.append(", ");
            sb.append(paramType);
            sb.append("* ");
            sb.append(paramName);
            sb.append('\n');
        }

        sb.append(bloc);

        if (f.getReturnType() instanceof VoidType) {
            indent(sb);
            sb.append("ret void\n");
        }

        sb.append("}\n\n");

        return sb.toString();
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr expr) {
        String returnType = cg.typeName(f.getReturnType());
        cg.ret(returnType, cg.getValue(returnType, expr));
        return cg.get();
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
        cg.store(type, cg.getValue(type, expr), loc.toString());
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
        String tmpCastedPtr = cg.cast("bitcast", "i8*", tmpPtr, cg.typeName(type) + "*");

        return new Expr(new Location(tmpCastedPtr), cg.get());
    }
    @Override
    public String genDelete(Type t, moc.gc.Expr expr) {
        String type = cg.typeName(t);
        String tmpValue = cg.getValue(type, expr);

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
        StringBuilder sb = new StringBuilder();
        String returnType = cg.typeName(fun.getReturnType());

        ArrayList<String> names = new ArrayList<String>();
        Iterator<Type> it = fun.getParameterTypes().iterator();
        Iterator<moc.gc.Expr> exprIt = exprs.iterator();
        while (exprIt.hasNext()) {
            names.add(getValue(cg.typeName(it.next()), exprIt.next(), sb));
        }

        // %retval = call i32 @funName(parameters)
        String tmpValueName = null;
        indent(sb);

        if (!(fun.getReturnType() instanceof VoidType)) {
            tmpValueName = getTmpName();
            sb.append(tmpValueName);
            sb.append(" = call ");
        }
        else {
            sb.append("call ");
        }
        sb.append(returnType);
        sb.append(" @");
        sb.append(funName);
        sb.append('(');

        it = fun.getParameterTypes().iterator();
        Iterator<String> nameIt = names.iterator();
        while (it.hasNext()) {
            sb.append(cg.typeName(it.next()));
            sb.append(' ');
            sb.append(nameIt.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(")\n");

        return new Expr(new Location(tmpValueName), sb.toString());
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
        String rhsCode = cg.getValue(type, rhs);
        cg.store(type, getTmpName(), rhsCode);
        return new Expr((Location)rhs.getLoc(), cg.get());
    }
    @Override
    public moc.gc.Expr genNonAff(Type t, moc.gc.Expr expr) {
        return expr;
    }

    @Override
    public Expr genSubInt(moc.gc.Expr expr) {
        return genSubInt(new Expr(null, "0"), expr);
    }
    @Override
    public Expr genNotInt(moc.gc.Expr expr) {
        String exprCode = cg.getValue("i64", expr);

        // compare to 0
        String tmp = cg.binaryOperator("icmp eq", "i64", exprCode, "0");

        // cast to i64
        String tmp2 = cg.cast("zext", "i1", tmp, "i64");

        return new Expr(new Location(tmp2), cg.get());
    }
    @Override
    public moc.gc.Expr genDeref(Type t, moc.gc.Expr expr) {
        String type = cg.typeName(t);
        String exprCode = cg.getValue(type, expr);
        String tmp = cg.load(type, exprCode);
        return new Expr(new Location(tmp), cg.get());
    }
    @Override
    public moc.gc.Expr genArrSub(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public moc.gc.Expr genCast(Type from, Type to, moc.gc.Expr expr) {
        return to.visit(from.visit(new CasterFromVisitor())).cast(cg, (Expr)expr);
    }

    @Override
    public Expr genAddInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genIntBinaryOp("add", lhs, rhs);
    }
    @Override
    public Expr genSubInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genIntBinaryOp("sub", lhs, rhs);
    }
    @Override
    public Expr genOrInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO: ensure 0 or 1
        return genIntBinaryOp("or", lhs, rhs);
    }
    @Override
    public Expr genMultInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genIntBinaryOp("mul", lhs, rhs);
    }
    @Override
    public Expr genDivInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genIntBinaryOp("sdiv", lhs, rhs);
    }
    @Override
    public Expr genModInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genIntBinaryOp("srem", lhs, rhs);
    }
    @Override
    public Expr genAndInt(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO: ensure 0 or 1
        return genIntBinaryOp("and", lhs, rhs);
    }
    private Expr genIntBinaryOp(String what, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        String type = "i64";
        String lhsCode = cg.getValue(type, lhs);
        String rhsCode = cg.getValue(type, rhs);

        String tmp = cg.binaryOperator(what, "i64", lhsCode, rhsCode);

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
     * Warning: the function has side effect on sb and may increment lastTmp!
     */
    protected String getValue(String type, moc.gc.Expr expr, StringBuilder sb) {
        if (expr.getLoc() != null) {
            sb.append(expr.getCode());
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

