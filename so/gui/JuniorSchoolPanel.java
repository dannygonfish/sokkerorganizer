package so.gui;

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.data.*;
import so.gui.render.*;
import so.text.LabelManager;
import so.config.Options;
import so.util.TableSorter;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.table.*;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

public class JuniorSchoolPanel extends JPanel implements MouseListener, ActionListener, ListSelectionListener {
    private static final String CMD_DELETE_JR = "del";

    private JTable table;
    private JTable oldTable;
    private JuniorTableModel tableModel;
    private JuniorTableModel oldTableModel;
    private DataPairCellRenderer dpCellRenderer;
    private NumberCellRenderer numberCellRenderer;
    private PositionCellRenderer positionCellRenderer;
    private DefaultTableCellRenderer ageCellRenderer;
    private LabelManager labelManager;
    private Options options;
    private JuniorSchool school;
    private GraphPanel graphPanel;
    private TableSorter tableSorter;

    private GridBagConstraints gbc;

    //public JuniorSchoolPanel() {
    public JuniorSchoolPanel(LabelManager lm, Options opt, JuniorSchool sch) {
        super(new GridBagLayout());
        labelManager = lm;
        options = opt;
        school = sch;
        gbc = new GridBagConstraints();
        dpCellRenderer = new DataPairCellRenderer(options);
        numberCellRenderer = new NumberCellRenderer(options);
        positionCellRenderer = new PositionCellRenderer(lm);
        ageCellRenderer = new DefaultTableCellRenderer();
        ageCellRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        tableModel = new JuniorTableModel();
        tableSorter = new TableSorter(tableModel);
        table = new JTable();
        table.setAutoCreateColumnsFromModel(false);
        table.setModel(tableSorter);
        initColumns(table);
        table.setTableHeader(new JuniorTableHeader(table.getColumnModel()));
        tableSorter.setTableHeader(table.getTableHeader());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(DataPair.class, dpCellRenderer);
        table.setDefaultRenderer(Short.class, numberCellRenderer);
        table.setDefaultRenderer(Float.class, numberCellRenderer);
        table.setDefaultRenderer(Long.class, numberCellRenderer);
        table.setDefaultRenderer(Integer.class, positionCellRenderer);
        table.setDefaultRenderer(String.class, ageCellRenderer);

        oldTableModel = new JuniorTableModel(false);
        oldTable = new JTable();
        oldTable.setAutoCreateColumnsFromModel(false);
        oldTable.setModel(oldTableModel);
        initColumns(oldTable);
        oldTable.setTableHeader(new JuniorTableHeader(oldTable.getColumnModel()));
        oldTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        oldTable.setDefaultRenderer(DataPair.class, dpCellRenderer);
        oldTable.setDefaultRenderer(Short.class, numberCellRenderer);
        oldTable.setDefaultRenderer(Float.class, numberCellRenderer);
        oldTable.setDefaultRenderer(Long.class, numberCellRenderer);
        oldTable.setDefaultRenderer(Integer.class, positionCellRenderer);
        oldTable.setDefaultRenderer(String.class, ageCellRenderer);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem(labelManager.getLabel(TXT_DELETE_FOREVER));
        menuItem.setActionCommand(CMD_DELETE_JR);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        oldTable.setComponentPopupMenu(popup);

        graphPanel = new GraphPanel(new String[] { labelManager.getLabel(TXT_CH_SKILL) } );
        graphPanel.setColors(new Color[] { Color.GRAY } );
        graphPanel.setToolTips(labelManager.getLabel(TXT_CH_WEEKS), labelManager.getLabel(TXT_CH_SKILL));;
        graphPanel.setWeeksInGraph(35);
        graphPanel.useGraphPopup(null, true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(table));
        splitPane.setBottomComponent(new JScrollPane(oldTable));
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(1.0d);
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane2.setTopComponent(splitPane);
        splitPane2.setBottomComponent(graphPanel);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setResizeWeight(0.5);
        splitPane2.setDividerLocation(1.0d);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(splitPane2, gbc);

        table.addMouseListener(this);
        oldTable.addMouseListener(this);
        table.addMouseListener(this);
        oldTable.addMouseListener(this);
        table.getSelectionModel().addListSelectionListener(this);
        oldTable.getSelectionModel().addListSelectionListener(this);
    }

    private void initColumns(JTable t) {
        if (t==null) return;
        // Remove any current columns
        TableColumnModel cm = t.getColumnModel();
        while (cm.getColumnCount() > 0) {
            cm.removeColumn(cm.getColumn(0));
        }
        // Create new columns from the options info
        ArrayList<TableColumnData> jtc = options.getJuniorTableColumns();
        TableColumn tc = null;
        for (TableColumnData data : jtc) {
            tc = new TableColumn( data.getModelIndex(), data.getWidth() );
            tc.setIdentifier(data);
            if (data.isActive()) t.addColumn( tc );
        }
        // fix por si faltan columnas
//         //% BORRAR EN SIGUIENTE VERSION
        if (cm.getColumnCount() < JUNIORCOLUMNS_COUNT ) {
            System.out.println("THIS IS NOT AN ERROR.");
            System.out.println("current Junior School Column count "+cm.getColumnCount());
            System.out.println("Updating Junior School Panel Column model to version " + so.So.getVersion());
            for (int i=cm.getColumnCount() ; i<JUNIORCOLUMNS_COUNT; i++) {
                System.out.println("adding new column "+i);
                TableColumnData data = new TableColumnData(i, 110);
                tc = new TableColumn( data.getModelIndex(), data.getWidth() );
                tc.setIdentifier(data);
                t.addColumn( tc );
                jtc.add( data );
            }
            options.setJuniorTableColumns(jtc);
        }
        //% BORRAR EN SIGUIENTE VERSION
    }
    public void storeColumnSettings() {
        if (table == null) return;
        TableColumnModel cm = table.getColumnModel();
        if (cm == null) return;
        ArrayList<TableColumnData> jtc = options.getJuniorTableColumns();
        //int columnsInView = cm.getColumnCount(); // = table.getColumnCount(); 

        TableColumnData [] tempArray = new TableColumnData[jtc.size()];
        int notUsedIndex = jtc.size() - 1;
        int indexInModel = 0;
        int indexInView = 0;
        int width = 75;
        for (TableColumnData data : jtc) {
            try {
                indexInView = cm.getColumnIndex(data);
            } catch (IllegalArgumentException iae) {
                //no está
                data.setActive(false);
                tempArray[notUsedIndex] = data;
                notUsedIndex--;
                continue;
            }
            indexInModel = table.convertColumnIndexToModel(indexInView);
            width = cm.getColumn(indexInView).getWidth();
            data.setWidth(width);
            data.setActive(true);
            tempArray[indexInView] = data;
        }
        ArrayList<TableColumnData> newJtc = new ArrayList<TableColumnData>(jtc.size());
        for (int i=0; i<tempArray.length; i++) {
            newJtc.add( tempArray[i] );
        }
        options.setJuniorTableColumns(newJtc);
    }

    public void refreshData() {
        refreshTable();
        graphPanel.setData(null);
        //repaint();
    }
    public void refreshTable() {
        tableModel.fireTableDataChanged();
        oldTableModel.fireTableDataChanged();
    }
    public void rebuildTable() {
        tableModel.fireTableStructureChanged();
        oldTableModel.fireTableStructureChanged();
    }

    /* interface ActionListener */
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        if (command.equals(CMD_DELETE_JR)) {
            int row = oldTable.getSelectedRow();
            if (row==-1) return;
            JuniorProfile jp = school.getFormerJuniorsList().get( row );
            int ok = JOptionPane.showConfirmDialog(this, jp.getFullName(), labelManager.getLabel(TXT_CONFIRM_DELETE),
                                                   JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                school.deleteJunior(jp);
                refreshData();
            }
        }
    }
    /* interface ListSelectionListener */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = ((ListSelectionModel)e.getSource()).getLeadSelectionIndex();
        if (row==-1) return;
        JuniorProfile jp = null;
        if (e.getSource().equals(table.getSelectionModel())) jp = school.getJuniorsList().get(tableSorter.modelIndex(row));
        else jp = school.getFormerJuniorsList().get( row );
        graphPanel.setData(jp.getGraphicableData());
    }

    /* interface MouseListener */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1 && e.getID()==MouseEvent.MOUSE_CLICKED && e.getClickCount()==1) {
            if (e.getSource() instanceof JTable) {
                int row = ((JTable)e.getSource()).getSelectionModel().getLeadSelectionIndex();
                if (row==-1) return;
                JuniorProfile jp = null;
                if (e.getSource().equals(table)) jp = school.getJuniorsList().get( tableSorter.modelIndex(row) );
                else jp = school.getFormerJuniorsList().get( row );
                graphPanel.setData(jp.getGraphicableData());
            }
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON3) {
            if (e.getSource() instanceof JTable) {
                int row = ((JTable)e.getSource()).rowAtPoint(e.getPoint());
                ((JTable)e.getSource()).getSelectionModel().setSelectionInterval(row, row);
            }
        }
    }
    public void mouseReleased(MouseEvent e) { }


    /* ####################################################################### */
    protected class JuniorTableModel extends AbstractTableModel {
        boolean activeJuniors;

        public JuniorTableModel() {
            this(true);
        }
        public JuniorTableModel(boolean activeJuniors) {
            super();
            this.activeJuniors = activeJuniors;
        }

        protected java.util.List<JuniorProfile> getJuniorsList() {
            if (activeJuniors) return school.getJuniorsList();
            else return school.getFormerJuniorsList();
        }

        public int getRowCount() {
            if (school==null) return 0;
            return getJuniorsList().size();
        }
        public int getColumnCount() { return JUNIORCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            return getJuniorsList().get(row).getData(column);
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            //return columnNames[col];
            switch(col) {
            case  0: return labelManager.getLabel(TXT_CH_NAME);
            case  1: return labelManager.getLabel(TXT_CH_WEEKS);
            case  2: return labelManager.getLabel(TXT_CH_SKILL);
            case  3: return labelManager.getLabel(TXT_CH_SKILLUP_COUNT);
            case  4: return labelManager.getLabel(TXT_CH_AVG_WEEKS);
            case  5: return labelManager.getLabel(TXT_CH_PROJECTED_LEVEL);
            case  6: return labelManager.getLabel(TXT_CH_INITIAL_WEEKS);
            case  7: return labelManager.getLabel(TXT_CH_INITIAL_SKILL);
            case  8: return labelManager.getLabel(TXT_CH_WEEKS_SINCE_POP);
            case  9: return labelManager.getLabel(TXT_CH_MONEY_SPENT);
            case  10: return labelManager.getLabel(TXT_CH_POSITION);
            case  11: return labelManager.getLabel(TXT_CH_AGE);
            default:
                return "";
            }
        }
    }
    /* ####################################################################### */
    protected class JuniorTableHeader extends JTableHeader {

        public JuniorTableHeader(TableColumnModel tcm) {
            super(tcm);
        }

        public String getToolTipText(java.awt.event.MouseEvent e) {
            String tip = null;
            java.awt.Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            int realIndex = columnModel.getColumn(index).getModelIndex();
            switch(realIndex) {
            case  2: return labelManager.getLabel(TT+TXT_CH_SKILL);
            case  3: return labelManager.getLabel(TT+TXT_CH_SKILLUP_COUNT);
            case  4: return labelManager.getLabel(TT+TXT_CH_AVG_WEEKS);
            case  5: return labelManager.getLabel(TT+TXT_CH_PROJECTED_LEVEL);
            case  6: return labelManager.getLabel(TT+TXT_CH_INITIAL_WEEKS);
            case  7: return labelManager.getLabel(TT+TXT_CH_INITIAL_SKILL);
            case  8: return labelManager.getLabel(TT+TXT_CH_WEEKS_SINCE_POP);
            case  9: return labelManager.getLabel(TT+TXT_CH_MONEY_SPENT);
            default:
                return null;
            }
        }
    }

}
