package moc.compiler;

import java.io.Serializable;

import mg.egg.eggc.runtime.libjava.ISourceUnit;
import mg.egg.eggc.runtime.libjava.problem.IProblem;
import mg.egg.eggc.runtime.libjava.problem.ProblemReporter;
import mg.egg.eggc.runtime.libjava.problem.ProblemRequestor;
import moc.egg.MOC;

/**
 * The main class of the generated MOC compiler.
 *
 * Usage: mocc file.moc options
 */
public class MOCC implements Serializable {
    private static final long serialVersionUID = 0xa6006c8bL;

    private static void checkFile(String[] args) throws MOCException {
        // check .moc extension
        if (args.length == 0 || !args[args.length-1].endsWith(".moc")) {
            throw new MOCException(Messages.getString("MOC.extError"));
        }
    }

    public static void main(String[] args) {
        try {
            // At least the name of the source file is needed
            checkFile(args);

            // Create the source
            MOCSourceFile cu = new MOCSourceFile(args);

            if (cu.getMachine() == null) {
                System.out.println(Messages.getString(
                    "MOC.wrongMachine", cu.getMachName()
                ));
                System.out.println(Messages.getString("MOC.aborted"));
                System.exit(5);
            }

            // Error management
            ProblemReporter prp = new ProblemReporter(cu);
            ProblemRequestor prq = new ProblemRequestor(true);
            System.out.println("Compiling " + cu.getFileName());

            // Start compilation
            MOC compilator = new MOC(prp);
            prq.beginReporting();
            compilator.set_source(cu);
            compilator.set_eval(true);
            compilator.compile(cu);

            // Handle errors
            for (IProblem problem : prp.getAllProblems()) {
                prq.acceptProblem(problem);
            }
            prq.endReporting();

            int nbErrors = prq.getFatal();
            int nbWarnings = prp.getAllProblems().size() - nbErrors;

            if (nbErrors > 0) {
                if (nbWarnings > 0) {
                    System.out.println(Messages.getString(
                        "MOC.errorAndWarnings", nbErrors, nbWarnings
                    ));
                }
                else {
                    System.out.println(Messages.getString("MOC.errors", nbErrors));
                }
                System.out.println(Messages.getString("MOC.aborted"));
                System.exit(2);
            }
            else if (nbWarnings > 0) {
                System.out.println(Messages.getString("MOC.warnings", nbWarnings));
                System.out.println(Messages.getString("MOC.successful"));
                System.exit(1);
            }
            else {
                System.out.println(Messages.getString("MOC.successful"));
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

