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
    SizeVisitor sizeVisitor = new SizeVisitor();

    int currentParameterAddress;
    int currentAddress = 3; // 0 -> ?
                            // 1 -> LB previous function
                            // 2 -> return address
    Stack<Integer> addressStack = new Stack<>();
    CodeGenerator cg = new CodeGenerator(this);

    int labelCount = 0;
    Map<String, String> binaryOperators = new HashMap<>();

    private int parametersSize;

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
        parametersSize = paramSize(fun);
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

    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType f, ArrayList<ILocation> parameters,
        String name, String block
    ) {
        cg.function(name);
        cg.append(block);

        if (f.getReturnType().isVoid()) {
            cg.ret(0, paramSize(f));
        }

        return cg.get();
    }

    @Override
    public String genMethod(
        Method method, ArrayList<ILocation> parameters, String bloc
    ) {
        return "TODO:method";
    }

    private int paramSize(FunctionType fun) {
        int paramSize = 0;
        for (Type t : fun.getParameterTypes()) {
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
    public String genInst(IExpr expr) {
        return expr.getCode();
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
        // TODO:code
        return null;
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
        cg.loadl(txt);
        return new Expr(cg.get());
    }
    @Override
    public Expr genCharacter(String txt) {
        cg.loadl(txt);
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
    public Expr genNew(Type t) {
        cg.loadl(t.visit(sizeVisitor));
        cg.subr("Malloc");

        return new Expr(cg.get());
    }
    @Override
    public Expr genNew(Type t, IExpr expr) {
        cg.loadl(t.visit(sizeVisitor));
        cg.subr("Malloc");

        // TODO: aff

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
        String funName, FunctionType fun,
        ArrayList<IExpr> exprs
    ) {
        ListIterator<IExpr> it = exprs.listIterator(exprs.size());
        ListIterator<Type> typeIt
            = fun.getParameterTypes().iterator(exprs.size());
        while (it.hasPrevious()) {
            IExpr expr = it.previous();
            Type type = typeIt.previous();
            cg.append(expr.getCode());
            getValue(expr, type.visit(sizeVisitor));
        }
        cg.call("SB", "function_" + funName);
        return new Expr(cg.get());
    }
    @Override
    public Expr genSizeOf(Type type) {
        return genInt(type.visit(sizeVisitor));
    }

    @Override
    public Expr genIdent(InfoVar info) {
        return new Expr("", (Location)info.getLoc());
    }
    @Override
    public Expr genAff(Type t, IExpr loc, IExpr gcrhs) {
        cg.append(loc.getCode());
        cg.append(gcrhs.getCode());
        cg.loada(loc.getLoc().toString());
        getValue(gcrhs, 1);
        cg.storei(t.visit(sizeVisitor));
        return new Expr(cg.get());
    }
    @Override
    public Expr genNonAff(Type t, IExpr expr) {
        // TODO:code
        return (moc.gc.tam.Expr)expr;
    }

    @Override
    public Expr genDeref(Type type, IExpr expr) {
        int size = type.visit(sizeVisitor);
        cg.append(expr.getCode());
        getValue(expr, size);
        cg.loadi(size);
        return new Expr(cg.get());
    }
    @Override
    public IExpr genArrSub(Array type, IExpr lhs, IExpr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public IExpr genPtrSub(Pointer type, IExpr lhs, IExpr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public IExpr genCast(Type from, Type to, IExpr expr) {
        return expr; // TODO:code
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
        if (expr.getLoc() != null){
            cg.load(size, expr.getLoc().toString());
        }
    }

    @Override
    public String genClass(ClassType clazz, String methodsCode) {
        // TODO:class
        return genComment("Class " + clazz.toString());
    }
}

/** A visitor to get the size of types.
 */
class SizeVisitor implements TypeVisitor<Integer> {
    public Integer visit(CharacterType what) { return 1; }
    public Integer visit(BooleanType what)   { return 1; }
    public Integer visit(IntegerType what)   { return 1; }

    public Integer visit(ClassType what) {
        return -1; // TODO:class
    }

    public Integer visit(VoidType what)      { return 0; }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }
    public Integer visit(NullType what)      { return 1; }
    public Integer visit(Pointer what)       { return 1; }
}

