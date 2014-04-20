package moc.compiler;

import java.io.Serializable;

import mg.egg.eggc.runtime.libjava.ISourceUnit;
import mg.egg.eggc.runtime.libjava.problem.IProblem;
import mg.egg.eggc.runtime.libjava.problem.ProblemReporter;
import mg.egg.eggc.runtime.libjava.problem.ProblemRequestor;
import moc.egg.MOC;

/**
 * The main class of the generated MOC compiler
 * Usage: mocc file.moc options
 */
public class MOCC implements Serializable {
    private static final long serialVersionUID = 0xa6006c8bL;

    private static void checkFile(String[] args) throws MOCException {
        // check .moc extension
        if (args.length == 0 || !args[0].endsWith(".moc")) {
            throw new MOCException(Messages.getString("MOC.extError"));
        }
    }

    public static void main(String[] args) {
        try {
            // At least the name of the source file is needed
            checkFile(args);

            // Create the source
            ISourceUnit cu = new MOCSourceFile(args);

            // Error management
            ProblemReporter prp = new ProblemReporter(cu);
            ProblemRequestor prq = new ProblemRequestor();
            System.out.println("Compiling " + cu.getFileName());

            // Start compilation
            MOC compilo = new MOC(prp);
            prq.beginReporting();
            compilo.set_source((MOCSourceFile) cu);
            compilo.set_eval(true);
            compilo.compile(cu);

            // Handle errors
            for (IProblem problem : prp.getAllProblems()) {
                prq.acceptProblem(problem);
            }
            prq.endReporting();

            int nbErrors = prq.getFatal();
            int nbWarnings = prp.getAllProblems().size() - nbErrors;
            if (nbErrors > 0)
            {
                if (nbWarnings > 0)
                    System.out.println(
                        "" + nbErrors + " errors and " +
                        nbWarnings + " warnings found. " +
                        "Compilation aborted.");
                else
                    System.out.println(
                        "" + nbErrors + " errors found. " +
                        "Compilation aborted.");
                System.exit(2);
            }
            else if(nbWarnings > 0)
            {
                System.out.println(
                    "" + nbWarnings +
                    " warnings found. Compilation successful.");
                System.exit(1);
            }
            else
            {
                System.out.println("Compilation successful.");
                System.exit(0);
            }
        }
        catch (MOCException e) {
            // Internal errors
            System.err.println(e.getMessage());
            System.exit(3);
        }
        catch (Exception e) {
            // Other errors
            e.printStackTrace();
            System.exit(4);
        }
    }
}
