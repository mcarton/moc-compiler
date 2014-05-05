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
 * The TAM machine and its generation functions
 */
public class Machine extends AbstractMachine {
    int lastGlobalTmp = -1;
    int lastTmp = 0; // name of the last generated temporary
    int bloc = -1; // the bloc we are in
    String declarations =
              "declare i8* @malloc(i64)\n"
            + "declare void @free(i8*)\n";

    RepresentationVisitor typeVisitor = new RepresentationVisitor();
    SizeVisitor sizeVisitor = new SizeVisitor();

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
    }

    @Override
    public void writeCode(String fname, String code) throws MOCException {
        super.writeCode(fname, declarations + '\n' + code);
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
        sb.append(f.getReturnType().visit(typeVisitor));
        sb.append(" @");
        sb.append(name);
        sb.append('(');

        // parameter names of the form "__p0", "__p1"
        Iterator<Type> it = f.getParameterTypes().iterator();
        int paramIt = 0;
        while (it.hasNext()) {
            sb.append(it.next().visit(typeVisitor));
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
            String paramType = it.next().visit(typeVisitor);
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
    public String genReturn(FunctionType f, moc.gc.Expr gcexpr) {
        Expr expr = (Expr)gcexpr;

        StringBuilder sb = new StringBuilder();

        String returnTypeRepr = f.getReturnType().visit(typeVisitor);
        String returnValue = getValue(returnTypeRepr, expr, sb);

        indent(sb);
        sb.append("ret ");
        sb.append(returnTypeRepr);
        sb.append(' ');
        sb.append(returnValue);
        sb.append('\n');

        return sb.toString();
    }

    @Override
    public String genVarDecl(Type t, moc.gc.Location loc) {
        StringBuilder sb = new StringBuilder(20);

        String type = t.visit(typeVisitor);

        indent(sb);
        sb.append(loc);
        sb.append(" = alloca ");
        sb.append(type);
        sb.append('\n');

        return sb.toString();
    }
    @Override
    public String genVarDecl(Type t, moc.gc.Location loc, moc.gc.Expr expr) {
        StringBuilder sb = new StringBuilder(50);

        String type = t.visit(typeVisitor);
        String exprCode = getValue(type, expr, sb);

        indent(sb);
        sb.append(loc);
        sb.append(" = alloca ");
        sb.append(type);
        sb.append('\n');

        indent(sb);
        sb.append("store ");
        sb.append(type);
        sb.append(' ');
        sb.append(exprCode);
        sb.append(", ");
        sb.append(type);
        sb.append("* ");
        sb.append(loc);
        sb.append('\n');

        return sb.toString();
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
        String escaped = escape(txt);
        StringBuilder sb = new StringBuilder(declarations);

        String name = getGlobalTmpName();

        // <name> = internal constant [4 x i8] c"foo\00", align 1
        sb.append(name);
        sb.append(" = internal constant [");
        sb.append(length);
        sb.append(" x i8] c\"");
        sb.append(escaped);
        sb.append("\\00\"\n");

        declarations = sb.toString();

        return new Expr(new Location(name), "");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr(null, Integer.toString(escapeChar(txt)));
    }
    @Override
    public Expr genNew(Type t) {
        StringBuilder sb = new StringBuilder(50);

        String type = t.visit(typeVisitor);

        // call malloc
        // <result1> = call i8* @malloc(i64 4)
        String tmpPtr = getTmpName();
        indent(sb);
        sb.append(tmpPtr);
        sb.append(" = call i8* @malloc(i64 ");
        sb.append(t.visit(sizeVisitor));
        sb.append(")\n");

        // cast to right pointer type
        // <result2> = bitcast i8* <result1> to i32*
        String tmpCastedPtr = getTmpName();
        indent(sb);
        sb.append(tmpCastedPtr);
        sb.append(" = bitcast i8* ");
        sb.append(tmpPtr);
        sb.append(" to ");
        sb.append(type);
        sb.append("*\n");

        return new Expr(new Location(tmpCastedPtr), sb.toString());
    }
    @Override
    public String genDelete(Type t, moc.gc.Expr expr) {
        StringBuilder sb = new StringBuilder(50);
        String type = t.visit(typeVisitor);

        String tmpValue = getValue(type, expr, sb);

        // cast to i8* (~ void*)
        // <result> = bitcast i32* <ptr> to i8*
        String tmpPtr = getTmpName();
        indent(sb);
        sb.append(tmpPtr);
        sb.append(" = bitcast ");
        sb.append(type);
        sb.append(' ');
        sb.append(tmpValue);
        sb.append(" to i8*\n");

        // call void @free(i8* <result>) #2
        indent(sb);
        sb.append("call void @free(i8* ");
        sb.append(tmpPtr);
        sb.append(")\n");

        return sb.toString();
    }

    @Override
    public Expr genCall(
        String funName, FunctionType fun,
        ArrayList<moc.gc.Expr> exprs
    ) {
        StringBuilder sb = new StringBuilder();
        String returnType = fun.getReturnType().visit(typeVisitor);

        ArrayList<String> names = new ArrayList<String>();
        Iterator<Type> it = fun.getParameterTypes().iterator();
        Iterator<moc.gc.Expr> exprIt = exprs.iterator();
        while (exprIt.hasNext()) {
            names.add(getValue(it.next().visit(typeVisitor), exprIt.next(), sb));
        }

        // %retval = call i32 @funName(parameters)
        String tmpValueName = getTmpName();
        indent(sb);

        if (!(fun.getReturnType() instanceof VoidType)) {
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
            sb.append(it.next().visit(typeVisitor));
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
        StringBuilder sb = new StringBuilder();

        String tmpValueName = getTmpName();
        indent(sb);
        sb.append(tmpValueName);
        sb.append(" = load ");
        sb.append(info.getType().visit(typeVisitor));
        sb.append("* ");
        sb.append(info.getLoc());
        sb.append('\n');

        return new Expr(new Location(tmpValueName), sb.toString());
    }
    @Override
    public Expr genAff(Type t, moc.gc.Location loc, moc.gc.Expr gcrhs) {
        Expr rhs = (Expr)gcrhs;

        StringBuilder sb = new StringBuilder(50);

        String type = t.visit(typeVisitor);
        String rhsCode= getValue(type, rhs, sb);
        String tmpValueName = getTmpName();

        indent(sb);
        sb.append("store ");
        sb.append(type);
        sb.append(tmpValueName);
        sb.append(", ");
        sb.append(type);
        sb.append("* ");
        sb.append(rhsCode);
        sb.append('\n');

        return new Expr(rhs.getLoc(), sb.toString());
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
        StringBuilder sb = new StringBuilder(50);

        String exprCode = getValue("i64", expr, sb);

        // <tmpValueName> = icmp eq i64 <exprCode>, 0
        String tmpValueName = getTmpName();
        indent(sb);
        sb.append(tmpValueName);
        sb.append(" = icmp eq i64 ");
        sb.append(exprCode);
        sb.append(", 0\n");

        // <tmpCastedName> = zext i1 <tmpValueName> to i64
        String tmpCastedName = getTmpName();
        indent(sb);
        sb.append(tmpCastedName);
        sb.append(" = zext i1 ");
        sb.append(tmpValueName);
        sb.append(" to i64");

        return new Expr(new Location(tmpCastedName), sb.toString());
    }
    @Override
    public moc.gc.Expr genDeref(moc.gc.Expr expr) {
        return expr; // TODO:code
    }
    @Override
    public moc.gc.Expr genArrSub(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public moc.gc.Expr genCast(Type from, Type to, moc.gc.Expr expr) {
        return expr; // TODO:code
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
        StringBuilder sb = new StringBuilder();

        String type = "i64";
        String lhsCode = getValue(type, lhs, sb);
        String rhsCode = getValue(type, rhs, sb);
        String tmpValueName = getTmpName();

        // <result> = add <ty> <op1>, <op2>
        indent(sb);
        sb.append(tmpValueName);
        sb.append(" = ");
        sb.append(what);
        sb.append(' ');
        sb.append(type);
        sb.append(' ');
        sb.append(lhsCode);
        sb.append(", ");
        sb.append(rhsCode);
        sb.append('\n');

        return new Expr(new Location(tmpValueName), sb.toString());
    }

    @Override
    public String genComment(String comment) {
        StringBuilder sb = new StringBuilder(comment.length());
        indent(sb);
        sb.append("; ");
        sb.append(comment);
        sb.append('\n');
        return sb.toString();
    }

    private String getTmpName() {
        return "%" + ++lastTmp;
    }

    private String getGlobalTmpName() {
        return "@" + ++lastGlobalTmp;
    }

    /**
     * Get the value of the expression, it may be either a constant like `42` or
     * `null`, or an unnamed temporary like `%1`; if expr is not a constant,
     * prepend the code used to genererate the value.
     *
     * @warning: the function has side effect on sb and may increment lastTmp!
     */
    private String getValue(String type, moc.gc.Expr expr, StringBuilder sb) {
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

    private void indent(StringBuilder sb) {
        if (bloc >= 0) {
            sb.append("    ");
        }
    }
}

/** A visitor to get the llvm representation of types.
 */
class RepresentationVisitor implements TypeVisitor<String> {
    public String visit(IntegerType what)   { return "i64"; }
    public String visit(CharacterType what) { return "i8"; }

    public String visit(VoidType what)      { return "void"; }
    public String visit(NullType what)      { return "i8*"; }

    public String visit(Array what) {
        return "["
            + what.getNbElements()
            + " x "
            + what.getPointee().visit(this)
            + "]";
    }
    public String visit(Pointer what) {
        return what.getPointee().visit(this) + "*";
    }
}

/** A visitor to get the size of types.
 */
class SizeVisitor implements TypeVisitor<Integer> {
    public Integer visit(IntegerType what)   { return 8; }
    public Integer visit(CharacterType what) { return 1; }

    public Integer visit(VoidType what)      { return 0; }
    public Integer visit(NullType what)      { return 8; }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }
    public Integer visit(Pointer what)       { return 8; }
}

