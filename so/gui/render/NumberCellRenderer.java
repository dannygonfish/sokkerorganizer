package so.gui.render;

import so.config.Options;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.NumberFormat;
import java.awt.Color;

public class NumberCellRenderer extends DefaultTableCellRenderer {
    private NumberFormat numberFormat;
    private Options options;
    private Color defaultFgColor;

    public NumberCellRenderer(Options opt) {
        super();
        options = opt;
        setHorizontalAlignment(javax.swing.JLabel.CENTER);
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        defaultFgColor = null;
    }
    public void setValue(Object value) {
        if (defaultFgColor == null) defaultFgColor = getForeground();
        if (value instanceof Short) setText( value.toString() );
        else if (value instanceof Float) { /* average weeks/pop in JuniorSchoolPanel */
            float v = ((Float)value).floatValue();
            if (v >= 0) setText( numberFormat.format( v ) );
            else setText( "\u2265 " + numberFormat.format( -v ) );
            if (v<=0) {
                setForeground(Color.GRAY);
                return;
            }
        }
        else if (value instanceof Long) setText( numberFormat.format(((Long)value).longValue()*options.getCurrencyConversionRate()) + " " + options.getCurrencySymbol() );
        else if (value instanceof Integer) setText( value.toString() );
        else if (value instanceof Double) setText( numberFormat.format( ((Double)value).doubleValue() ) );
        setForeground(defaultFgColor);
    }

//     public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected,
//                                                             boolean hasFocus, int row, column) {
//         super(table, value, boolean, isSelected, hasFocus, row, column);
//     }

}
