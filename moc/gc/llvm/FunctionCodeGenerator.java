package moc.gc.llvm;

import java.util.ArrayList;
import java.util.Iterator;
import moc.gc.*;
import moc.symbols.*;
import moc.type.*;

/**
 * This class exists only to lighten Machine.
 * It contains all stuffs relating functions.
 */
final class FunctionCodeGenerator {
    Machine machine;

    FunctionCodeGenerator(Machine machine) { this.machine = machine; }
    CodeGenerator cg() { return machine.cg; }

    static final String returnParameterName = "%__return";

    Type returnType;
    boolean returnsArray;
    boolean returnsVoid;
    String returnTypeName;

    void prepare(FunctionType function) {
        returnType = function.getReturnType();
        returnsArray = returnType.isArray();
        returnsVoid = returnType.isVoid() || returnsArray;
        returnTypeName = returnsVoid ? "void" : cg().typeName(returnType);
    }

    /**
     * Generate code for a function call.
     */
    Expr genCall(String funName, FunctionType fun, ArrayList<IExpr> exprs) {
        prepare(fun);

        printParametersCode(exprs.iterator());
        ArrayList<String> names = loadParameters(
            fun.getParameterTypes().iterator(), exprs.iterator()
        );

        String tmpValueName = null;

        if (!returnsVoid) {
            tmpValueName = cg().callNonVoid(returnTypeName, funName);
        }
        else if (returnsArray) {
            tmpValueName = passReturnAddress(
                funName, !fun.getParameterTypes().isEmpty()
            );
        }
        else {
            cg().callVoid(funName);
        }

        passParameters(fun.getParameterTypes().iterator(), names.iterator());

        cg().callEnd();

        return new Expr(new Location(tmpValueName), cg().get());
    }

    /**
     * Generate code for a function definition.
     * - When the return type is an array, it is passed as a pointer
     *   allocated by the callee.
     * - When a parameter is an array, it is passed as a pointer and must
     *   be copied in the function.
     */
    String genFunction(
        FunctionType fun, ArrayList<ILocation> params,
        String name, String block
    ) {
        prepare(fun);
        int returnTmp = machine.lastTmp;
        machine.lastTmp = 0; // reseted for parameters

        cg().beginDefine(returnTypeName, name);
        returnArray(returnsArray, !fun.getParameterTypes().isEmpty());
        parameters(fun.getParameterTypes().iterator());
        cg().endDefine();

        allocateReturn(returnsVoid);
        allocateParameters(params.iterator(), fun.getParameterTypes().iterator());

        if (!params.isEmpty() || !returnsVoid) {
            cg().comment("end of generated code for return value and parameters");
            cg().skipLine();
        }

        cg().body(block);
        endFunction(returnTmp);

        return cg().get();
    }

    // implementation stuffs for function definition:

    /** Allocate space for the return value. */
    void allocateParameters(Iterator<ILocation> locIt, Iterator<Type> it) {
        int paramIt = 0;
        while (it.hasNext()) {
            Type paramType = it.next();
            String paramName = locIt.next().toString();
            cg().alloca(paramName, cg().typeName(paramType));
            machine.copy(paramType, "%__p"+ ++paramIt, paramName);
        }
    }

    /** Allocate space for the parameters. */
    void allocateReturn(boolean returnsVoid) {
        if (!returnsVoid) {
            cg().alloca(returnParameterName, returnTypeName);
        }
    }

    /** Add the parameter names of the form "__p0", "__p1". */
    void parameters(Iterator<Type> it) {
        int paramIt = 0;
        while (it.hasNext()) {
            Type type = it.next();
            String typename = cg().typeName(type);

            if (type.isArray()) {
                typename += '*';
            }

            cg().parameter(typename, "%__p" + ++paramIt, it.hasNext());
        }
    }

    /** Add the `return` parameter if the return type if an array. */
    void returnArray(boolean returnsArray, boolean onlyParameter) {
        if (returnsArray) {
            cg().parameter(
                cg().typeName(returnType) + "* noalias sret",
                returnParameterName, onlyParameter
            );
        }
    }

    /** Terminates the function and returns. */
    void endFunction(int returnTmp) {
        cg().br("End");
        cg().label("End");

        if (returnsVoid) {
            cg().ret();
        }
        else {
            machine.lastTmp = returnTmp;
            String tmp = cg().load(returnTypeName, returnParameterName);
            cg().ret(returnTypeName, tmp);
        }

        cg().endFunction();
    }

    // implementation stuffs for function call:

    ArrayList<String> loadParameters(Iterator<Type> typeIt, Iterator<IExpr> exprIt) {
        ArrayList<String> names = new ArrayList<String>();
        while (exprIt.hasNext()) {
            names.add(
                machine.getValue(
                    cg().typeName(typeIt.next()), exprIt.next(), false
                )
            );
        }
        return names;
    }

    void passParameters(Iterator<Type> typeIt, Iterator<String> nameIt) {
        while (typeIt.hasNext()) {
            Type type = typeIt.next();
            String typename = cg().typeName(type);

            if (type.isArray()) {
                typename += '*';
            }

            cg().parameter(typename, nameIt.next(), typeIt.hasNext());
        }
    }

    /**
     * When the return type is an arrays it is passed as the first parameter.
     */
    String passReturnAddress(String funName, boolean hasParameters) {
        String returnTypeName = cg().typeName(returnType);
        String tmpValueName = machine.getTmpName();
        cg().alloca(tmpValueName, returnTypeName);
        cg().callVoid(funName);
        cg().parameter(returnTypeName + '*', tmpValueName, hasParameters);
        return tmpValueName;
    }

    void printParametersCode(Iterator<IExpr> exprIt) {
        while (exprIt.hasNext()) {
            machine.printCode(exprIt.next());
        }
    }
}

