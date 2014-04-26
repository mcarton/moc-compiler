package moc.gc.llvm;

import java.util.ArrayList;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.util.Iterator;
import moc.gc.*;
import moc.compiler.MOCException;
import moc.type.*;
import moc.symbols.*;

/**
 * The TAM machine and its generation functions
 */
public class Machine extends AbstractMachine {
    int lastTmp = 0; // name of the last generated temporary
    int bloc = 0; // the bloc we are in

    RepresentationVisitor typeVisitor = new RepresentationVisitor();

    public Machine(int verbosity, ArrayList<String> warnings) {
        super(verbosity, warnings);
    }

    @Override
    public void writeCode(String fname, String code) throws MOCException {
        // TODO: to remove if we add forward declaration of function
        // we need to add some declaration here for now
        code = code
            + "declare i8* @malloc(i64)\n"
            + "declare void @free(i8*)\n";
        super.writeCode(fname, code);
    }

    @Override
    public String getSuffix() {
        return "ll";
    }

    // type size stuffs:
    @Override public Type getCharType() {
        return new CharacterType(1);
    }
    @Override public Type getIntType() {
        return new IntegerType(8);
    }
    @Override public Type getPtrType(Type what) {
        return new Pointer(8, what);
    }
    @Override public Type getArrayType(Type what, int nbElements) {
        return new Array(what, nbElements);
    }

    // location stuffs:
    @Override
    public void newFunction() {
        lastTmp = 0;
        bloc = 0;
    }

    @Override
    public void newBloc() {
        ++bloc;
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

        Iterator<Type> it = f.getParameterTypes().iterator();
        Iterator<moc.gc.Location> nameIt = params.iterator();
        while(it.hasNext()) {
            sb.append(it.next().visit(typeVisitor));
            sb.append(' ');
            sb.append(nameIt.next());
            if(it.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(") nounwind {\n"); // nounwind = no exceptions
        sb.append(bloc);
        sb.append("}\n\n");

        return sb.toString();
    }

    @Override
    public String genReturn(FunctionType f, moc.gc.Expr gcexpr) {
        Expr expr = (Expr)gcexpr;

        StringBuilder sb = new StringBuilder();

        String returnTypeRepr = f.getReturnType().visit(typeVisitor);
        String returnValue = getValue(returnTypeRepr, expr, sb);

        sb.append("    ret ");
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

        sb.append("    ");
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

        sb.append("    ");
        sb.append(loc);
        sb.append(" = alloca ");
        sb.append(type);
        sb.append('\n');

        sb.append("    store ");
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
    @Override
    public Expr genString(String txt) {
        return new Expr(new Location("%TODO"), "TODO");
    }
    @Override
    public Expr genCharacter(String txt) {
        return new Expr(null, txt); // TODO:string
    }
    @Override
    public Expr genNew(Type t) {
        StringBuilder sb = new StringBuilder(50);

        String type = t.visit(typeVisitor);

        // call malloc
        // <result1> = call i8* @malloc(i64 4)
        String tmpPtr = getTmpName();
        sb.append("    ");
        sb.append(tmpPtr);
        sb.append(" = call i8* @malloc(i64 ");
        sb.append(t.getSize());
        sb.append(")\n");

        // cast to right pointer type
        // <result2> = bitcast i8* <result1> to i32*
        String tmpCastedPtr = getTmpName();
        sb.append("    ");
        sb.append(tmpCastedPtr);
        sb.append(" = bitcast i8* ");
        sb.append(tmpPtr);
        sb.append(" to ");
        sb.append(type);
        sb.append("*\n");

        return new Expr(new Location(tmpCastedPtr), sb.toString());
    }
    @Override
    public String genDelete(Type t, moc.gc.Location loc) {
        StringBuilder sb = new StringBuilder(50);

        String type = t.visit(typeVisitor);

        // cast to i8* (~ void*)
        // <result> = bitcast i32* <ptr> to i8*
        String tmpPtr = getTmpName();
        sb.append(tmpPtr);
        sb.append(" = bitcast ");
        sb.append(type);
        sb.append("* ");
        sb.append(loc);
        sb.append(" to i8*\n");

        // call void @free(i8* <result>) #2
        sb.append("call void @free(i8* ");
        sb.append(tmpPtr);
        sb.append(")\n");

        return sb.toString();
    }

    @Override
    public Expr genIdent(InfoVar info) {
        StringBuilder sb = new StringBuilder();

        String tmpValueName = getTmpName();
        sb.append("    ");
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

        sb.append("    store ");
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
    public Expr genAdd(moc.gc.Expr lhs, moc.gc.Expr rhs) {
        StringBuilder sb = new StringBuilder();

        String type = "i64";
        String lhsCode = getValue(type, lhs, sb);
        String rhsCode = getValue(type, rhs, sb);
        String tmpValueName = getTmpName();

        // <result> = add <ty> <op1>, <op2>
        sb.append("    ");
        sb.append(tmpValueName);
        sb.append(" = add ");
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
        return "    ; " + comment + '\n';
    }

    private String getTmpName() {
        return String.valueOf("%" + ++lastTmp);
    }

    /**
     * Get the value of the expression, it may be either a constant like `42` or
     * `null`, or an unnamed temporary like `%1`; if expr is not a constant,
     * prepend the code used to genererate the value.
     *
     * @warning: the function has side effect on sb and may increment lastTmp!
     */
    private String getValue(String type, moc.gc.Expr expr, StringBuilder sb) {
        if(expr.getLoc() != null) {
            sb.append(expr.getCode());
            return expr.getLoc().toString();
        }
        else {
            return expr.getCode();
        }
    }
}

class RepresentationVisitor implements TypeVisitor<String> {
    public String visit(Type what) {
        throw new UnsupportedOperationException(
            "This visitor does not support the type of " + what
        );
    }

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

