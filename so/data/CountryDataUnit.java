package so.data;

import javax.swing.ImageIcon;
import java.text.Collator;

public class CountryDataUnit implements java.io.Serializable, Comparable<CountryDataUnit> {
    private static final long serialVersionUID = 5736910147667304910L;

    private static Collator COLLATOR = null;

    private short id;
    private String name;
    transient private ImageIcon flag = null;

    public CountryDataUnit(int id) {
        this.id = (short)id;
        flag = so.gui.MainFrame.getFlagIcon(id);
        name = flag.getDescription();
        if (COLLATOR == null) {
            COLLATOR = Collator.getInstance();
            COLLATOR.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            COLLATOR.setStrength(Collator.SECONDARY);
        }
    }

    public int getId()       { return id; }
    public String getName()    { return name; }
    public ImageIcon getFlag() {
        if (flag == null) flag = so.gui.MainFrame.getFlagIcon(id);
        return flag;
    }

    public String toString() { return name; }

    /* Interface Comparable */
    public int hashCode() {
        return id;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof CountryDataUnit)) return false;
        if (id == ((CountryDataUnit)obj).id) return true;
        return false;
    }

    public int compareTo(CountryDataUnit obj) throws ClassCastException {
        return COLLATOR.compare(this.name, obj.name);
    }

}
