package moc.gc.tam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import moc.compiler.MOCException;
import moc.gc.*;
import moc.symbols.*;
import moc.type.*;

/**
 * The TAM machine and its generation functions.
 */
public class Machine extends AbstractMachine {
    final SizeVisitor sizeVisitor = new SizeVisitor();

    int currentParameterAddress;
    final int initialOffset = 3;
        // there is an initial offset cause be the call to main
        // 0 -> 0
        // 1 -> LB previous function
        // 2 -> return address
    int currentAddress = initialOffset;
    final Stack<Integer> addressStack = new Stack<>();
    final CodeGenerator cg = new CodeGenerator();

    int labelCount = 0;
    final Map<String, String> binaryOperators = new HashMap<>();

    private int parametersSize;
    private int currentSelfOffset = 1 /* initial offset of 1 for vtable */;

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);

        binaryOperators.put("+" , "IAdd");
        binaryOperators.put("-" , "ISub");
        binaryOperators.put("*" , "IMul");
        binaryOperators.put("/" , "IDiv");
        binaryOperators.put("%" , "IMod");
        binaryOperators.put("==", "IEq" );
        binaryOperators.put("!=", "INeq");
        binaryOperators.put("<" , "ILss");
        binaryOperators.put(">" , "IGtr");
        binaryOperators.put("<=", "ILeq");
        binaryOperators.put(">=", "IGeq");
        binaryOperators.put("&&", "BAnd");
        binaryOperators.put("||", "BOr" );
        // TODO: integers may require to be converted to 0 or 1 with BAnd/BOr
    }

    @Override
    public void writeCode(String fname, String code) throws MOCException {
        super.writeCode(fname, cg.getDeclaration() + '\n' + code);
    }

    @Override
    public String getSuffix() {
        return "tam";
    }

    // location stuffs:
    @Override
    public void beginFunction(FunctionType fun) {
        currentParameterAddress = 0;
        parametersSize = paramSize(fun.getParameterTypes());
    }

    @Override
    public void endFunction() {
    }

    @Override
    public void beginMethod(Method meth) {
        // TODO
    }

    @Override
    public void endMethod() {
        // TODO
    }

    @Override
    public void beginClass(ClassType type) {
        currentSelfOffset = (type.hasSuper() ? type.visit(sizeVisitor) : 1);
    }

    @Override
    public void endClass() {
    }

    @Override
    public void beginBlock() {
        addressStack.push(currentAddress);
    }

    @Override
    public void endBlock() {
        currentAddress = addressStack.pop();
    }

    @Override
    public Location getLocationForParameter(Type type, String name) {
        currentParameterAddress -= type.visit(sizeVisitor);
        Location tempLoc = new Location(currentParameterAddress, "LB");
        return tempLoc;
    }
    @Override
    public Location getLocationForVariable(Type type, String name) {
        Location tempLoc = new Location(currentAddress, "LB");
        currentAddress += type.visit(sizeVisitor);
        return tempLoc;
    }
    @Override
    public Location getLocationForAttribute(
        ClassType clazz, Type type, String name
    ) {
        Location tempLoc = new Location(currentSelfOffset);
        currentSelfOffset += type.visit(sizeVisitor);
        return tempLoc;
    }

    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType fun, ArrayList<ILocation> parameters,
        String name, String block
    ) {
        cg.function(name);
        return genFunctionImpl(
            block, fun.getReturnType(), fun.getParameterTypes()
        );
    }

    @Override
    public String genMethod(
        Method method, ArrayList<ILocation> parameters, String block
    ) {
        cg.method(name(method));
        return genFunctionImpl(
            block, method.getReturnType(), method.getParameterTypes()
        );
    }

    private String genFunctionImpl(
        String block, Type returnType, Iterable<Type> parameters
    ) {
        cg.append(block);

        if (returnType.isVoid()) {
            cg.ret(0, paramSize(parameters));
        }

        return cg.get();
    }

    static private String name(Method method) {
        StringBuilder sb = new StringBuilder(method.getClassType().toString());

        for (Selector selector : method.getSelectors()) {
            sb.append("__" + selector.getName());
        }

        return sb.toString();
    }

    private int paramSize(Iterable<Type> types) {
        int paramSize = 0;
        for (Type t : types) {
            paramSize += t.visit(sizeVisitor);
        }
        return paramSize;
    }

    @Override
    public String genReturn(Type returnType, IExpr expr) {
        int returnSize = returnType.visit(sizeVisitor);
        cg.append(expr.getCode());
        getValue(expr, returnSize);
        cg.ret(returnSize, parametersSize);
        return cg.get();
    }
    @Override
    public String genBlock(String code) {
        cg.append(code);
        cg.pop(0, currentAddress - addressStack.peek());
        return cg.get();
    }

    @Override
    public String genInst(Type type, IExpr expr) {
        int size = type.visit(sizeVisitor);
        cg.append(expr.getCode());
        getValue(expr, size);
        cg.pop(0, size);
        return cg.get();
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
        String thenLabel =  "then_" + labelCount;
        String elseLabel =  "else_" + labelCount;
        String endLabel  =  "endif_"  + labelCount;
        ++labelCount;

        cg.append(cond.getCode());
        getValue(cond, 1);
        cg.jumpif(0, elseCode != null ? elseLabel : endLabel);
        cg.label(thenLabel);
        cg.append(thenCode);

        if (elseCode != null) {
            cg.jump(endLabel);
            cg.label(elseLabel);
            cg.append(elseCode);
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
    public String genWhile(IExpr cond, String block) {
        String whileLabel =  "while_"  + labelCount;
        String thenLabel  =  "then_"   + labelCount;
        String endLabel   =  "endif_"  + labelCount;
        ++labelCount;

        cg.label(whileLabel);
        cg.append(cond.getCode());
        getValue(cond, 1);
        cg.jumpif(0, endLabel);
        cg.label(thenLabel);
        cg.append(block);
        cg.jump(whileLabel);

        cg.label(endLabel);

        return cg.get();
    }

    @Override
    public String genVarDecl(Type t, ILocation loc) {
        cg.comment("declaration of " + loc + " (" + t + ')');
        cg.push(t.visit(sizeVisitor));
        return cg.get();
    }
    @Override
    public String genVarDecl(Type t, ILocation loc, IExpr expr) {
        cg.comment("declaration of " + loc + " (" + t + ')');
        cg.append(expr.getCode());
        getValue(expr, t.visit(sizeVisitor));
        return cg.get();
    }

    @Override
    public Expr genInt(String txt) {
        cg.loadl(txt);
        return new Expr(cg.get());
    }
    public Expr genInt(int nb) {
        return genInt(Integer.toString(nb));
    }
    @Override
    public Expr genString(int length, String txt) {
        boolean backslash = false;
        int size = 0;
        for (int i = 1; i < txt.length()-1; ++i) { // exludes ""
            switch (txt.charAt(i)) {
                case '\\':
                    if (backslash) {
                        cg.declLoadl("'\\\\'");
                        ++size;
                    }
                    backslash = !backslash;
                    break;
                case '0':
                    cg.declLoadl(backslash ? "'\\0'" : "'0'");
                    ++size;
                    backslash = false;
                    break;
                case 'n':
                    cg.declLoadl(backslash ? "'\\n'" : "'n'");
                    ++size;
                    backslash = false;
                    break;
                case 't':
                    cg.declLoadl(backslash ? "'\\t'" : "'t'");
                    ++size;
                    backslash = false;
                    break;
                case '"':
                    cg.declLoadl(backslash ? "'\\\"'" : "'\"'");
                    ++size;
                    backslash = false;
                    break;
                default:
                    cg.declLoadl("'" + txt.charAt(i) + "'");
                    ++size;
                    backslash = false;
            }
        }
        cg.declLoadl(0);
        cg.loada(currentAddress-initialOffset + "[CB]");
        return new Expr(cg.get(), true);
    }
    @Override
    public Expr genCharacter(String txt) {
        if (txt.charAt(1) == '\\') {
            switch (txt.charAt(2)) {
                case '\\': cg.loadl("'\\'"); break;
                case '0' : cg.loadl(0);      break;
                case 'n' : cg.loadl("'\n'"); break;
                case 't' : cg.loadl("'\t'"); break;
                case '"' : cg.loadl("'\"'"); break;
                default  : cg.loadl("'" + txt.charAt(2) + "'");
            }
        }
        else {
            cg.loadl("'" + txt.charAt(1) + "'");
        }
        return new Expr(cg.get());
    }
    @Override
    public Expr genNull() {
        cg.subr("MVoid");
        return new Expr(cg.get());
    }
    @Override
    public Expr genYes() {
        cg.loadl(1);
        cg.subr("I2B");
        return new Expr(cg.get());
    }
    @Override
    public Expr genNo() {
        cg.loadl(0);
        cg.subr("I2B");
        return new Expr(cg.get());
    }
    @Override
    public Expr genSelf(Type type) {
        // self and super are the first parameter
        cg.loada(selfLocation());
        return new Expr(cg.get());
    }
    @Override
    public Expr genSuper(Type type) {
        return genSelf(type);
    }

    private String selfLocation() {
        return (currentParameterAddress-1) + "[LB]";
    }

    @Override
    public Expr genNew(Type type) {
        cg.loadl(type.visit(sizeVisitor));
        cg.subr("Malloc");

        if (type.isClass()) { // initialize vtable
            ClassType clazz = (ClassType)type;
            cg.loada("vtable_" + clazz.getName());
            cg.loada("-2 [ST]");
            cg.loadi(1);
            cg.storei(1);
        }

        return new Expr(cg.get());
    }
    @Override
    public Expr genNewArray(IExpr nbElements, Type t) {
        cg.loadl(t.visit(sizeVisitor));
        cg.append(nbElements.getCode());
        getValue(nbElements, 1);
        cg.subr("IMul");
        cg.subr("Malloc");
        return new Expr(cg.get());
    }

    @Override
    public String genDelete(Type t, IExpr expr) {
        cg.append(expr.getCode());
        getValue(expr, t.visit(sizeVisitor));
        /* NOTE: Mfree does not seem to be implemented on the tam compiler
         *       and/or interpreter. The following instruction should be:
        cg.subr("Mfree");
                 As a temporary fix, we just pop the pointer instead of freeing
                 it.
         */
        cg.pop(0, 1);

        return cg.get();
    }

    @Override
    public Expr genCall(
        String funName, FunctionType fun, ArrayList<IExpr> params
    ) {
        int parameterNumber = params.size();
        genCallImpl(
            fun.getParameterTypes().iterator(parameterNumber),
            params.listIterator(parameterNumber)
        );
        cg.call("SB", "function_" + funName);
        return new Expr(cg.get());
    }

    @Override
    public IExpr genCall(
        Method method, Pointer ptrType, IExpr self, ArrayList<IExpr> params
    ) {
        cg.comment("Method " + method.getName() + " call:");
        // TODO: it is currently impossible to iterate from end to begin
        //       on method parameters, this is an ugly fix
        ArrayList<Type> types = new ArrayList<>();
        for (Type type : method.getParameterTypes()) {
            types.add(type);
        }

        genCallImpl(
            types.listIterator(types.size()),
            params.listIterator(types.size())
        );

        cg.append(self.getCode());
        getValue(self, ptrType.visit(sizeVisitor));

        // copy the pointer
        cg.loada("-1 [ST]");
        cg.loadi(1);

        // get the vtable
        cg.loadi(1);

        // the method address is at the position 2*n+1
        int methodIndex = method.getClassType().getMethods().indexOf(method);
        cg.loadl(2 * methodIndex);
        cg.subr("IAdd");
        cg.loadl(1);
        cg.subr("IAdd");

        cg.loadi(1);
        cg.calli();

        cg.comment("End of method " + method.getName() + " call:");

        return new Expr(cg.get());
    }

    @Override
    public Expr genCall(Method method, ArrayList<IExpr> params) {
        // TODO: it is currently impossible to iterate from end to begin
        //       on method parameters, this is an ugly fix
        ArrayList<Type> types = new ArrayList<>();
        for (Type type : method.getParameterTypes()) {
            types.add(type);
        }

        genCallImpl(
            types.listIterator(types.size()),
            params.listIterator(types.size())
        );
        cg.call("SB", "method_" + name(method));

        return new Expr(cg.get());
    }

    private void genCallImpl(
        ListIterator<Type> typeIt, ListIterator<IExpr> paramIt
    ) {
        while (paramIt.hasPrevious()) {
            IExpr expr = paramIt.previous();
            Type type = typeIt.previous();
            cg.append(expr.getCode());
            getValue(expr, type.visit(sizeVisitor));
        }
    }

    @Override
    public Expr genSizeOf(Type type) {
        return genInt(type.visit(sizeVisitor));
    }

    @Override
    public Expr genIdent(InfoVar info) {
        Location location = (Location)info.getLoc();

        if (location.getReg() == null) { // attributes
            cg.loada(selfLocation());
            cg.loadi(1);
            cg.loadl(location.getDep());
            cg.subr("IAdd");

            return new Expr(cg.get());
        }
        else {
            cg.loada(location.toString());

            // array parameters need not to be loaded
            boolean needsLoadi =
                !(location.getDep() < 0 && info.getType().isArray());

            return new Expr(cg.get(), needsLoadi);
        }
    }
    @Override
    public Expr genAff(Type t, IExpr loc, IExpr rhs) {
        cg.comment("start genAff");
        cg.append(rhs.getCode());
        getValue(rhs, 1);
        cg.append(loc.getCode());
        cg.storei(t.visit(sizeVisitor));
        cg.comment("end genAff");
        return new Expr(cg.get());
    }
    @Override
    public IExpr genNonAff(Type type, IExpr expr) {
        return expr;
    }

    @Override
    public Expr genDeref(Type type, IExpr expr) {
        int size = type.visit(sizeVisitor);
        cg.append(expr.getCode());
        getValue(expr, size);
        return new Expr(cg.get(), true);
    }
    @Override
    public IExpr genArrSub(Array type, IExpr lhs, IExpr rhs) {
        return genSubImpl(type.getPointee().visit(sizeVisitor), lhs, rhs);
    }
    @Override
    public IExpr genPtrSub(Pointer type, IExpr lhs, IExpr rhs) {
        return genSubImpl(type.getPointee().visit(sizeVisitor), lhs, rhs);
    }
    private Expr genSubImpl(int size, IExpr lhs, IExpr rhs) {
        cg.loadl(size);
        cg.append(rhs.getCode());
        getValue(rhs, 1 /* integer size */);
        cg.subr("IMul");
        cg.append(lhs.getCode());
        getValue(lhs, 1 /* pointer size */);
        cg.subr("IAdd");
        return new Expr(cg.get(), true);
    }

    @Override
    public IExpr genCast(Type from, Type to, IExpr expr) {
        if (from.isArray() && to.isPointer()) {
            return new Expr(expr.getCode()); // need not to load
        }                                    // for array to ptr cast
        else {
            return expr;
        }
    }

    @Override
    public IExpr genIntUnaryOp(String op, IExpr expr) {
        switch(op){
            case "+":
                return expr;
            case "-":
                cg.append(expr.getCode());
                getValue(expr, 1);
                cg.subr("INeg");
                break;
            case "!":
                cg.append(expr.getCode());
                getValue(expr, 1);
                cg.loadl("0");
                cg.subr("IEq");
                break;
            default:
                cg.append("<<<ERROR>>> genIntUnaryOp " + op + '\n');
        }
        return new Expr(cg.get());
    }

    @Override
    public Expr genIntBinaryOp(String op, IExpr lhs, IExpr rhs) {
        cg.append(lhs.getCode());
        getValue(lhs, 1);
        cg.append(rhs.getCode());
        getValue(rhs, 1);
        cg.subr(binaryOperators.get(op));

        return new Expr(cg.get());
    }
    @Override
    public Expr genCharBinaryOp(String op, IExpr lhs, IExpr rhs) {
        return genIntBinaryOp(op, lhs, rhs);
    }
    @Override
    public Expr genPtrBinaryOp(
        String op, Type pointer,
        IExpr lhs, IExpr rhs
    ) {
        return genIntBinaryOp(op, lhs, rhs);
    }

    @Override
    public String genComment(String comment) {
        cg.comment(comment);
        return cg.get();
    }

    private void getValue(IExpr expr, int size) {
        if (((Expr)expr).isAddress()) {
            cg.loadi(size);
        }
    }

    @Override
    public String genClass(ClassType clazz, String methodsCode) {
        String className = clazz.toString();
        cg.globalComment("Class " + className);

        cg.vtable(className);

        for (Method method : clazz.getMethods()) {
            cg.declLoadl('"' + method.getName() + '"');
            cg.declLoada("method_" + name(method));
        }

        // mark the end of the vtable
        cg.declLoadl("\"\"");
        cg.declLoadl(0);

        cg.globalComment("end of vtable for " + className + '\n');

        cg.append(methodsCode);

        return cg.get();
    }
}

/** A visitor to get the size of types.
 */
final class SizeVisitor extends moc.type.SizeVisitor {
    protected int baseClassSize()            { return 1; /* vtable */ }
    public Integer visit(CharacterType what) { return 1; }
    public Integer visit(BooleanType what)   { return 1; }
    public Integer visit(IntegerType what)   { return 1; }
    public Integer visit(IdType what)        { return 1; }
    public Integer visit(NullType what)      { return 1; }
    public Integer visit(Pointer what)       { return 1; }
}

