package so.gui.flagsplugin;

import so.data.CountryDataUnit;


public class FlagRenderer extends javax.swing.DefaultListCellRenderer {

    private FlagGridPanel flagGrid;

    public FlagRenderer(FlagGridPanel fgp) {
        super();
        setOpaque(true);
        setHorizontalAlignment(LEADING);
        setVerticalAlignment(CENTER);
        flagGrid = fgp;
        setPreferredSize(new java.awt.Dimension(200,20));
    }

    public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value instanceof CountryDataUnit) {
            CountryDataUnit cdu = (CountryDataUnit)value;
            super.getListCellRendererComponent(list, cdu.getFlag(), index, isSelected, cellHasFocus);
            setText(cdu.getName());

            if (flagGrid != null) {
                if (flagGrid.contains(value)) {
                    setForeground(java.awt.Color.LIGHT_GRAY);
                    setFont(getFont().deriveFont(java.awt.Font.ITALIC));
                }
            }
            setToolTipText("countryID = " + cdu.getId());


        }
        return this;
    }

}
