package moc.gc.tam;

import java.util.ArrayList;
import java.util.Stack;
import moc.gc.*;
import moc.symbols.*;
import moc.type.*;
/**
 * The TAM machine and its generation functions.
 */
public class Machine extends AbstractMachine {
    SizeVisitor sizeVisitor = new SizeVisitor();

    int currentAddress = 0;
    Stack<Integer> addressStack = new Stack<>();

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
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
        Location tempLoc = new Location(currentAddress, null /* TODO:reg */);
        currentAddress += type.visit(sizeVisitor);
        return tempLoc;
    }

    // code generation stuffs:
    @Override
    public String genFunction(
        FunctionType f, ArrayList<moc.gc.Location> parameters,
        String name, String block
    ) {
        return block; // TODO:code
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr expr) {
        return ""; // TODO:code
    }

    @Override
    public String genInst(moc.gc.Expr expr) {
        return expr.getCode();
    }

    @Override
    public String genAsm(String code) {
        return code.substring(1, code.length()-1);
    }

    @Override
    public String genGlobalAsm(String code) {
        return ""; // TODO:code
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
        StringBuilder sb = new StringBuilder(50);

        sb.append("    ");
        sb.append("PUSH ");
        sb.append(t.visit(sizeVisitor));
        sb.append('\n');
        currentAddress = currentAddress + t.visit(sizeVisitor);
        return sb.toString();
    }
    @Override
    public String genVarDecl(Type t, moc.gc.Location loc, moc.gc.Expr expr) {
        StringBuilder sb = new StringBuilder(50);

        sb.append(expr.getCode());
        sb = getValue(sb, expr, t.visit(sizeVisitor));
        currentAddress = currentAddress + t.visit(sizeVisitor);
        return sb.toString();
    }

    @Override
    public Expr genInt(String txt) {
        return new Expr("    LOADL " + txt + "\n");
    }
    public Expr genInt(int nb) {
        return genInt(Integer.toString(nb));
    }
    @Override
    public Expr genString(int length, String txt) {
        return new Expr("    LOADL " + txt + "\n");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr("    LOADL " + txt + "\n");
    }
    @Override
    public Expr genNull() {
        return new Expr("    SUBR MVoid \n");
    }
    @Override
    public Expr genNew(Type t) {
        StringBuilder sb = new StringBuilder(50);

        sb.append("    ");
        sb.append("LOADL ");
        sb.append(t.visit(sizeVisitor));
        sb.append('\n');
        sb.append("    ");
        sb.append("SUBR Malloc");
        sb.append('\n');
        return new Expr(sb.toString());
    }
    @Override
    public String genDelete(Type t, moc.gc.Expr expr) {
        StringBuilder sb = new StringBuilder(50);
        System.out.println("size type"+t.visit(sizeVisitor));
        System.out.println(expr);
        sb.append("    ");
        sb.append(expr.getCode());
        sb.append('\n');
        sb = getValue(sb,expr,t.visit(sizeVisitor));
        sb.append("    ");
        sb.append("SUBR Mfree");
        sb.append('\n');

        return sb.toString(); // TODO:check
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
        // TODO:code
        return null;
    }
    @Override
    public Expr genNonAff(Type t, moc.gc.Expr expr) {
        // TODO:code
        return (moc.gc.tam.Expr)expr;
    }

    @Override
    public moc.gc.Expr genDeref(Type type, moc.gc.Expr expr) {
        return expr; // TODO:code
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
        // TODO:code
        return expr;
    }

    private StringBuilder getValue(StringBuilder sb, moc.gc.Expr expr, int size) {
        if (expr.getLoc()!=null){
            sb.append("    ");
            sb.append("LOAD ("+ size +")"+ expr.getLoc().toString());
            sb.append('\n');
        }
        return sb;
    }
    @Override
    public Expr genIntBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        StringBuilder sb = new StringBuilder(50);
        sb.append("    ");
        sb.append(lhs.getCode());
        // si .code est une location,
        sb.append('\n');
        sb.append("    ");
        sb = getValue(sb, lhs, 1);
        sb.append(rhs.getCode());
        sb.append('\n');
        sb = getValue(sb, rhs, 1);
        sb.append("    ");
        sb.append("SUBR ");
        switch(op){
            case "+":
                sb.append("IAdd");
                break;
            case "-":
                sb.append("ISub");
                break;
            case "*":
                sb.append("IMul");
                break;
            case "/":
                sb.append("IDiv");
                break;
        }
        sb.append('\n');
        return new Expr(sb.toString());
    }
    @Override
    public Expr genCharBinaryOp(String op, moc.gc.Expr lhs, moc.gc.Expr rhs) {
        // TODO:code
        return null;
    }
    @Override
    public Expr genPtrBinaryOp(
        String op, Type pointer,
        moc.gc.Expr lhs, moc.gc.Expr rhs
    ) {
        // TODO:code
        return null;
    }

    @Override
    public String genComment(String comment) {
        return("; " + comment + '\n');
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

