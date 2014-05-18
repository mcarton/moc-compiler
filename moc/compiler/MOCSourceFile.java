package moc.compiler;

import java.util.ArrayList;
import mg.egg.eggc.runtime.libjava.SourceUnit;
import moc.gc.*;

/**
 * Describes a MOC compilation unit.
 */
public class MOCSourceFile extends SourceUnit {
    // Mandatory for a SourceUnit: filename
    private String fileName;
    // target machine
    private AbstractMachine machine;
    private String machName = "tam";
    private int verbosity = 0;
    private ArrayList<String> warnings = new ArrayList<String>();

    public MOCSourceFile(String[] args) throws MOCException {
        super(args[args.length-1]);

        // file name
        fileName = args[args.length-1];

        // other arguments
        analyze(args);
    }

    /**
     * Print available options.
     */
    private void usage(String a) throws MOCException {
        throw new MOCException(Messages.getString("MOC.usage", a));
    }

    /**
     * Analyse supplementary arguments of the compiler.
     */
    public void analyze(String[] args) throws MOCException {
        int argc = args.length;
        for (int i = 0; i < argc -1; ++i) {
            if (i+1 < argc) {
                if (args[i].equals("-m")) {
                    machName = args[++i];
                }
                else if (args[i].equals("-v")) {
                    verbosity = Integer.parseInt(args[++i]);
                }
                else if (args[i].equals("-w")) {
                    while (i+1 < argc && !args[i+1].startsWith("-")) {
                        warnings.add(args[++i]);
                    }
                }
            }
            else {
                usage(args[i]);
            }
        }

        setMachine(machName);
    }

    /**
     * Determines and creates the target machine.
     */
    private void setMachine(String mach) {
        machName = mach;
        if (machName.equals("tam")) {
            machine = new moc.gc.tam.Machine(verbosity, warnings);
        }
        else if (machName.equals("llvm")) {
            machine = new moc.gc.llvm.Machine(verbosity, warnings);
        }
    }

    public AbstractMachine getMachine() {
        return machine;
    }

    public String getMachName() {
        return machName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getVerbosity() {
        return verbosity;
    }
}

