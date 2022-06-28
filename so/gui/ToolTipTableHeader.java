package so.gui;

import javax.swing.table.*;


public class ToolTipTableHeader extends JTableHeader {

    public ToolTipTableHeader(TableColumnModel tcm) {
        super(tcm);
    }

    public String getToolTipText(java.awt.event.MouseEvent e) {
        java.awt.Point p = e.getPoint();
        int index = columnModel.getColumnIndexAtX(p.x);
        if (index < 0) return null;
        int realIndex = columnModel.getColumn(index).getModelIndex();
        if (super.table == null) return null;
        TableModel tm = super.table.getModel();
        if (tm == null) return null;
        return tm.getColumnName(realIndex);
    }

}
