package so.gui.render;

import static so.Constants.*;
import static so.Constants.Positions.*;
import so.gui.MainFrame;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Color;


/* renders columns 34-Squad and 33-Pilm of table in SquadPanel, and SquadEditorCombo JComboBox */
public  class SquadIconCellRenderer extends DefaultTableCellRenderer implements javax.swing.ListCellRenderer {
    private javax.swing.DefaultListCellRenderer listRenderer;

    public SquadIconCellRenderer() {
        super();
        setHorizontalAlignment(javax.swing.JLabel.CENTER);
        setForeground(Color.WHITE);
        setFont(new java.awt.Font("Times", java.awt.Font.BOLD, 12));
        listRenderer = new javax.swing.DefaultListCellRenderer();
        listRenderer.setFont(this.getFont());
    }
    public void setValue(Object value) {
        setIcon( getIcon(value) );
    }
    private ImageIcon getIcon(Object value) {
        if (value instanceof Character) {
            char c = ((Character)value).charValue();
            switch (c) {
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H':
                return MainFrame.getImageIcon(DIRNAME_IMAGES + "/squad" + c + ".gif");
            case ' ':
            default:
                return MainFrame.getImageIcon(FILENAME_IMG_BLANK);
            }
        }
        else if (value instanceof Integer) {
            switch (((Integer)value).intValue()) {
            case TRAINING_GK:
            case GK:
                return MainFrame.getImageIcon(FILENAME_IMG_SHIRT_GK);
            case TRAINING_DEF:
            case DEF:
            case WB:
            case CB:
            case SW:
                return MainFrame.getImageIcon(FILENAME_IMG_SHIRT_DEF);
            case TRAINING_MID:
            case MID:
            case DM:
            case CM:
            case AM:
            case WM:
                return MainFrame.getImageIcon(FILENAME_IMG_SHIRT_MID);
            case TRAINING_ATT:
            case ATT:
            case FW:
            case ST:
                return MainFrame.getImageIcon(FILENAME_IMG_SHIRT_ATT);
            case NO_POSITION:
            default:
                return MainFrame.getImageIcon(FILENAME_IMG_BLANK);
            }
        }
        else return null;
    }

    public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        Component comp = listRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        listRenderer.setIcon( getIcon(value) );
        listRenderer.setText("");
        return comp;
    }
}
