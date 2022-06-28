package so.gui.flagsplugin;
/**
 * FlagLabel.java
 *
 * @author Daniel González Fisher
 */

import so.data.CountryDataUnit;


public class FlagLabel extends javax.swing.JLabel {
    private CountryDataUnit cdu;

    public FlagLabel(CountryDataUnit cdu) {
        super();
        this.cdu = cdu;
        this.setIcon(cdu.getFlag());
        this.setToolTipText(cdu.getName());
        this.setBorder( javax.swing.BorderFactory.createRaisedBevelBorder() );
    }

    public CountryDataUnit getCountryDataUnit()  { return cdu; }
    public String getFlagName()        { return cdu.getName(); }
    public int getFlagId()             { return cdu.getId(); }


}
