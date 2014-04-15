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
    private String machName;

    public MOCSourceFile(String[] args) throws MOCException {
        super(args[0]);
        // other arguments?
        analyze(args);
    }

    /**
     * Print available options
     *
     * @param a
     * @throws MOCException
     */
    private void usage(String a) throws MOCException {
        throw new MOCException("Incorrect option: " + a + ". "
                               + Messages.getString("MOC.usage"));
    }

    /**
     * Analyse supplementary arguments of the compiler
     *
     * @param args
     * @throws MOCException
     */
    public void analyze(String[] args) throws MOCException {
        int argc = args.length;
        // file name
        fileName = args[0];
        // target machine?
        if (argc == 1) {
            // default: tam machine
            setMachine("tam");
        }
        else {
            // machine name
            for (int i = 1; i < argc; i++) {
                String a = args[i];
                if ("-m".equals(a)) {
                    //$NON-NLS-1$
                    if (i + 1 < argc) {
                        i++;
                        setMachine(args[i]);
                    }
                    else {
                        usage(a);
                    }
                }
                else {
                    usage(a);
                }
            }
        }
    }

    /**
     * Determines and creates the target machine
     *
     * @param mach
     */
    private void setMachine(String mach) {
        // System.err.println("mach " + mach);
        machName = mach;
        if ("tam".equals(mach)) {
            machine = new MTAM();
        }
        else {
            // TODO if the machine is not TAM
            // machine = new ???();
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

}
