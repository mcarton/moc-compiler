package moc.compiler;

import mg.egg.eggc.runtime.libjava.SourceUnit;
import moc.gc.AbstractMachine;
import moc.gc.MTAM;

/**
 * Describes a MOC compilation unit
 */
public class MOCSourceFile extends SourceUnit {
    // Mandatory for a SourceUnit: filename
    private String fileName;
    // target machine
    private AbstractMachine machine;
    private String machName = "tam";
    private int verbosity = 0;

    public MOCSourceFile(String[] args) throws MOCException {
        super(args[0]);
        // other arguments?
        analyze(args);
    }

    /**
     * Print available options.
     */
    private void usage(String a) throws MOCException {
        throw new MOCException("Incorrect option: " + a + ". "
                               + Messages.getString("MOC.usage"));
    }

    /**
     * Analyse supplementary arguments of the compiler.
     */
    public void analyze(String[] args) throws MOCException {
        // file name
        fileName = args[0];

        int argc = args.length;
        for(int i = 0; i < argc; ++i) {
            if(args[i].equals("-m")) {
                if(i+1 < argc) {
                    machName = args[++i];
                }
                else {
                    usage(args[i]);
                }
            }
            else if(args[i].equals("-v")) {
                verbosity = i+1 < argc ? verbosity = Integer.parseInt(args[++i]) : 1;
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
            machine = new MTAM(verbosity);
        }
        else if(machName.equals("llvm")) {
            // TODO:llvm
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
