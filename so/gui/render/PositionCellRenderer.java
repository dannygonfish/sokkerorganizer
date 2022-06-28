package so.gui.render;

import static so.Constants.*;
import static so.Constants.Colors.*;
import static so.Constants.Positions.*;
import so.data.DataPair;
import so.text.LabelManager;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;

public class PositionCellRenderer extends DefaultTableCellRenderer implements javax.swing.ListCellRenderer {

    private LabelManager labelManager;
    private Font posFont;
    private NumberFormat decFormat;
    private JTable tableForList;

    public PositionCellRenderer(LabelManager lm) {
        super();
        labelManager = lm;
        setHorizontalAlignment( CENTER );
        //posFont = new Font("Arial Black", Font.BOLD, 12);
        posFont = new Font("Serif", Font.BOLD, 12);
        decFormat = NumberFormat.getInstance();
        decFormat.setMaximumFractionDigits(1);
        decFormat.setMinimumFractionDigits(1);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        DataPair dp = null;
        int pos;
        int type;
        if (value instanceof DataPair) {
            dp = (DataPair)value;
            pos = dp.getValue();
            type = dp.getType();
        }
        else if (value instanceof Integer) {
            pos = ((Integer)value).intValue();
            type = DATA_POSITION;
        }
        else return comp;

        switch (type) {
        case DATA_POSITION:
            int pure_pos = pos & ~(P_LEFT | P_RIGHT | RESERVE);
            if (pos == NO_POSITION) {
                setText(" ");
                setToolTipText(null);
                break;
            }
            setText( labelManager.getPositionShortName( pos ) );
            setToolTipText( labelManager.getPositionLongName( pos ) );
            setFont( posFont );
            if (pure_pos>=GK && pure_pos<DEF) setForeground( CELLCOLOR_GK );
            else if (pure_pos>=DEF && pure_pos<MID) setForeground( CELLCOLOR_DEF );
            else if (pure_pos>=MID && pure_pos<ATT) setForeground( CELLCOLOR_MID );
            else if (pure_pos>=ATT) setForeground( CELLCOLOR_ATT );
            else {
                if (isSelected) setForeground(table.getSelectionForeground());
                else setForeground( table.getForeground() );
            }
            break;
        case DATA_RATING:
            //setToolTipText(null);
            if (pos==dp.getSecondValue()) {
                if (pos>=GK && pos<DEF) setForeground( CELLCOLOR_GK );
                else if (pos>=DEF && pos<MID) setForeground( CELLCOLOR_DEF );
                else if (pos>=MID && pos<ATT) setForeground( CELLCOLOR_MID );
                else if (pos>=ATT) setForeground( CELLCOLOR_ATT );
            }
            else {
                if (isSelected) setForeground(table.getSelectionForeground());
                else setForeground( table.getForeground() );
            }
            setText( decFormat.format( dp.getDecimalValue() ) );
            break;
        default:
        }

        if (dp!=null && dp.getBackgroundColor() != null) {
            setBackground(dp.getBackgroundColor());
            if (isSelected) setBackground(getBackground().darker());
        }
        else {
            if (isSelected) setBackground(table.getSelectionBackground());
            else setBackground(table.getBackground());
        }
        return comp;
    }

    public void setTable(JTable table) { tableForList = table; }

    public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        return getTableCellRendererComponent(tableForList, value, isSelected, cellHasFocus, index, 0);
    }

}
