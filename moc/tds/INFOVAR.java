package moc.tds;

import moc.gc.Location;
import moc.type.DTYPE;

/**
 * Cette classe décrit une variable locale : adresse et type
 * 
 * @author marcel
 * 
 */
public class INFOVAR implements INFO {
    /**
     * Le type de la variable.
     */
    protected DTYPE type;

    /**
     * Represente un emplacement memoire : depend de la machine.
     */
    protected Location empl;

    public Location getEmpl() {
        return empl;
    }

    public int getSize() {
        return getType().getSize();
    }

    public DTYPE getType() {
        return type;
    }

    /**
     * Une variable a un type et un emplacement pour sa valeur.
     */
    public INFOVAR(DTYPE t, Location e) {
        type = t;
        empl = e;
    }

    @Override
    public String toString() {
        return "INFOVAR [type=" + type.getName() + ", empl=" + empl + "]";
    }
}

