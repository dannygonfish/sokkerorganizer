package so.gui.render;

import so.data.CountryDataUnit;
import javax.swing.DefaultListCellRenderer;
import java.awt.Component;

public class CountryListCellRenderer extends DefaultListCellRenderer {
    public CountryListCellRenderer() {
        super();
        //setHorizontalAlignment(javax.swing.JLabel.CENTER);
    }

    public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value instanceof CountryDataUnit) {
            CountryDataUnit cdu = (CountryDataUnit)value;
            super.getListCellRendererComponent(list, cdu.getFlag(), index, isSelected, cellHasFocus);
            setText(cdu.getName());
        }
        return this;
    }

}
