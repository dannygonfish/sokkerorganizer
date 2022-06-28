package so.gui;

import static so.Constants.*;
import static so.Constants.Labels.*;
import static so.Constants.Positions.*;
import so.data.*;
import so.gui.render.*;
import so.text.LabelManager;
import so.config.Options;
import so.util.TableSorter;
import so.util.FormattedDateHolder;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultCellEditor;
import javax.swing.table.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.ArrayList;
import java.util.Vector;
import java.util.SortedSet;
import java.text.NumberFormat;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.toedter.calendar.JCalendar;


public class SquadPanel extends JPanel implements PropertyChangeListener, ItemListener, MouseListener, ActionListener {
    private static final String CMD_PLAYER_REPORT     = "pr";
    private static final String CMD_MANAGER_NOTES     = "mn";

    private JPanel summaryPanel;
    private Box dateChooserPanel;
    private JTable squadTable;
    private JTable rowHeader;
    private PlayerTableModel tableModel;
    private TableSorter tableSorter;
//     private DataPairCellRenderer cellRenderer;
//     private PositionCellRenderer positionRenderer;
//     private PositionCellRenderer ratingRenderer;
    private LabelManager labelManager;
    private Options options;
    private TeamDetails team;
    private PlayerRoster roster;
    private MatchRepository matchRepo;
    private PlayerStatsPanel playerStatsPanel;
    private JCalendar dateChooserCalendar;
    private JComboBox datesCombo;
    private boolean compareDateIsChanging;

    private SortedSet<Date> allPlayersDates;
    private JLabel lbldiffTotalPlayers, lbldiffNatPlayers, lbldiffFgnPlayers, lbldiffAvgAge, lbldiffAvgForm,
        lbldiffTotalSalary, lbldiffAvgSalary, lbldiffTotalValue, lbldiffAvgValue, lbldiffRank, lbldiffMoney,
        lbldiffFans, lbldiffFanClubMood;

    public SquadPanel(LabelManager lm, Options opt, TeamDetails td, PlayerRoster ros, MatchRepository repo) {
        super(new GridBagLayout());
        labelManager = lm;
        options = opt;
        team = td;
        roster = ros;
        matchRepo = repo;
        compareDateIsChanging = false;
        dateChooserCalendar = new JCalendar();

        allPlayersDates = roster.getAllDates();
        GridBagConstraints gbc = new GridBagConstraints();
        tableModel = new PlayerTableModel();
        tableSorter = new TableSorter(tableModel);
        squadTable = new JTable();
        squadTable.setAutoCreateColumnsFromModel(false);
        squadTable.setModel(tableSorter);
        initColumns(squadTable);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem(labelManager.getLabel(TXT_PLAYER_REPORT));
        menuItem.setActionCommand(CMD_PLAYER_REPORT);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem(labelManager.getLabel(TXT_MANAGER_NOTES));
        menuItem.setActionCommand(CMD_MANAGER_NOTES);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        squadTable.setComponentPopupMenu(popup);

        JTableHeader tableHeader = squadTable.getTableHeader();
        tableSorter.setTableHeader(tableHeader);
        tableHeader.setToolTipText(labelManager.getLabel(TXT_TT_TABLE_HEADER));

        DataPairCellRenderer cellRenderer = new DataPairCellRenderer(options);
        squadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        squadTable.setDefaultRenderer(DataPair.class, cellRenderer);
        squadTable.setDefaultRenderer(ImageIcon.class, new CountryFlagCellRenderer() );
        JScrollPane scrollPane = new JScrollPane(squadTable);

        rowHeader = new JTable();
        rowHeader.setAutoCreateColumnsFromModel(false);
        rowHeader.setModel(tableSorter);
        rowHeader.setDefaultRenderer(DataPair.class, cellRenderer);
        rowHeader.addColumn( new TableColumn( 0, 160 ) );
        rowHeader.getColumnModel().getColumn(0).setResizable(false);
        rowHeader.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rowHeader.getTableHeader().setReorderingAllowed(false);
        rowHeader.setSelectionModel( squadTable.getSelectionModel() );
        squadTable.addMouseListener(this);
        rowHeader.addMouseListener(this);
        rowHeader.setComponentPopupMenu(popup);

        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader() );
        //squadTable.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 100));
        rowHeader.setPreferredScrollableViewportSize(new java.awt.Dimension(rowHeader.getColumnModel().getColumn(0).getWidth(), 100));
        tableSorter.setHeaderLines(2);

        summaryPanel = new JPanel();
        dateChooserPanel = new Box(javax.swing.BoxLayout.Y_AXIS);
        buildSummaryPanel();
        buildDateChooserPanel();

        JPanel topPanel = new JPanel(new GridBagLayout());
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = 1; //GridBagConstraints.RELATIVE;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.ipadx = 15;
        gbc.ipady = 15;
        topPanel.add(dateChooserPanel, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        topPanel.add(summaryPanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        JScrollPane topScroll = new JScrollPane(topPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topScroll);
        splitPane.setBottomComponent(scrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation( topScroll.getPreferredSize().height + 1 );
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(splitPane, gbc);
    }

    private void initColumns(JTable t) {
        if (t==null) return;
        PositionCellRenderer positionRenderer = new PositionCellRenderer(labelManager);
        PositionCellRenderer ratingRenderer = new PositionCellRenderer(labelManager);
        // Remove any current columns
        TableColumnModel cm = t.getColumnModel();
        while (cm.getColumnCount() > 0) cm.removeColumn(cm.getColumn(0));
        // Create new columns from the options info
        ArrayList<TableColumnData> ptc = options.getPlayerTableColumns();
        TableColumn tc = null;
        JComboBox posEditorCombo = new JComboBox();
        posEditorCombo.addItem(new DataPair(DATA_POSITION, NO_POSITION));
        for (int _pos : SELECTABLE_POSITIONS) posEditorCombo.addItem(new DataPair(DATA_POSITION, _pos));
        positionRenderer.setTable(t);
        posEditorCombo.setRenderer(positionRenderer);
        JComboBox squadEditorCombo = new JComboBox(new Character[]{ ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' });
        squadEditorCombo.setMaximumRowCount( squadEditorCombo.getItemCount() );
        SquadIconCellRenderer squadCellRenderer = new SquadIconCellRenderer();
        squadEditorCombo.setRenderer(squadCellRenderer);
        for (TableColumnData data : ptc) {
            tc = new TableColumn( data.getModelIndex(), data.getWidth() );
            tc.setIdentifier(data);
            if (data.getModelIndex()==0) ;
            else if (data.isActive()) t.addColumn( tc );
            switch (data.getModelIndex()) {
            case 3:
                tc.setCellRenderer(positionRenderer);
                tc.setCellEditor(new DefaultCellEditor( posEditorCombo ));
                break;
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                tc.setCellRenderer(ratingRenderer);
                break;
            case 34: // squad
                tc.setCellEditor(new DefaultCellEditor( squadEditorCombo ));
            case 33: // PilM
                tc.setCellRenderer(squadCellRenderer);
                break;
            default:
            }
        }
        // fix por si faltan columnas
        //% BORRAR EN SIGUIENTE VERSION, el +1 es porque la Columna 0 no se muestra en esta tabla, i.e. cm tiene 1 menos
        if (cm.getColumnCount()+1 < PLAYERCOLUMNS_COUNT ) {
            System.out.println("THIS IS NOT AN ERROR.");
            System.out.println("current Squad Column count "+cm.getColumnCount());
            System.out.println("Updating Squad Panel Column model to version " + so.So.getVersion());
            for (int i=cm.getColumnCount()+1 ; i<PLAYERCOLUMNS_COUNT; i++) {
                System.out.println("adding new column "+i);
                TableColumnData data = new TableColumnData(i, 50);
                tc = new TableColumn( data.getModelIndex(), data.getWidth() );
                tc.setIdentifier(data);
                t.addColumn( tc );
                if (i==33) t.moveColumn(i-1,3);
                if (i==34) {
                    tc.setCellRenderer(squadCellRenderer);
                    tc.setCellEditor(new DefaultCellEditor( squadEditorCombo ));
                    t.moveColumn(i-1,4);
                }
                ptc.add( data );
            }
            options.setPlayerTableColumns(ptc);
        }
        //% BORRAR EN SIGUIENTE VERSION
    }

    private void buildSummaryPanel() {
        JPanel p = summaryPanel;
        GridBagConstraints sgbc = new GridBagConstraints();
        GridBagConstraints sgbc2 = new GridBagConstraints();
        GridBagConstraints sgbc3 = new GridBagConstraints();
        p.setLayout(new GridBagLayout());

        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        NumberFormat decFormat = NumberFormat.getInstance();
        decFormat.setMaximumFractionDigits(2);
        int country = team.getCountryId();
        int totalPlayers = roster.getActivePlayerCount();
        int natPlayers = roster.getPlayersByNationalityCount(country);

        int hal = JLabel.LEADING;
        Color colLabel = so.Constants.Colors.COLOR_LIGHT_YELLOW;

        JLabel lblTeamName = createLabel( team.getName(), Color.BLACK, JLabel.CENTER );
        lblTeamName.setForeground(Color.WHITE);
        JLabel lblCountryFlag = new JLabel( so.gui.MainFrame.getFlagIcon(country) );
        JLabel lbltxtTotalPlayers = createLabel( "  " + labelManager.getLabel(TXT_TOTAL_PLAYERS), colLabel, hal );
        JLabel lbltxtNatPlayers = createLabel( "  " + labelManager.getLabel(TXT_NAT_PLAYERS), colLabel, hal );
        JLabel lbltxtFgnPlayers = createLabel( "  " + labelManager.getLabel(TXT_FGN_PLAYERS), colLabel, hal );
        JLabel lbltxtAvgAge = createLabel( "  " + labelManager.getLabel(TXT_AVG_AGE), colLabel, hal );
        JLabel lbltxtAvgForm = createLabel( "  " + labelManager.getLabel(TXT_AVG_FORM), colLabel, hal );
        JLabel lbltxtTotalSalary = createLabel( "  " + labelManager.getLabel(TXT_TOTAL_SALARY), colLabel, hal );
        JLabel lbltxtAvgSalary = createLabel( "  " + labelManager.getLabel(TXT_AVG_SALARY), colLabel, hal );
        JLabel lbltxtTotalValue = createLabel( "  " + labelManager.getLabel(TXT_TOTAL_VALUE), colLabel, hal );
        JLabel lbltxtAvgValue = createLabel( "  " + labelManager.getLabel(TXT_AVG_VALUE), colLabel, hal );
        JLabel lbltxtRank = createLabel( "  " + labelManager.getLabel(TXT_RANK), colLabel, hal );
        JLabel lbltxtMoney = createLabel( "  " + labelManager.getLabel(TXT_MONEY), colLabel, hal );
        JLabel lbltxtFans = createLabel( "  " + labelManager.getLabel(TXT_FANS), colLabel, hal );
        JLabel lbltxtFanClubMood = createLabel( "  " + labelManager.getLabel(TXT_FANCLUBMOOD), colLabel, hal );
        colLabel = Color.WHITE;
        hal = JLabel.TRAILING;
        double ccr = options.getCurrencyConversionRate();
        String cs = options.getCurrencySymbol();
        JLabel lblvarTotalPlayers = createLabel( totalPlayers + "  " , colLabel, hal );
        JLabel lblvarNatPlayers = createLabel( natPlayers + "  " , colLabel, hal );
        JLabel lblvarFgnPlayers = createLabel( (totalPlayers-natPlayers) + "  " , colLabel, hal );
        JLabel lblvarAvgAge = createLabel( decFormat.format(roster.getAverageAge()) + "  " , colLabel, hal );
        JLabel lblvarAvgForm = createLabel( decFormat.format(roster.getAverageForm()) + "  " , colLabel, hal );
        JLabel lblvarTotalSalary = createLabel( intFormat.format( roster.getTotalSalary()*ccr ) + " " + cs + "  " , colLabel, hal );
        JLabel lblvarAvgSalary = createLabel( decFormat.format(roster.getAverageSalary()*ccr) + " " + cs + "  " , colLabel, hal );
        JLabel lblvarTotalValue = createLabel( intFormat.format(roster.getTotalValue()*ccr) + " " + cs + "  " , colLabel, hal );
        JLabel lblvarAvgValue = createLabel( decFormat.format(roster.getAverageValue()*ccr) + " " + cs + "  " , colLabel, hal );
        JLabel lblvarRank = createLabel( decFormat.format(team.getRank()) + "  " , colLabel, hal );
        JLabel lblvarMoney = createLabel( intFormat.format(team.getMoney()*ccr) + " " + cs + "  " , colLabel, hal );
        JLabel lblvarFans = createLabel( team.getFans() + "  " , colLabel, hal );
        JLabel lblvarFanClubMood = createLabel( labelManager.getMoodLevelName(team.getFanClubMood())+"  ", colLabel, hal );

        hal = JLabel.LEADING;
        lbldiffTotalPlayers = createLabel( "  " , colLabel, hal );
        lbldiffNatPlayers = createLabel( "  " , colLabel, hal );
        lbldiffFgnPlayers = createLabel( "  " , colLabel, hal );
        lbldiffAvgAge = createLabel( "  " , colLabel, hal );
        lbldiffAvgForm = createLabel( "  " , colLabel, hal );
        lbldiffTotalSalary = createLabel( "   " , colLabel, hal );
        lbldiffAvgSalary = createLabel( "   " , colLabel, hal );
        lbldiffTotalValue = createLabel( "   " , colLabel, hal );
        lbldiffAvgValue = createLabel( "   " , colLabel, hal );
        lbldiffRank = createLabel( "  " , colLabel, hal );
        lbldiffMoney = createLabel( "   " , colLabel, hal );
        lbldiffFans = createLabel( "  " , colLabel, hal );
        lbldiffFanClubMood = createLabel( "  ", colLabel, hal );

        Insets insCentre = new Insets(0,2,0,2);
        Insets insLabel  = new Insets(0,2,0,0);
        Insets insValue  = new Insets(0,0,0,2);
        sgbc.fill = GridBagConstraints.BOTH;
        sgbc.gridwidth = 3;
        sgbc.gridheight = 1;
        sgbc.weightx = 0.0;
        sgbc.weighty = 0.0;
        sgbc.insets = insCentre;
        p.add(lblTeamName, sgbc);
        sgbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lblCountryFlag, sgbc);

        sgbc.gridwidth = 1;
        sgbc.anchor = GridBagConstraints.LINE_START;
        sgbc2 = (GridBagConstraints)sgbc.clone();
        sgbc3 = (GridBagConstraints)sgbc.clone();
        sgbc2.anchor = GridBagConstraints.LINE_END;
        sgbc3.anchor = GridBagConstraints.LINE_END;
        sgbc.insets = insCentre;
        sgbc2.insets = insLabel;
        sgbc3.insets = insValue;
        p.add(lbltxtMoney, sgbc);
        p.add(lblvarMoney, sgbc2);
        p.add(lbldiffMoney, sgbc3);
        p.add(lbltxtRank, sgbc);
        p.add(lblvarRank, sgbc2);
        sgbc3.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lbldiffRank, sgbc3);

        sgbc3.gridwidth = 1;
        p.add(lbltxtFans, sgbc);
        p.add(lblvarFans, sgbc2);
        p.add(lbldiffFans, sgbc3);
        p.add(lbltxtFanClubMood, sgbc);
        p.add(lblvarFanClubMood, sgbc2);
        sgbc3.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lbldiffFanClubMood, sgbc3);

        sgbc3.gridwidth = 1;
        p.add(lbltxtTotalPlayers, sgbc);
        p.add(lblvarTotalPlayers, sgbc2);
        p.add(lbldiffTotalPlayers, sgbc3);
        sgbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(Box.createHorizontalGlue(), sgbc);

        sgbc.gridwidth = 1;
        p.add(lbltxtNatPlayers, sgbc);
        p.add(lblvarNatPlayers, sgbc2);
        p.add(lbldiffNatPlayers, sgbc3);
        p.add(lbltxtFgnPlayers, sgbc);
        p.add(lblvarFgnPlayers, sgbc2);
        sgbc3.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lbldiffFgnPlayers, sgbc3);

        sgbc3.gridwidth = 1;
        p.add(lbltxtAvgAge, sgbc);
        p.add(lblvarAvgAge, sgbc2);
        p.add(lbldiffAvgAge, sgbc3);
        p.add(lbltxtAvgForm, sgbc);
        p.add(lblvarAvgForm, sgbc2);
        sgbc3.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lbldiffAvgForm, sgbc3);

        sgbc3.gridwidth = 1;
        p.add(lbltxtTotalValue, sgbc);
        p.add(lblvarTotalValue, sgbc2);
        p.add(lbldiffTotalValue, sgbc3);
        p.add(lbltxtAvgValue, sgbc);
        p.add(lblvarAvgValue, sgbc2);
        sgbc3.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lbldiffAvgValue, sgbc3);

        sgbc.gridheight = GridBagConstraints.REMAINDER;
        sgbc2.gridheight = GridBagConstraints.REMAINDER;
        sgbc3.gridheight = GridBagConstraints.REMAINDER;
        sgbc3.gridwidth = 1;
        p.add(lbltxtTotalSalary, sgbc);
        p.add(lblvarTotalSalary, sgbc2);
        p.add(lbldiffTotalSalary, sgbc3);
        p.add(lbltxtAvgSalary, sgbc);
        p.add(lblvarAvgSalary, sgbc2);
        sgbc3.gridwidth = GridBagConstraints.REMAINDER;
        p.add(lbldiffAvgSalary, sgbc3);
        p.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder() ,
                                                         BorderFactory.createLoweredBevelBorder() ) );
    }

    private void updateSummaryPanel(Date compareDate) {
        if (allPlayersDates==null || allPlayersDates.isEmpty()) return;
        if (!allPlayersDates.contains(compareDate)) {
            SortedSet<Date> auxSet = allPlayersDates.headSet(compareDate);
            if (auxSet.isEmpty()) compareDate = allPlayersDates.first();
            else compareDate = auxSet.last();
        }

        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        NumberFormat decFormat = NumberFormat.getInstance();
        decFormat.setMaximumFractionDigits(2);
        int country = team.getCountryId();
        int totalPlayers = roster.getActivePlayerCount();
        int natPlayers = roster.getPlayersByNationalityCount(country);

        int diffTotalPlayers = totalPlayers - roster.getActivePlayerCountOn(compareDate);
        int diffNatPlayers = natPlayers - roster.getPlayersByNationalityCount(country, compareDate);
        int diffFgnPlayers = diffTotalPlayers - diffNatPlayers;
        double diffAvgAge = roster.getAverageAge() - roster.getAverageAge(compareDate);
        double diffAvgForm = roster.getAverageForm() - roster.getAverageForm(compareDate);
        int diffTotalSalary = roster.getTotalSalary() - roster.getTotalSalary(compareDate);
        double diffAvgSalary = roster.getAverageSalary() - roster.getAverageSalary(compareDate);
        int diffTotalValue = roster.getTotalValue() - roster.getTotalValue(compareDate);
        double diffAvgValue = roster.getAverageValue() - roster.getAverageValue(compareDate);
        float diffRank = team.getRank() - team.getRank(compareDate);;
        long diffMoney = team.getMoney() - team.getMoney(compareDate);
        int diffFans = team.getFans() - team.getFans(compareDate);
        int diffFanClubMood = team.getFanClubMood() - team.getFanClubMood(compareDate);

        double ccr = options.getCurrencyConversionRate();

        auxSignFormat( lbldiffTotalPlayers, diffTotalPlayers, Integer.toString(diffTotalPlayers) );
        auxSignFormat( lbldiffNatPlayers, diffNatPlayers, Integer.toString(diffNatPlayers) );
        auxSignFormat( lbldiffFgnPlayers, diffFgnPlayers, Integer.toString(diffFgnPlayers) );
        auxSignFormat( lbldiffAvgAge, (int)diffAvgAge, decFormat.format(diffAvgAge) );
        auxSignFormat( lbldiffAvgForm, (int)diffAvgForm, decFormat.format(diffAvgForm) );
        auxSignFormat( lbldiffTotalSalary, diffTotalSalary, intFormat.format( diffTotalSalary*ccr ) );
        auxSignFormat( lbldiffAvgSalary, (int)diffAvgSalary, decFormat.format(diffAvgSalary*ccr) );
        auxSignFormat( lbldiffTotalValue, diffTotalValue, intFormat.format(diffTotalValue*ccr) );
        auxSignFormat( lbldiffAvgValue, (int)diffAvgValue, decFormat.format(diffAvgValue*ccr) );
        auxSignFormat( lbldiffRank, (int)diffRank, decFormat.format(diffRank) );
        auxSignFormat( lbldiffMoney, (int)diffMoney, intFormat.format(diffMoney*ccr) );
        auxSignFormat( lbldiffFans, diffFans, Integer.toString(diffFans) );
        auxSignFormat( lbldiffFanClubMood, diffFanClubMood, Integer.toString(diffFanClubMood) );
    }

    private void auxSignFormat(JLabel lbl, int sign, String txt) {
        if (sign<0) lbl.setForeground(Color.RED);
        else lbl.setForeground(Color.GREEN.darker());
        if (sign>0) lbl.setText("  +" + txt);
        else lbl.setText("  " + txt);
        return;
    }

    private void buildDateChooserPanel() {
        Box b = dateChooserPanel;
        datesCombo = new JComboBox();
        fillDatesComboBox();
        datesCombo.addItemListener(this);
        dateChooserCalendar.getDayChooser().addPropertyChangeListener("day", this);
        java.util.Calendar auxCal = dateChooserCalendar.getCalendar();
        auxCal.set(java.util.Calendar.AM_PM, java.util.Calendar.AM);
        auxCal.set(java.util.Calendar.MILLISECOND, 999);
        auxCal.set(java.util.Calendar.SECOND, 59);
        auxCal.set(java.util.Calendar.MINUTE, 59);
        auxCal.set(java.util.Calendar.HOUR, 23);
        b.add(datesCombo);
        b.add(dateChooserCalendar);
        b.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                                                       labelManager.getLabel(TXT_COMPARE_DATA) ) );
    }
    private void fillDatesComboBox() {
        datesCombo.removeAllItems();
        Vector<Date> dates = new Vector<Date>(roster.getDates());
        java.util.Collections.reverse(dates);
        datesCombo.addItem(null);
        for (Date d : dates) datesCombo.addItem(new FormattedDateHolder(d));
    }

    private JLabel createLabel(String text, Color backColor, int halign) {
        JLabel label = new JLabel(text, halign);
        label.setOpaque(true);
        label.setBackground(backColor);
        return label;
    }

    public void refreshData() {
        allPlayersDates = roster.getAllDates();
        refreshTable();
        refreshTableHeaders();
        refreshSummaryPanel();
        //refreshDateChooserPanel();
        fillDatesComboBox();
        datesCombo.repaint();
    }
    public void refreshSummaryPanel() {
        summaryPanel.removeAll();
        buildSummaryPanel();
    }
    public void refreshDateChooserPanel() {
        fillDatesComboBox();
        dateChooserCalendar.getDayChooser().drawWeeks();
        datesCombo.repaint();
    }
    public void refreshTable() {
        tableModel.fireTableDataChanged();
    }

    public void refreshTableHeaders() {
        TableColumnModel tcm = squadTable.getColumnModel();
        String columnName;
        TableColumn aColumn;
        for (int i=0; i<tcm.getColumnCount(); i++) {
            aColumn = tcm.getColumn(i);
            columnName = tableModel.getColumnName( aColumn.getModelIndex() );
            aColumn.setHeaderValue(columnName);
        }
        squadTable.getTableHeader().repaint();
    }

    public void rebuildTable() {
        tableModel.fireTableStructureChanged();
    }
    public void storeColumnSettings() {
        if (squadTable == null) return;
        TableColumnModel cm = squadTable.getColumnModel();
        if (cm == null) return;
        ArrayList<TableColumnData> ptc = options.getPlayerTableColumns();
        //int columnsInView = cm.getColumnCount(); // squadTable.getColumnCount(); 

        TableColumnData [] tempArray = new TableColumnData[ptc.size()];
        int notUsedIndex = ptc.size() - 1;
        int indexInModel = 0;
        int indexInView = 0;
        int width = 75;
        for (TableColumnData data : ptc) {
            try {
                indexInView = cm.getColumnIndex(data);
            } catch (IllegalArgumentException iae) {
                //no está
                data.setActive(false);
                tempArray[notUsedIndex] = data;
                notUsedIndex--;
                continue;
            }
            indexInModel = squadTable.convertColumnIndexToModel(indexInView);
            width = cm.getColumn(indexInView).getWidth();
            data.setWidth(width);
            data.setActive(true);
            tempArray[indexInView] = data;
        }
        ArrayList<TableColumnData> newPtc = new ArrayList<TableColumnData>(ptc.size());
        for (int i=0; i<tempArray.length; i++) {
            newPtc.add( tempArray[i] );
        }
        options.setPlayerTableColumns(newPtc);
    }

    public void setPlayerStatsPanel(PlayerStatsPanel psp) { playerStatsPanel = psp; }

    /* interface PropertyChangeListener */
	/**
	 * Listens for a "date" property change or a "day" property change event
	 * from the JCalendar. Updates the date editor and closes the popup.
	 * 
	 * @param evt
	 *            the event
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("day")) {
            if (!compareDateIsChanging) {
                datesCombo.setSelectedItem(null);
                dateChooserCalendar.getCalendar().set(java.util.Calendar.AM_PM, java.util.Calendar.AM);
                dateChooserCalendar.getCalendar().set(java.util.Calendar.MILLISECOND, 999);
                dateChooserCalendar.getCalendar().set(java.util.Calendar.SECOND, 59);
                dateChooserCalendar.getCalendar().set(java.util.Calendar.MINUTE, 59);
                dateChooserCalendar.getCalendar().set(java.util.Calendar.HOUR, 23);
            }
            PlayerProfile.setDateForComparison( dateChooserCalendar.getCalendar().getTime() );
            refreshTable();
            refreshTableHeaders();
            updateSummaryPanel( dateChooserCalendar.getCalendar().getTime() );
		}
	}
    /* interface ItemListener */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            if (ie.getItem() == null) return;
            compareDateIsChanging = true;
            Date d = ( (FormattedDateHolder)ie.getItem() ).getDate();
            dateChooserCalendar.setDate(d);
            //PlayerProfile.setDateForComparison( d );
            //refreshTable();
            compareDateIsChanging = false;
        }
    }
    /* interface MouseListener */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2) {
            int row = squadTable.getSelectedRow();
            if (row==-1) return;
            PlayerProfile p = roster.getPlayersList().get( tableSorter.modelIndex(row) );
            if (playerStatsPanel!=null) {
                so.gui.MainFrame.setSelectedTab(playerStatsPanel);
                playerStatsPanel.showPlayer(p);
            }
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON3) {
            int row = squadTable.rowAtPoint(e.getPoint());
            squadTable.getSelectionModel().setSelectionInterval(row, row);
        }
    }
    public void mouseReleased(MouseEvent e) { }

    /* interface ActionListener */
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        if (command.equals(CMD_PLAYER_REPORT)) {
            int row = squadTable.getSelectedRow();
            if (row==-1) return;
            PlayerProfile p = roster.getPlayersList().get( tableSorter.modelIndex(row) );
            so.text.ReportFrame.showPlayerReport(p);
        }
        else if (command.equals(CMD_MANAGER_NOTES)) {
            if (playerStatsPanel!=null) {
                int row = squadTable.getSelectedRow();
                if (row==-1) return;
                PlayerProfile p = roster.getPlayersList().get( tableSorter.modelIndex(row) );
                p.showManagerNotesEditor();
            }
        }
    }

    /* ####################################################################### */
    private class PlayerTableModel extends AbstractTableModel {
        private Object [][] cache;
        private int [] sumsCache;
        private int rows;
        private int cols;

        public PlayerTableModel() {
            super();
            rebuildDataCache();
        }

        private void rebuildDataCache() {
            rows = getRowCount();
            cols = getColumnCount();
            if (rows>0 && cols>0) {
                cache = new Object[rows][cols];
                sumsCache = new int[cols];
                for (int r=0; r<rows; r++) {
                    for (int c=0; c<cols; c++) {
                        cache[r][c] = roster.getPlayersList().get(r).getData(c);
                    }
                }
                for (int c=4; c<=15; c++) {
                    int sum = 0;
                    for (int r=0; r<rows; r++) {
                        if (cache[r][c] instanceof DataPair) {
                            sum += ( ((DataPair)cache[r][c]).getValue() - ((DataPair)cache[r][c]).getSecondValue() );
                        }
                    }
                    sumsCache[c] = sum;
                }
            }
            else {
                cache = null;
                sumsCache = null;
            }
        }

        public int getRowCount() {
            if (roster==null) return 0;
            return roster.getActivePlayerCount();
        }
        public int getColumnCount() { return PLAYERCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            if (column == 33) { // PilM
                int mid = matchRepo.getLatestMatchId(team.getId());
                if (mid==0) return MainFrame.getImageIcon(FILENAME_IMG_BLANK);
                if (roster.getPlayersList().size() <= row) return Integer.valueOf(-1);
                MatchRepository.PlayerProfile mpp = matchRepo.getPlayer(roster.getPlayersList().get(row).getId());
                if (mpp!=null && mpp.hasPlayedInMatch(mid)) return Integer.valueOf(mpp.getOrderInMatch(mid));
                return Integer.valueOf(-1);
            }
            else {
                //return roster.getPlayersList().get(row).getData(column);
                if (cache!=null && row<rows && column<cols) return cache[row][column];
                else return "";
            }
        }
        public void setValueAt(Object value, int row, int column) {
            try {
                switch (column) {
                case 3:
                    DataPair dp = (DataPair)value;
                    if (dp.getType() != DATA_POSITION) return;
                    roster.getPlayersList().get(row).setPreferredPosition( dp.getValue() );
                    break;
                case 34:
                    Character ch = (Character)value;
                    roster.getPlayersList().get(row).setSquadAssignment( ch.charValue() );
                    break;
                case 35:
                    if (value instanceof Boolean)
                        roster.getPlayersList().get(row).setSendToNTDB(((Boolean)value).booleanValue());
                    break;
                default:
                }
            } catch (ClassCastException cce) {
                return;
            }
            cache[row][column] = roster.getPlayersList().get(row).getData(column);
        }
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch(columnIndex) {
            case 3:
            case 34:
                return true;
            case 35:
                if (so.data.NTDBHandler.isNtdbUrlSet(roster.getPlayersList().get(rowIndex).getCountryFrom())) return true;
                //else return false;
            default:
                return false;
            }
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  0: return labelManager.getLabel(TXT_CH_NAME);
            //case  0: return "<HTML>Nombre<BR>Apellido</HTML>";
            case  1: return " "; //"Country";
            case  2: return labelManager.getLabel(TXT_CH_AGE);
            case  3: return labelManager.getLabel(TXT_CH_BEST_POSITION);
            case  4: return createHeaderText(col, labelManager.getLabel(TXT_CH_FORM));
            case  5: return createHeaderText(col, labelManager.getLabel(TXT_CH_TACTDISCIP));
            case  6: return createHeaderText(col, labelManager.getLabel(TXT_CH_EXPERIENCE));
            case  7: return createHeaderText(col, labelManager.getLabel(TXT_CH_TEAMWORK));
            case  8: return createHeaderText(col, labelManager.getLabel(TXT_CH_STAMINA));
            case  9: return createHeaderText(col, labelManager.getLabel(TXT_CH_PACE));
            case 10: return createHeaderText(col, labelManager.getLabel(TXT_CH_TECHNIQUE));
            case 11: return createHeaderText(col, labelManager.getLabel(TXT_CH_PASSING));
            case 12: return createHeaderText(col, labelManager.getLabel(TXT_CH_KEEPER));
            case 13: return createHeaderText(col, labelManager.getLabel(TXT_CH_DEFENDER));
            case 14: return createHeaderText(col, labelManager.getLabel(TXT_CH_PLAYMAKER));
            case 15: return createHeaderText(col, labelManager.getLabel(TXT_CH_SCORER));
            case 16: return labelManager.getLabel(TXT_CH_MATCHES);
            case 17: return labelManager.getLabel(TXT_CH_GOALS);
            case 18: return labelManager.getLabel(TXT_CH_ASSISTS);
            case 19: return labelManager.getLabel(TXT_CH_STATE);
            case 20: return labelManager.getLabel(TXT_CH_VALUE);
            case 21: return labelManager.getLabel(TXT_CH_SALARY);
            case 22: return labelManager.getPositionShortName(GK);
            case 23: return labelManager.getPositionShortName(WB);
            case 24: return labelManager.getPositionShortName(CB);
            case 25: return labelManager.getPositionShortName(SW);
            case 26: return labelManager.getPositionShortName(DM);
            case 27: return labelManager.getPositionShortName(CM);
            case 28: return labelManager.getPositionShortName(AM);
            case 29: return labelManager.getPositionShortName(WM);
            case 30: return labelManager.getPositionShortName(FW);
            case 31: return labelManager.getPositionShortName(ST);
            case 32: return labelManager.getLabel(TXT_CH_ID);
            case 33: return labelManager.getLabel(TXT_CH_PLAYEDLASTMATCH);
            case 34: return labelManager.getLabel(TXT_CH_SQUAD);
            case 35: return labelManager.getLabel(TXT_CH_SEND_TO_NTDB);
            case 36: return labelManager.getLabel(TXT_CH_HEIGHT);
            default:
                return " ";
            }
        }

        private String createHeaderText(int col, String txt) {
            if (sumsCache == null) return txt;
            String val = "";
            if (sumsCache[col] > 0) {
                val = "<FONT color=GREEN><B><CENTER>+" + Integer.toString(sumsCache[col]) + "</CENTER></B></FONT>";
            }
            else if (sumsCache[col] < 0) {
                val = "<FONT color=RED><B><CENTER>" + Integer.toString(sumsCache[col]) + "</CENTER></B></FONT>";
            }
            else val = "&nbsp;";
            return "<HTML>" + txt + "<BR>" + val + "</HTML>";
        }

        public void fireTableDataChanged() {
            rebuildDataCache();
            super.fireTableDataChanged();
        }

    }

}
