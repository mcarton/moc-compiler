package moc.gc.llvm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
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
    int returnTmp;

    void prepare(Type returnType) {
        this.returnType = returnType;
        returnsArray = returnType.isArray();
        returnsVoid = returnType.isVoid() || returnsArray;
        returnTypeName = returnsVoid ? "void" : cg().typeName(returnType);
        returnTmp = machine.lastTmp;
    }

    /**
     * Generate code for a function call.
     */
    Expr genCall(String funName, FunctionType fun, ArrayList<IExpr> exprs) {
        prepare(fun.getReturnType());

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
        FunctionType fun, ArrayList<ILocation> parameters,
        String name, String block
    ) {
        prepare(fun.getReturnType());
        machine.lastTmp = 0; // reseted for parameters

        cg().beginDefine(returnTypeName, name);
        returnArray();
        parameters(fun.getParameterTypes().iterator(), returnsArray);
        cg().endDefine();

        allocateReturn(returnsVoid);
        allocateParameters(
            parameters.iterator(), fun.getParameterTypes().iterator(),
            !parameters.isEmpty()
        );

        cg().body(block);
        endFunction();

        return cg().get();
    }

    /**
     * Generate code for a method definition. The same rules as for functions
     * apply to the return type.
     */
    String genMethod(
        Method method, ArrayList<ILocation> parameters, String block
    ) {
        prepare(method.getReturnType());
        machine.lastTmp = 0; // reseted for parameters

        cg().beginDefine(returnTypeName, mangledName(method));
        returnArray();
        cg().parameter(false, cg().typeName(method.getClassType()) + '*', "%__self");
        parameters(method.getParameterTypes().iterator(), true);
        cg().endDefine();

        allocateReturn(returnsVoid);
        allocateParameter(new Pointer(method.getClassType()), "%__self", "%self");
        allocateParameters(
            parameters.iterator(), method.getParameterTypes().iterator(), true
        );

        cg().body(block);
        endFunction();

        return cg().get();
    }

    void genVirtualTable(String className, List<Method> methods) {
        StringBuilder vtable = new StringBuilder();
        StringBuilder methodNamesString = new StringBuilder();
        StringBuilder methodTypeString = new StringBuilder();

        Iterator<Method> methIt = methods.iterator();
        while (methIt.hasNext()) {
            Method current = methIt.next();
            int methodNameLenght = 0;
            for (Selector selector : current.getSelectors()) {
                methodNamesString.append(selector.getName());
                methodNamesString.append("\\00");
                methodNameLenght += selector.getName().length() + 1;
            }

            // TODO: arrays as parameter or return type
            methodTypeString.append(cg().typeName(current.getReturnType()));
            methodTypeString.append("(%class." + className + '*');
            for (Type type : current.getParameterTypes()) {
                methodTypeString.append(", ");
                methodTypeString.append(cg().typeName(type));
            }
            methodTypeString.append(")*");

            String name = methodNamesString.toString();
            methodNamesString.setLength(0);

            String type = methodTypeString.toString();
            methodTypeString.setLength(0);

            String mangledName = mangledName(current);
            String constantName = "@names." + mangledName;
            cg().stringCstDeclaration(constantName, methodNameLenght+1, name);
            cg().methodCstDeclaration(type, mangledName, methodNameLenght+1);

            vtable.append("    %mocc.method* @ptr.");
            vtable.append(mangledName);
            if (methIt.hasNext()) {
                vtable.append(",\n");
            }
        }

        cg().vtableBegin(className, methods.size());
        cg().declAppend(vtable);
        cg().vtableEnd();
    }

    // implementation stuffs for function definition:

    /** Allocate space for the parameters. */
    void allocateParameters(
        Iterator<ILocation> locIt, Iterator<Type> it, boolean hasParameters
    ) {
        int paramIt = 0;
        while (it.hasNext()) {
            Type paramType = it.next();
            String paramName = locIt.next().toString();
            allocateParameter(paramType, "%__p"+ ++paramIt, paramName);
        }

        if (hasParameters || !returnsVoid) {
            cg().comment("end of generated code for return value and parameters");
            cg().skipLine();
        }
    }

    /** Allocate space for a parameter. */
    void allocateParameter(Type type, String tmpName, String name) {
        cg().alloca(name, cg().typeName(type));
        machine.copy(type, tmpName, name);
    }

    /** Allocate space for the return value. */
    void allocateReturn(boolean returnsVoid) {
        if (!returnsVoid) {
            cg().alloca(returnParameterName, returnTypeName);
        }
    }

    /** Add the parameter names of the form "__p0", "__p1". */
    void parameters(Iterator<Type> it, boolean hasPrevious) {
        int paramIt = 0;
        while (it.hasNext()) {
            Type type = it.next();
            String typename = cg().typeName(type);

            if (type.isArray()) {
                typename += '*';
            }

            cg().parameter(hasPrevious, typename, "%__p" + ++paramIt);
            hasPrevious = true;
        }
    }

    /** Add the `return` parameter if the return type if an array. */
    void returnArray() {
        if (returnsArray) {
            cg().parameter(
                false,
                cg().typeName(returnType) + "* noalias sret",
                returnParameterName
            );
        }
    }

    /** Terminates the function and returns. */
    void endFunction() {
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

    void passParameters(ListIterator<Type> typeIt, Iterator<String> nameIt) {
        boolean hasPrevious = typeIt.hasPrevious();
        while (typeIt.hasNext()) {
            Type type = typeIt.next();
            String typename = cg().typeName(type);

            if (type.isArray()) {
                typename += '*';
            }

            cg().parameter(hasPrevious, typename, nameIt.next());
            hasPrevious = true;
        }
    }

    /**
     * When the return type is an arrays it is passed as the first parameter.
     */
    String passReturnAddress(String funName, boolean hasNext) {
        String returnTypeName = cg().typeName(returnType);
        String tmpValueName = machine.getTmpName();
        cg().alloca(tmpValueName, returnTypeName);
        cg().callVoid(funName);
        cg().parameter(false, returnTypeName + '*', tmpValueName);
        if (hasNext) {
            cg().append(", ");
        }
        return tmpValueName;
    }

    void printParametersCode(Iterator<IExpr> exprIt) {
        while (exprIt.hasNext()) {
            machine.printCode(exprIt.next());
        }
    }

    String mangledName(Method method) {
        StringBuilder sb = new StringBuilder("method");

        sb.append('.');
        sb.append(method.getClassType());

        for (Selector selector : method.getSelectors()) {
            sb.append('.' + selector.getName());
        }

        return sb.toString();
    }
}

