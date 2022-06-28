package so.gui.render;

import static so.Constants.*;
import so.data.DataPair;
import so.config.Options;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;

public class DataPairCellRenderer extends JPanel implements TableCellRenderer {

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1); 
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    private JLabel label1;
    private JLabel label2;
    private Options options;
    private NumberFormat intFormat;
    private NumberFormat decFormat;

    public DataPairCellRenderer(Options opt) {
        super(new java.awt.GridLayout(1,2));
        options = opt;
        intFormat = NumberFormat.getIntegerInstance();
        decFormat = NumberFormat.getInstance();
        decFormat.setMaximumFractionDigits(2);

        setBorder(getNoFocusBorder());
        label1 = new JLabel("", SwingConstants.TRAILING);
        label2 = new JLabel("", SwingConstants.TRAILING);
        label1.setOpaque(false);
        label2.setOpaque(false);
        add(label1);
        add(label2);
        setBorder(getNoFocusBorder());
    }

    protected void setValue(Object value) {
        DataPair dp = null;
        label1.setIcon(null);
        label2.setIcon(null);
        label1.setText(null);
        label2.setText(null);
        setToolTipText(null);
        label1.setForeground(getForeground());
        label2.setForeground(getForeground());
        label2.setHorizontalTextPosition(SwingConstants.TRAILING);
        try {
            dp = (DataPair)value;
        } catch (ClassCastException cce) {
            return;
        }
        if (dp == null) return;
        int a = dp.getValue();
        int b = dp.getSecondValue();

        switch (dp.getType()) {
        case DATA_COMPARABLE_CURRENCY:
            a = (int)(a * options.getCurrencyConversionRate());
            b = (int)(b * options.getCurrencyConversionRate());
        case DATA_COMPARABLE_SKILL:
            if (dp.getType()==DATA_COMPARABLE_SKILL) {
                setToolTipText( so.gui.MainFrame.getSkillLevelName(a) );
                if ( a <= options.getLowSkill() ) label1.setForeground(Color.GRAY);
                else if ( a >= options.getHighSkill() ) label1.setFont( label1.getFont().deriveFont(Font.BOLD) );
            }
        case DATA_COMPARABLE_NUMBER:
            label1.setHorizontalAlignment(SwingConstants.TRAILING);
            label2.setHorizontalAlignment(SwingConstants.TRAILING);
            label1.setText( formatNumber(a, dp.getType()) );
            if (a>b) {
                label2.setForeground(Color.GREEN.darker());
                label2.setText("+" + formatNumber(a-b, dp.getType()) );
                label2.setFont( label2.getFont().deriveFont(Font.BOLD) );
            }
            else if (a<b) {
                label2.setForeground(Color.RED);
                label2.setText( formatNumber(a-b, dp.getType()) );
                label2.setFont( label2.getFont().deriveFont(Font.BOLD) );
            }
            break;
        case DATA_COMPARABLE_NAMED_SKILL:
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            if (a>=0) {
		if (a>17) a = 17;
                double exactLevel = dp.getDecimalValue();
                String _text = formatNumber(a, dp.getType());
                if ( a <= options.getLowSkill() ) label1.setForeground(Color.RED);
                else if ( a >= options.getHighSkill() ) label1.setForeground(Color.BLUE);
                if (a>b) {
                    label2.setForeground(Color.GREEN.darker());
                    label2.setText("+" + formatNumber(a-b, DATA_COMPARABLE_NUMBER) );
                    label2.setFont( label2.getFont().deriveFont(Font.BOLD) );
                }
                if (exactLevel>0) _text = _text + " (" + decFormat.format(exactLevel) + ")";
                else if (exactLevel == JR_GUESSED_SKILL) {
                    _text = "\u2264 " + _text; // less or equal
                    label1.setForeground(Color.GRAY);
                    label2.setText(null);
                }
                else if (exactLevel == JR_ESTIMATED_SKILL) {
                    _text = "\u2248 " + _text; // similar
                    label1.setForeground(Color.DARK_GRAY);
                }
                // else ==0 do nothing
                label1.setText( _text );
            }
            break;
        case DATA_NAME2:
            label2.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_NT_PLAYER));
            label2.setHorizontalTextPosition(SwingConstants.LEADING);
        case DATA_NAME:
            label1.setHorizontalAlignment(SwingConstants.LEADING);
            label2.setHorizontalAlignment(SwingConstants.LEADING);
            label1.setText(dp.getFirstName());
            label2.setText(dp.getSecondName());
            if (dp.getForegroundColor() != null) {
                label1.setForeground(dp.getForegroundColor());
                label2.setForeground(dp.getForegroundColor());
            }
            break;
        case DATA_STATUS:
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            b = (int)Math.ceil(dp.getDecimalValue());
            switch (a) { // how many yellow cards, 3=red
            case 0:
                break;
            case 1:
                label1.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_YELLOWCARD));
                break;
            case 2:
                label1.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_YELLOWCARD2));
                break;
            case 3:
            default:
                label1.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_REDCARD));
                break;
            }
            if (b>0) {
                label2.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_INJURY));
                label2.setText(Integer.toString(b));
            }
            break;
        default:
        }
    }

    private String formatNumber(int n, int type) {
        switch (type) {
        case DATA_COMPARABLE_CURRENCY:
            return intFormat.format(n) + " " + options.getCurrencySymbol();
        case DATA_COMPARABLE_NAMED_SKILL:
            return so.gui.MainFrame.getSkillLevelName(n);
        case DATA_COMPARABLE_SKILL:
        case DATA_COMPARABLE_NUMBER:
        default:
            return Integer.toString(n);
        }
    }

    private static Border getNoFocusBorder() {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        } else {
            return noFocusBorder;
        }
    }

    /* interface javax.swing.table.TableCellRenderer */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }
        else {
            super.setForeground(table.getForeground());
            super.setBackground(table.getBackground());
        }
        label1.setFont(table.getFont());
        label2.setFont(table.getFont());

        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            setBorder(border);
            if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null) {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null) {
                    super.setBackground(col);
                }
            }
        } else {
            setBorder(getNoFocusBorder());
        }

        setValue(value);

        if (value == null) return this;
        if (((DataPair)value).getBackgroundColor() != null) {
            super.setBackground(((DataPair)value).getBackgroundColor());
            if (isSelected) super.setBackground(getBackground().darker());
        }

        return this;
    }

}
