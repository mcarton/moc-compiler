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
    Expr genCall(String funName, FunctionType fun, ArrayList<IExpr> params) {
        prepare(fun.getReturnType());
        return genCallImpl(funName, fun.getParameterTypes(), params);
    }

    /**
     * Generate code for a static method call.
     */
    Expr genCall(Method method, ArrayList<IExpr> params) {
        prepare(method.getReturnType());
        return genCallImpl(
            "method."+ name(method), method.getParameterTypes(), params
        );
    }

    /**
     * Generate code for a method call on a instance.
     */
    Expr genCall(Method method, Pointer type, IExpr self, ArrayList<IExpr> params) {
        prepare(method.getReturnType());

        printParametersCode(params.iterator());

        String selfType = cg().typeName(type.getPointee());
        String selfTypePtr = cg().typeName(type);
        String selfName = machine.getValue(selfTypePtr, self);
        ArrayList<String> names = loadParameters(
            method.getParameterTypes().iterator(), params.iterator()
        );

        String methodName = getMethodFromVtable(method, type, selfType, selfName);

        String tmpValueName = callBegin(
            returnTypeName + " (...)*", methodName, true
        );
        cg().parameter(false, selfTypePtr, selfName);
        passParameters(true, method.getParameterTypes().iterator(), names.iterator());
        cg().callEnd();

        return new Expr(new Location(tmpValueName), cg().get());
    }

    Expr genCallImpl(
        String funName, Iterable<Type> types, ArrayList<IExpr> params
    ) {
        printParametersCode(params.iterator());
        ArrayList<String> names = loadParameters(
            types.iterator(), params.iterator()
        );

        String tmpValueName = callBegin(
            returnTypeName, '@' + funName, types.iterator().hasNext()
        );
        passParameters(false, types.iterator(), names.iterator());
        cg().callEnd();

        return new Expr(new Location(tmpValueName), cg().get());
    }

    String getMethodFromVtable(
        Method method, Pointer type, String selfType, String self
    ) {
        String vtable = getVtable((ClassType)type.getPointee(), selfType, self);

        int methodIndex = method.getClassType().getMethods().indexOf(method);
        String loadedMethod = cg().load("%mocc.method*", vtable);
        String methodPtr = cg().getelementptr(
            "%mocc.method", loadedMethod,
            new String[] {"i32", Integer.toString(methodIndex), "i32", "1"}
        );
        String loadedMethodPtr = cg().load("void (...)*", methodPtr);

        if (!returnsVoid) {
            String castMethodPtr = cg().cast(
                "bitcast",
                "void (...)*", loadedMethodPtr, returnTypeName + " (...)*"
            );
            return castMethodPtr;
        }
        else {
            return loadedMethodPtr;
        }
    }

    String getVtable(ClassType type, String selfType, String self) {
        ArrayList<String> parameters = new ArrayList<>(4);
        parameters.add("i64");
        parameters.add("0");

        for (int i = 0, end = type.parentNumbers(); i <= end+1; ++i) {
            parameters.add("i32");
            parameters.add("0");
        }

        return cg().getelementptr(
            selfType, self, parameters.toArray(new String[parameters.size()])
        );
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

        cg().beginDefine(returnTypeName, '@' + name);
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

        String className = cg().typeName(method.getClassType()) + '*';
        String name = name(method);
        String methodName = "@method." + name;
        String constantName = "@name." + name;

        cg().beginDefine(returnTypeName, methodName);
        returnArray();
        if (!method.isStatic()) {
            cg().parameter(false, className, "%self");
        }
        parameters(method.getParameterTypes().iterator(), true);
        cg().endDefine();

        if (!method.isStatic() && method.getClassType().hasSuper()) {
            String superName = cg().typeName(method.getClassType().getSuper()) + '*';
            cg().cast("%super", "bitcast", className, "%self", superName);
        }

        allocateReturn(returnsVoid);
        allocateParameters(
            parameters.iterator(), method.getParameterTypes().iterator(), true
        );

        cg().body(block);
        endFunction();

        if (!method.isStatic()) {
            cg().stringCstDeclaration(constantName, name.length()+1, name);

            // TODO: clean+ array parameters
            StringBuilder methodTypeString = new StringBuilder();
            methodTypeString.append(returnTypeName);
            methodTypeString.append('(');
            methodTypeString.append(className);
            for (Type type : method.getParameterTypes()) {
                methodTypeString.append(", ");
                methodTypeString.append(cg().typeName(type));
            }
            methodTypeString.append(")*");
            cg().methodCstDeclaration(
                methodTypeString.toString(), name, name.length()+1
            );
        }

        return cg().get();
    }

    void genVirtualTable(String className, List<Method> methods) {
        int tableSize = methods.size() + 1 /* terminal null */;
        cg().vtableBegin(className, tableSize);

        for(Method method : methods) {
            cg().vtableAdd(name(method));
        }

        cg().vtableEnd();
        cg().vtablePtr(className, tableSize);
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

    String callBegin(
        String returnTypeName, String funName, boolean hasParameters
    ) {
        if (!returnsVoid) {
            return cg().callNonVoid(returnTypeName, funName);
        }
        else if (returnsArray) {
            return callReturnsArray(funName, hasParameters);
        }
        else {
            cg().callVoid(returnTypeName, funName);
            return null;
        }
    }

    ArrayList<String> loadParameters(Iterator<Type> typeIt, Iterator<IExpr> exprIt) {
        ArrayList<String> names = new ArrayList<>();
        while (exprIt.hasNext()) {
            names.add(machine.getValue(
                    cg().typeName(typeIt.next()), exprIt.next(), false
            ));
        }
        return names;
    }

    void passParameters(
        boolean hasPrevious, Iterator<Type> typeIt, Iterator<String> nameIt
    ) {
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
    String callReturnsArray(String funName, boolean hasNext) {
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

    static String name(Method method) {
        StringBuilder sb = new StringBuilder(method.getClassType().toString());

        for (Selector selector : method.getSelectors()) {
            sb.append('.' + selector.getName());
        }

        return sb.toString();
    }
}

