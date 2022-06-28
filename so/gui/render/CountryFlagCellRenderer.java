package so.gui.render;

import javax.swing.ImageIcon;


public class CountryFlagCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

    public CountryFlagCellRenderer() {
        super();
        setHorizontalAlignment(javax.swing.JLabel.CENTER);
    }

    public void setValue(Object value) {
        setIcon((value instanceof ImageIcon) ? (ImageIcon)value : null);
        setToolTipText( ((ImageIcon)value).getDescription() );
    }

}
