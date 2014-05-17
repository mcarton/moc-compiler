package moc.gc.tam;

import java.util.ArrayList;
import java.util.HashMap;
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

    int currentAddress = 3; // 0 -> ?
                            // 1 -> LB previous function
                            // 2 -> return address
    Stack<Integer> addressStack = new Stack<>();
    CodeGenerator cg = new CodeGenerator(this);

    Map<String, String> binaryOperators = new HashMap<>();

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
    }

    @Override
    public void endFunction() {
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
    public Location getLocationFor(String name, Type type) {
        Location tempLoc = new Location(currentAddress, "LB");
        currentAddress += type.visit(sizeVisitor);
        return tempLoc;
    }

    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType f, ArrayList<moc.gc.Location> parameters,
        String name, String block
    ) {
        cg.function(name);
        cg.append(block);
        return cg.get();
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr expr) {
        int param_size = 0;
        for (Type t : f.getParameterTypes()) {
            param_size += t.visit(sizeVisitor);
        }
        int return_size = f.getReturnType().visit(sizeVisitor);
        cg.append(expr.getCode());
        getValue(expr, return_size);
        cg.ret(return_size, param_size);
        return cg.get();
    }

    @Override
    public String genInst(moc.gc.Expr expr) {
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
    public String genIf(moc.gc.Expr cond, String thenCode, String elseCode) {
        return thenCode; // TODO:code
    }
    @Override
    public String genElse() {
        return null; // TODO:code
    }
    @Override
    public String genElse(String code) {
        return code; // TODO:code
    }

    @Override
    public String genVarDecl(Type t, moc.gc.Location loc) {
        cg.comment("declaration of " + loc + " (" + t + ')');
        cg.push(t.visit(sizeVisitor));
        return cg.get();
    }
    @Override
    public String genVarDecl(Type t, moc.gc.Location loc, moc.gc.Expr expr) {
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
    public Expr genNew(Type t) {
        cg.loadl(t.visit(sizeVisitor));
        cg.subr("Malloc");

        return new Expr(cg.get());
    }
    @Override
    public String genDelete(Type t, moc.gc.Expr expr) {
        cg.append(expr.getCode());
        getValue(expr, t.visit(sizeVisitor));
        cg.subr("Mfree");
        return cg.get();
    }

    @Override
    public Expr genCall(
        String funName, FunctionType fun,
        ArrayList<moc.gc.Expr> exprs
    ) {
        return null; // TODO:code
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
    public Expr genAff(Type t, moc.gc.Expr loc, moc.gc.Expr gcrhs) {
        // TODO:check
        cg.append(loc.getCode());
        cg.append(gcrhs.getCode());
        getValue(gcrhs,1);
        cg.subr("STOREI");
        return new Expr(cg.get());
    }
    @Override
    public Expr genNonAff(Type t, moc.gc.Expr expr) {
        // TODO:code
        return (moc.gc.tam.Expr)expr;
    }

    @Override
    public Expr genDeref(Type type, moc.gc.Expr expr) {
        int size = type.visit(sizeVisitor);
        cg.append(expr.getCode());
        getValue(expr, size);
        cg.loadi(size);
        return new Expr(cg.get());
    }
    @Override
    public moc.gc.Expr genArrSub(Array type, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public moc.gc.Expr genPtrSub(Pointer type, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return lhs; // TODO:code
    }
    @Override
    public moc.gc.Expr genCast(Type from, Type to, moc.gc.Expr expr) {
        return expr; // TODO:code
    }

    @Override
    public moc.gc.Expr genIntUnaryOp(String op, moc.gc.Expr expr) {
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
    public Expr genIntBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        cg.append(lhs.getCode());
        getValue(lhs, 1);
        cg.append(rhs.getCode());
        getValue(rhs, 1);
        cg.subr(binaryOperators.get(op));

        return new Expr(cg.get());
    }
    @Override
    public Expr genCharBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        return genIntBinaryOp(op, lhs, rhs);
    }
    @Override
    public Expr genPtrBinaryOp(
        String op, Type pointer,
        moc.gc.Expr lhs, moc.gc.Expr rhs
    ) {
        return genIntBinaryOp(op, lhs, rhs);
    }

    @Override
    public String genComment(String comment) {
        cg.comment(comment);
        return cg.get();
    }

    private void getValue(moc.gc.Expr expr, int size) {
        if (expr.getLoc() != null){
            cg.load(size, expr.getLoc().toString());
        }
    }
}

/** A visitor to get the size of types.
 */
class SizeVisitor implements TypeVisitor<Integer> {
    public Integer visit(IntegerType what)   { return 1; }
    public Integer visit(CharacterType what) { return 1; }

    public Integer visit(VoidType what)      { return 0; }
    public Integer visit(NullType what)      { return 1; }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }
    public Integer visit(Pointer what)       { return 1; }
}

