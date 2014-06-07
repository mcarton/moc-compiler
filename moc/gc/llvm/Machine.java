package moc.gc.llvm;

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
    int block = 0; // the block we are in
    int labelCount = 0;
    Map<String, String> binaryOperators = new HashMap<String, String>();

    SizeVisitor sizeVisitor = new SizeVisitor();
    CodeGenerator cg = new CodeGenerator(this);
    FunctionCodeGenerator fcg = new FunctionCodeGenerator(this);

    String currentClassName;

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
        block = 0;
    }

    @Override
    public void beginMethod(Method method) {
        lastTmp = 0;
        ++block;
        currentClassName = cg.typeName(method.getClassType());
    }

    @Override
    public void endMethod() {
        block = 0;
        currentClassName = null;
    }

    @Override
    public void beginClass(ClassType type) {}
    @Override
    public void endClass() {}

    @Override
    public void beginBlock() {
        ++block;
    }

    @Override
    public void endBlock() {
    }

    @Override
    public Location getLocationForParameter(Type type, String name) {
        return getLocationFor(type, name);
    }
    @Override
    public Location getLocationForVariable(Type type, String name) {
        return getLocationFor(type, name);
    }
    public Location getLocationFor(Type type, String name) {
        return new Location('%' + name + '.' + block);
    }
    @Override
    public Location getLocationForAttribute(
        ClassType clazz, Type type, String name
    ) {
        return new Location(name, clazz, clazz.getAttributes().size()+1);
    }

    // code generation stuffs:
    /**
     * Generate code for a function definition.
     * - When the return type is an array, it is passed as a pointer
     *   allocated by the callee.
     * - When a parameter is an array, it is passed as a pointer and must
     *   be copied in the function.
     */
    @Override
    public String genFunction(
        FunctionType f, ArrayList<ILocation> params,
        String name, String block
    ) {
        return fcg.genFunction(f, params, name, block);
    }

    @Override
    public String genMethod(
        Method method, ArrayList<ILocation> parameters, String block
    ) {
        return fcg.genMethod(method, parameters, block);
    }

    /**
     * Code for a return expression. It does not actually return but jumps to
     * the end of the function where it returns.
     */
    @Override
    public String genReturn(Type returnType, IExpr expr) {
        String typename = cg.typeName(returnType);
        String tmp = cg.getValue(typename, expr);
        copy(returnType, tmp, "%__return");
        cg.br("End");
        cg.implicitLabel(Integer.toString(++lastTmp));
        return cg.get();
    }

    @Override
    public String genInst(Type type, IExpr expr) {
        // if expr.getLoc() is null, the expression is an llvm constant
        return ((Expr)expr).getLoc() == null ? "" : expr.getCode();
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
    public String genIf(IExpr cond, String thenCode, String elseCode) {
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
    public String genVarDecl(Type type, ILocation loc) {
        cg.comment("declaration of " + loc + " (" + type + ')');
        cg.alloca(loc.toString(), cg.typeName(type));
        cg.skipLine();
        return cg.get();
    }
    @Override
    public String genVarDecl(Type type, ILocation loc, IExpr expr) {
        cg.comment("declaration of " + loc + " (" + type + ')');
        String typename = cg.typeName(type);
        cg.alloca(loc.toString(), typename);
        copy(type, getValue(typename, expr), loc.toString());
        cg.skipLine();
        return cg.get();
    }

    @Override
    public String genWhile(IExpr cond, String block) {
        String whileLabel = "While." + labelCount;
        String condLabel  = "%Cond." + labelCount;
        String thenLabel  =  "Then." + labelCount;
        String endLabel   =  "End."  + labelCount;
        ++labelCount;

        cg.br(whileLabel);
        cg.label(whileLabel);

        // TODO: here we cast from i64 to i1 but the condition was probably
        //       casted from i1 to i64 just before
        String tmp = getValue("i64", cond);
        cg.cast(condLabel, "trunc", "i64", tmp, "i1");

        cg.br(condLabel, thenLabel, endLabel);
        cg.label(thenLabel);
        cg.append(block);

        cg.br(whileLabel);
        cg.label(endLabel);

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
        String name = cg.stringCstDeclaration(length, StringEscapor.escape(txt));
        return new Expr(new Location(name), "");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr(null, Integer.toString(StringEscapor.escapeChar(txt)));
    }
    @Override
    public Expr genNull() {
        // first `null` means the expression is constant, it is not related to
        // we are creating a null constant
        return new Expr(null, "null");
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
    public Expr genSelf(Type type) {
        return new Expr(new Location("%self"), "");
    }
    @Override
    public Expr genSuper(Type type) {
        return new Expr(new Location("%super"), "");
    }

    @Override
    public Expr genNew(Type type) {
        String tmpPtr = cg.malloc(Integer.toString(type.visit(sizeVisitor)));
        return genNewImpl(tmpPtr, type);
    }
    @Override
    public Expr genNewArray(IExpr nbElements, Type type) {
        String tmpSize = Integer.toString(type.visit(sizeVisitor));
        String tmpNbElements = getValue("i64", nbElements);
        String size = genBinaryOpImpl("i64", "mul", tmpNbElements, tmpSize);
        String tmpPtr = cg.malloc(size);
        return genNewImpl(tmpPtr, type);
    }
    private Expr genNewImpl(String allocated, Type type) {
        // cast to right pointer type
        String typeName = cg.typeName(type);
        String typeNamePtr = typeName + '*';
        String tmpCastedPtr = cg.cast("bitcast", "i8*", allocated, typeNamePtr);

        // note: we cannot create an array of class, so that is ok here
        if (type.isClass()) {
            ClassType clazz = (ClassType)type;
            String name = clazz.getName();
            String vtable = fcg.getVtable(clazz, typeName, tmpCastedPtr);
            String tmp = cg.load("%mocc.method**", "@vtablePtr." + name);
            cg.store("%mocc.method**", tmp, vtable);
        }

        return new Expr(new Location(tmpCastedPtr), cg.get());
    }
    @Override
    public String genDelete(Type t, IExpr expr) {
        String type = cg.typeName(t);
        String tmpValue = getValue(type, expr);

        // cast to i8* (~ void*)
        String tmpPtr = cg.cast("bitcast", type, tmpValue, "i8*");

        cg.free(tmpPtr);

        return cg.get();
    }

    @Override
    public Expr genCall(
        String funName, FunctionType fun, ArrayList<IExpr> exprs
    ) {
        return fcg.genCall(funName, fun, exprs);
    }

    @Override
    public IExpr genCall(
        Method method, Pointer type, IExpr instance, ArrayList<IExpr> params
    ) {
        return fcg.genCall(method, type, instance, params);
    }

    @Override
    public IExpr genCall(Method method, ArrayList<IExpr> params) {
        return fcg.genCall(method, params);
    }

    @Override
    public Expr genSizeOf(Type type) {
        return genInt(type.visit(sizeVisitor));
    }

    @Override
    public Expr genIdent(InfoVar info) {
        Location location = (Location)info.getLoc();
        if (location.isMember()) {
            String className = cg.typeName(location.getClassType());
            String casted = cg.cast(
                "bitcast", currentClassName + '*', "%self", className + '*'
            );
            String tmp = cg.getelementptr(
                className, casted,
                new String[]{
                    "i64", "0", // there are no type errors here
                    "i32", Integer.toString(location.getMemberNumber())
                }
            );
            return new Expr(new Location(tmp), cg.get(), !info.getType().isArray());
        }
        else {
            return new Expr(location, "", !info.getType().isArray());
        }
    }
    @Override
    public Expr genAff(Type type, IExpr lhs, IExpr rhs) {
        Location loc = ((Expr)lhs).getLoc();
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
    public IExpr genNonAff(Type t, IExpr expr) {
        return expr;
    }

    @Override
    public String genUsing(String name, Type type) {
        return genComment(
            "using " + name + " = " + type + " (" + type.visit(cg.reprVisitor) + ")"
        );
    }

    @Override
    public Expr genDeref(Type t, IExpr expr) {
        String type = cg.typeName(t);
        String tmp = getValue(type, expr);
        return new Expr(new Location(tmp), cg.get(), true);
    }
    @Override
    public IExpr genArrSub(Array t, IExpr lhs, IExpr rhs) {
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
    public IExpr genPtrSub(Pointer t, IExpr lhs, IExpr rhs) {
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
    public IExpr genCast(Type from, Type to, IExpr expr) {
        return to.visit(from.visit(new CasterFromVisitor())).cast(cg, (Expr)expr);
    }

    @Override
    public IExpr genIntUnaryOp(String op, IExpr expr) {
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
    public Expr genIntBinaryOp(String op, IExpr lhs, IExpr rhs) {
        return genBinaryOpImpl("i64", binaryOperators.get(op), lhs, rhs);
    }
    @Override
    public Expr genCharBinaryOp(String op, IExpr lhs, IExpr rhs) {
        return genBinaryOpImpl("i8", binaryOperators.get(op), lhs, rhs);
    }
    @Override
    public Expr genPtrBinaryOp(
        String op, Type pointer,
        IExpr lhs, IExpr rhs
    ) {
        String typename = cg.typeName(pointer);
        return genBinaryOpImpl(typename, binaryOperators.get(op), lhs, rhs);
    }

    private Expr genBinaryOpImpl(
        String type, String op, IExpr lhs, IExpr rhs
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
    public String genClass(ClassType clazz, String methods) {
        cg.classBegin(cg.typeName(clazz));

        Iterator<Attributes> it = clazz.getAttributes().iterator();

        // add ptr to parent or vtable
        if (clazz.getSuper() != null) {
            String superName = cg.typeName(clazz.getSuper());
            cg.classAddMember(superName, it.hasNext());
        }
        else {
            cg.classAddMember("%mocc.vtable", it.hasNext());
        }

        while (it.hasNext()) {
            String typename = cg.typeName(it.next().type);
            cg.classAddMember(typename, it.hasNext());
        }

        cg.classEnd();

        cg.append(methods);

        fcg.genVirtualTable(clazz.getName(), clazz.getMethods());

        return cg.get();
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
    protected void printCode(IExpr expr) {
        if (((Expr)expr).getLoc() != null) {
            cg.append(expr.getCode());
        }
    }

    /** {@code getValue(type, expr, false) } */
    protected String getValue(String type, IExpr expr) {
        return getValue(type, expr, true);
    }

    /**
     * Get the value of the expression, it may be either a constant like `42` or
     * `null`, or an unnamed temporary like `%1`; if expr is not a constant
     * and printCode is true, prepend the code used to genererate the value.
     *
     * Warning: the function has side effect on cg and may increment lastTmp!
     */
    protected String getValue(String type, IExpr iexpr, boolean printCode) {
        Expr expr = (Expr)iexpr;
        if (expr.getLoc() != null) {
            if (printCode) {
                cg.append(expr.getCode());
            }

            if (expr.needsLoad()) {
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

    /** Copy a variable using `malloc` for arrays. */
    void copy(Type type, String what, String where) {
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

}

