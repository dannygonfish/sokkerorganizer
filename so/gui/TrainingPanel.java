package so.gui;

import static so.Constants.*;
import static so.Constants.Labels.*;
import static so.Constants.Positions.*;
import static so.Constants.Colors.*;
import so.data.*;
import so.gui.render.*;
import so.util.SokkerCalendar;
import so.util.SokkerWeek;
import so.text.LabelManager;
import so.config.Options;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.Box;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import com.toedter.calendar.JCalendar;


public class TrainingPanel extends JPanel implements PropertyChangeListener, MouseListener, ItemListener {
    private static final DateFormat DFORMAT = DateFormat.getDateInstance(DateFormat.SHORT);
    private static final int [] trTypeMap = { 0, 5, 9, 11, 8, 7, 10, 12, 6 };
    private static Color [] skillBgColour = { Color.WHITE, Color.LIGHT_GRAY, new Color(225,226,255),
                                              new Color(255,255,225), new Color(225,255,255), new Color(255,248,205),
                                              new Color(255,225,255), new Color(255,225,225), new Color(225,255,225) };

    private LabelManager labelManager;
    private Options options;
    private CoachOffice coachOffice;
    private PlayerRoster roster;
    private MatchRepository matchRepo;

    private NumberFormat intFormat;
    private JTable coachTable;
    private CoachTableModel coachTableModel;
    private JTable playerTrainingTable;
    private PlayerTrainingTableModel ptTableModel;

    private JCalendar dateChooserCalendar;
    private JPanel trainingDisplayPanel;
    private Box dateChooserPanel;

    private JLabel lblSeason;
    private JLabel lblWeek;
    private JLabel lblTraining;
    private JLabel lblOrder;
    private JLabel lblTotalWage;
    private JLabel lblDataStatus;
    private JComboBox playerSelectorCombo;
    private JComboBox trainingAnalisisCombo;

    private String [] trainingLabels;
    private final String [] orderLabels = { "GK", "DEF", "MID", "ATT" };

    private CoachOffice.TrainingData2 trainingData;
    private SokkerWeek skWeek;
    private PlayerTrainingData playerAnalized;

    public TrainingPanel(LabelManager lm, Options opt, CoachOffice office, PlayerRoster roster, MatchRepository repo) {
        super(new GridBagLayout());
        labelManager = lm;
        options = opt;
        coachOffice = office;
        this.roster = roster;
        matchRepo = repo;
        trainingData = office.getTrainingData(null);
        intFormat = NumberFormat.getIntegerInstance();

        skWeek = new SokkerWeek(new Date());
        dateChooserCalendar = new JCalendar();
        dateChooserCalendar.getDayChooser().setDrawTrainingWeek(true);
        coachTable = new JTable();
        coachTableModel = new CoachTableModel();
        coachTable.setModel(coachTableModel);
        coachTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        initColumns1();
        coachTable.setPreferredScrollableViewportSize(new Dimension(500, 128));
        coachTable.setTableHeader(new JTableHeader(coachTable.getColumnModel()) {
                public String getToolTipText(java.awt.event.MouseEvent e) {
                    int index = columnModel.getColumnIndexAtX(e.getPoint().x);
                    return coachTableModel.getHeaderToolTip(index);
                }
            } );
        coachTable.getTableHeader().setReorderingAllowed(false);
        //coachTable.getTableHeader().setResizingAllowed(false);

        playerTrainingTable = new JTable();
        ptTableModel = new PlayerTrainingTableModel();
        playerTrainingTable.setModel(ptTableModel);
        playerTrainingTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        initColumns2();
        playerTrainingTable.setPreferredScrollableViewportSize(new Dimension(500, 300));
        playerTrainingTable.setTableHeader(new JTableHeader(playerTrainingTable.getColumnModel()) {
                public String getToolTipText(java.awt.event.MouseEvent e) {
                    int index = columnModel.getColumnIndexAtX(e.getPoint().x);
                    return ptTableModel.getHeaderToolTip(index);
                }
            } );
        playerTrainingTable.getTableHeader().setReorderingAllowed(false);

        /* player Selector ComboBox */
        playerSelectorCombo = new JComboBox();
        trainingAnalisisCombo = new JComboBox();

        fillPlayerSelectorCombo();
        playerSelectorCombo.setSelectedIndex(-1);
        playerSelectorCombo.addItemListener(this);
        playerSelectorCombo.setBackground(COLOR_LIGHT_YELLOW);

        createGUI();

        trainingAnalisisCombo.addItemListener(this);
        trainingAnalisisCombo.setBackground(Color.WHITE);
        trainingAnalisisCombo.setMaximumRowCount(10);

        try {
            Dimension dim = playerSelectorCombo.getPreferredSize();
            dim.setSize(250, dim.getHeight());
            playerSelectorCombo.setPreferredSize(dim);
            trainingAnalisisCombo.setPreferredSize(new Dimension(150, (int)dim.getHeight()));
        } catch (NullPointerException npe) { }
     }

    private void initColumns1() {
        TableColumn column = null;
        CoachTableRenderer ctr = new CoachTableRenderer();
        int cC = coachTable.getColumnModel().getColumnCount();
        for (int i = 0; i < cC; i++) {
            column = coachTable.getColumnModel().getColumn(i);
            column.setCellRenderer(ctr);
            switch (i) {
            case 13: /* salary */
                column.setPreferredWidth(120);
                break;
            case 0:  /* job */
                column.setPreferredWidth(100);
                break;
            case 1: /* name */
                column.setPreferredWidth(200);
                break;
            case 2: /* flag */
                column.setPreferredWidth(24);
                break;
            case 3: /* age */
            default: /* skills */
                column.setPreferredWidth(50);
            }
        }
    }

    private void initColumns2() {
        TableColumn column = null;
        PlayerTrainingTableRenderer pttr = new PlayerTrainingTableRenderer();
        int cC = playerTrainingTable.getColumnModel().getColumnCount();
        for (int i = 0; i < cC; i++) {
            column = playerTrainingTable.getColumnModel().getColumn(i);
            column.setCellRenderer(pttr);
            switch (i) {
            case 0:  /* week */
                column.setPreferredWidth(152);
                break;
            case 10: /* training */
                column.setPreferredWidth(120);
                break;
            case 11: /* order */
            case 16:
            case 15 : /* minutes */
                column.setPreferredWidth(50);
                break;
            case 12: /* main coach trained skill level */
            case 13: /* residual level of main coach */
            case 14: /* assistants average level */
            case 1: /* age */
            default: /* skills */
                column.setPreferredWidth(45);
            }
        }
    }

    private void fillPlayerSelectorCombo() {
        List<PlayerProfile> _currentPlayers = roster.getPlayersList();
        if (_currentPlayers==null || _currentPlayers.size()==0) ; //return;
        else {
            for (PlayerProfile _p : _currentPlayers) playerSelectorCombo.addItem(_p);
        }
    }

    private String getTrainingLabel(int idx) {
        if (trainingLabels==null || idx<0 || trainingLabels.length<=idx) return "-?-";
        return trainingLabels[idx];
    }
    private String getOrderLabel(int idx) {
        if (orderLabels==null || idx<0 || orderLabels.length<=idx) return "-?-";
        return orderLabels[idx];
    }

    private void createGUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        trainingDisplayPanel = new JPanel();
        dateChooserPanel = new Box(javax.swing.BoxLayout.Y_AXIS);
        buildTrainingDisplayPanel();
        buildDateChooserPanel();

        trainingAnalisisCombo.removeAllItems();
        trainingAnalisisCombo.addItem("");
        for (String _tr : trainingLabels) trainingAnalisisCombo.addItem(_tr);
        trainingAnalisisCombo.setSelectedIndex(0);

        JScrollPane coachTableScroll = new JScrollPane(coachTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        coachTableScroll.setMinimumSize(new Dimension(500, 86));
        coachTableScroll.setPreferredSize(new Dimension(500, 86));
        JScrollPane ptTableScroll = new JScrollPane(playerTrainingTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ptTableScroll.setMinimumSize(new Dimension(500, 144));
        ptTableScroll.setPreferredSize(new Dimension(500, 144));

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
        topPanel.add(trainingDisplayPanel, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        topPanel.add(coachTableScroll, gbc);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        JPanel p = new JPanel();
        p.add(playerSelectorCombo);
        p.add(trainingAnalisisCombo);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1; //GridBagConstraints.RELATIVE;
        gbc.weighty = 0.0;
        bottomPanel.add(p, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        bottomPanel.add(ptTableScroll, gbc);

        JScrollPane topScroll = new JScrollPane(topPanel);
        JScrollPane bottomScroll = new JScrollPane(bottomPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topScroll);
        splitPane.setBottomComponent(bottomScroll);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation( topScroll.getPreferredSize().height + 4 );

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(splitPane, gbc);

    }

    private void buildDateChooserPanel() {
        Box b = dateChooserPanel;
        dateChooserCalendar.getDayChooser().addPropertyChangeListener("day", this);
        Calendar auxCal = dateChooserCalendar.getCalendar();
        auxCal.set(Calendar.AM_PM, Calendar.AM);
        auxCal.set(Calendar.MILLISECOND, 999);
        auxCal.set(Calendar.SECOND, 59);
        auxCal.set(Calendar.MINUTE, 59);
        auxCal.set(Calendar.HOUR, 23);
        b.add(dateChooserCalendar);
        b.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.RAISED) );
    }

    private void buildTrainingDisplayPanel() {
        trainingLabels = new String[] { labelManager.getLabel(TXT_STAMINA), labelManager.getLabel(TXT_KEEPER),
                                        labelManager.getLabel(TXT_PLAYMAKER), labelManager.getLabel(TXT_PASSING),
                                        labelManager.getLabel(TXT_TECHNIQUE), labelManager.getLabel(TXT_DEFENDER),
                                        labelManager.getLabel(TXT_SCORER), labelManager.getLabel(TXT_PACE) };
        JPanel p = trainingDisplayPanel;
        p.setLayout(new GridBagLayout());
        p.setToolTipText(labelManager.getLabel(TT+TXT_CHANGE_TRAINING));
        JLabel txtSeason = new JLabel(labelManager.getLabel(TXT_SEASON));
        lblSeason = new JLabel("24");
        JLabel txtWeek = new JLabel(labelManager.getLabel(TXT_WEEK));
        lblWeek = new JLabel("16");
        JLabel txtTraining = new JLabel(labelManager.getLabel(TXT_TRAINING));
        lblTraining = new JLabel("training");
        JLabel txtOrder = new JLabel(labelManager.getLabel(TXT_ORDER));
        lblOrder = new JLabel("ATT");
        JLabel txtTotalWage = new JLabel(labelManager.getLabel(TXT_TOTAL_SALARY));
        lblTotalWage = new JLabel("1.000.000 $$$");
        lblDataStatus = new JLabel(" ");
        p.addMouseListener(this);

        GridBagConstraints tgbc = new GridBagConstraints();
        GridBagConstraints tgbc2 = new GridBagConstraints();
        tgbc.anchor = GridBagConstraints.LINE_START;
        tgbc.insets = new java.awt.Insets(1,10,1,2);
        tgbc2.gridwidth = GridBagConstraints.REMAINDER;
        p.add(Box.createHorizontalStrut(240), tgbc2);
        p.add(txtSeason, tgbc);
        p.add(lblSeason, tgbc2);
        p.add(txtWeek, tgbc);
        p.add(lblWeek, tgbc2);
        p.add(txtTraining, tgbc);
        p.add(lblTraining, tgbc2);
        p.add(txtOrder, tgbc);
        p.add(lblOrder, tgbc2);
        p.add(txtTotalWage, tgbc);
        p.add(lblTotalWage, tgbc2);
        tgbc2.gridheight = GridBagConstraints.REMAINDER;
        p.add(lblDataStatus, tgbc2);
        p.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder() ,
                                                         BorderFactory.createLoweredBevelBorder() ) );
        refreshTrainingPanel();
    }

    private void refreshTrainingPanel() {
        SokkerCalendar skCal = SokkerCalendar.getSharedInstance();
        lblSeason.setText(Integer.toString(skWeek.getSeason()));
        lblWeek.setText(Integer.toString(skWeek.getWeekOfSeason()));
        if (trainingData != null) {
            lblTraining.setText( getTrainingLabel(trainingData.getTrainingType()-1) );
            lblOrder.setText( getOrderLabel(trainingData.getTrainingPos()) );
            double ccr = options.getCurrencyConversionRate();
            String cs = options.getCurrencySymbol();
            lblTotalWage.setText( intFormat.format( trainingData.getTrainersWage()*ccr ) + " " + cs );
            if (trainingData.getDate().equals(skWeek.getEndDateInclusive())) lblDataStatus.setText("<html><i><font color=red>" + labelManager.getLabel(TXT_DATA_SET_MANUALLY) + "</font></i></html>");
            else lblDataStatus.setText("<html><i>" + labelManager.getExtendedLabel(TXT_DATA_STATUS, java.text.DateFormat.getDateTimeInstance().format(trainingData.getDate())) + "</i></html>");
        }
        else {
            lblTraining.setText(" ");
            lblOrder.setText(" ");
            lblTotalWage.setText(" ");
            lblDataStatus.setText(" ");
        }
    }

    private void changeTrainingOrder() {
        JComboBox comboTraining = new JComboBox(trainingLabels);
        JComboBox comboOrder = new JComboBox(orderLabels);
        int oldTraining = trainingData.getTrainingType();
        int oldOrder = trainingData.getTrainingPos();
        if (oldTraining<1) oldTraining = 0;
        if (oldOrder<0) oldOrder = -1;
        comboTraining.setSelectedIndex(oldTraining-1);
        comboOrder.setSelectedIndex(oldOrder);
        //show dialog
        Box changeBox = new Box(javax.swing.BoxLayout.Y_AXIS);
        changeBox.add(comboTraining);
        changeBox.add(comboOrder);
        int ret = JOptionPane.showConfirmDialog(this, changeBox, labelManager.getLabel(TXT_CHANGE_TRAINING),
                                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret != JOptionPane.OK_OPTION) return;
        int training = comboTraining.getSelectedIndex() + 1;
        int order = comboOrder.getSelectedIndex();
        if (oldTraining==training && oldOrder==order) return;
        if (training<1 || order<0) return;
        coachOffice.updateTraining(training, order, skWeek.getEndDateInclusive(), true);

        /* refresh */
        trainingData = coachOffice.getTrainingData(skWeek.getEndDateInclusive());
        refreshPanels();
    }

    public void refreshData() {
        refreshPanels();
        playerSelectorCombo.removeItemListener(this);
        playerSelectorCombo.removeAllItems();
        fillPlayerSelectorCombo();
        playerSelectorCombo.addItemListener(this);
        playerSelectorCombo.setSelectedIndex(-1);
        playerAnalized = null;
        refreshPlayerTrainingPanel();
    }

    public void refreshPanels() {
        refreshTraining();
        refreshPlayerTrainingPanel();
    }

    public void refreshTraining() {
        refreshTrainingPanel();
        coachTableModel.fireTableDataChanged();
    }

    public void refreshPlayerTrainingPanel() {
        ptTableModel.fireTableDataChanged();
    }


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
            Date d = dateChooserCalendar.getDate();
            if (skWeek.contains(d)) return;
            skWeek = new SokkerWeek(d);
            trainingData = coachOffice.getTrainingData(d);
            refreshTraining();
		}
	}

    /* interface MouseListener */
    public void mouseClicked(MouseEvent me) {
        if (me.getButton()==MouseEvent.BUTTON1 && me.getClickCount()==2) {
            changeTrainingOrder();
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    /* ========= interface ItemListener ========= */
    /* change in Player selector */
    public void itemStateChanged(java.awt.event.ItemEvent ie) {
        if (ie.getItem()==null) return;
        if (ie.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (ie.getSource().equals(playerSelectorCombo)) {
                PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
                if (p==null) return;
                playerAnalized = new PlayerTrainingData(p);
                refreshPlayerTrainingPanel();
            }
            else if (ie.getSource().equals(trainingAnalisisCombo)) {
                playerTrainingTable.repaint();
            }
        }
    }

    /* ####################################################################### */
    /* ####################################################################### */
    private class CoachTableModel extends AbstractTableModel {

        public CoachTableModel() {
            super();
        }

        public int getRowCount() {
            if (coachOffice==null) return 0;
            if (!coachOffice.hasData()) return 0;
            if (trainingData == null) return 0;
            return trainingData.getTrainers().size();
        }

        public int getColumnCount() { return TRAINERCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            return trainingData.getTrainers().get(row).getData(column);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  0: return labelManager.getLabel(TXT_JOB);
            case  1: return labelManager.getLabel(TXT_CH_NAME);
            case  2: return " "; //"Country";
            case  3: return labelManager.getLabel(TXT_CH_AGE);
            case  4: return labelManager.getLabel(TXT_CH_TRAINER_SKILL); // GENERAL SKILL
            case  5: return labelManager.getLabel(TXT_CH_STAMINA);
            case  6: return labelManager.getLabel(TXT_CH_PACE);
            case  7: return labelManager.getLabel(TXT_CH_TECHNIQUE);
            case  8: return labelManager.getLabel(TXT_CH_PASSING);
            case  9: return labelManager.getLabel(TXT_CH_KEEPER);
            case 10: return labelManager.getLabel(TXT_CH_DEFENDER);
            case 11: return labelManager.getLabel(TXT_CH_PLAYMAKER);
            case 12: return labelManager.getLabel(TXT_CH_SCORER);
            case 13: return labelManager.getLabel(TXT_CH_SALARY);
            default:
                return " ";
            }
        }

        public String getHeaderToolTip(int col) {
            switch(col) {
            case 2:
                return null;
            default:
                return getColumnName(col);
            }
        }

    }

    /* ####################################################################### */
    private class CoachTableRenderer extends DefaultTableCellRenderer {
        NumberFormat numberFormat;

        CoachTableRenderer() {
            super();
            numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            setBorder(null);
        }

        public void setValue(Object value) {
            setIcon(null);
            setToolTipText(null);
            if (value instanceof Integer) setText( value.toString() );
            else if (value instanceof Long) setText( numberFormat.format(((Long)value).longValue()*options.getCurrencyConversionRate()) + " " + options.getCurrencySymbol() );
            else if (value instanceof ImageIcon) {
                setIcon((ImageIcon)value);
                setToolTipText( ((ImageIcon)value).getDescription() );
                setText(null);
            }
            else if (value instanceof Byte) setText( labelManager.getLabel(TXT_TRAINERJOB_ + value.toString()) );
            else setText(value.toString());
        }

        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setFont(table.getFont());
            if (column==0) value = Byte.valueOf( (byte)trainingData.getCoachJob(((Integer)value).intValue()) );
            setValue(value);
            if (column<2) setHorizontalAlignment(JLabel.LEADING);
            else setHorizontalAlignment(JLabel.CENTER);

            int job = JOB_IDLE;
            try {
                //job = trainingData.getTrainers().get(row).getJobType();
                job = trainingData.getCoachJob( trainingData.getTrainers().get(row).getId() );
            } catch (NullPointerException npe) { }

            Color bg = table.getBackground();
            Color fg = table.getForeground();
            switch( job ) {
            case JOB_HEAD: /* red */
                bg = COLOR_LIGHT_PINK;
                int aux = trTypeMap[trainingData.getTrainingType()];
                //if (column==aux) setFont(table.getFont().deriveFont(Font.BOLD));
                if (column==aux) bg = new Color(255,128,128);
                break;
            case JOB_ASSISTANT: /* yellow */
                bg = COLOR_LIGHT_YELLOW;
                if (4<column && column<14) fg = Color.GRAY;
                break;
            case JOB_JUNIORS: /* green */
                bg = COLOR_LIGHT_GREEN;
                if (4<column && column<14) fg = Color.GRAY;
                break;
            case JOB_IDLE: /* normal, white */
            default:
            }
            if (isSelected) {
                bg = bg.darker();
                fg = table.getSelectionForeground();
            }
            setForeground(fg);
            setBackground(bg);
            return this;
        }
    }

    /* ####################################################################### */
    /* ####################################################################### */
    private class PlayerTrainingTableModel extends AbstractTableModel {
        private Object [][] cache;
        private int rows;
        private int cols;

        public PlayerTrainingTableModel() {
            super();
            rebuildDataCache();
        }

        private void rebuildDataCache() {
            rows = getRowCount();
            cols = getColumnCount();
            if (rows>0 && cols>0) {
                cache = new Object[rows][cols];
                for (int r=0; r<rows; r++) {
                    for (int c=0; c<cols; c++) {
                        cache[r][c] = playerAnalized.getData(r,c);
                    }
                }
            }
            else cache = null;
        }

        public int getRowCount() {
            if (playerAnalized == null) return 0;
            return playerAnalized.dataSize();
        }

        public int getColumnCount() { return PLAYERTRAININGCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            if (column == 13) return playerAnalized.getData(row, column);
            if (cache!=null && row<rows && column<cols) return cache[row][column];
            else return "";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  0: return labelManager.getLabel(TXT_WEEK);
            case  1: return labelManager.getLabel(TXT_CH_AGE);
            case  2: return labelManager.getLabel(TXT_CH_STAMINA);
            case  3: return labelManager.getLabel(TXT_CH_PACE);
            case  4: return labelManager.getLabel(TXT_CH_TECHNIQUE);
            case  5: return labelManager.getLabel(TXT_CH_PASSING);
            case  6: return labelManager.getLabel(TXT_CH_KEEPER);
            case  7: return labelManager.getLabel(TXT_CH_DEFENDER);
            case  8: return labelManager.getLabel(TXT_CH_PLAYMAKER);
            case  9: return labelManager.getLabel(TXT_CH_SCORER);
            case 10: return labelManager.getLabel(TXT_TRAINING);
            case 11: return labelManager.getLabel(TXT_ORDER);
            case 12: return labelManager.getLabel(TXT_TRAINING_LEVEL);
            case 13: return labelManager.getLabel(TXT_RESIDUAL_LEVEL);
            case 14: return labelManager.getLabel(TXT_ASSITANTS_TOTAL);
            case 15: return labelManager.getLabel(TXT_MINUTES_TRAINED);
            case 16: return labelManager.getLabel(TXT_ORDER_PLAYED);
            default:
                return " ";
            }
        }

        public void fireTableDataChanged() {
            rebuildDataCache();
            super.fireTableDataChanged();
        }

        public String getHeaderToolTip(int col) {
            switch(col) {
            case 2:
            default:
                return getColumnName(col);
            }
        }

    }
    /* ####################################################################### */
    private class PlayerTrainingTableRenderer extends DefaultTableCellRenderer {
        private NumberFormat numberFormat;
        private Color fg;

        PlayerTrainingTableRenderer() {
            super();
            numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            setBorder(null);
            setIcon(null);
            setToolTipText(null);
        }

        public void setValue(Object value) {
            if (value instanceof DataPair) {
                int a = ((DataPair)value).getValue();
                int b = ((DataPair)value).getSecondValue();
                if (a == NO_DATA) setText("-?-");
                else {
                    //if (a > b) fg = CELLCOLOR_DEF;
                    //else if (a < b) fg = Color.RED;
                    if (a != b) setBorder( BorderFactory.createMatteBorder(0,0,2,1, Color.BLACK) );
                    else setBorder( BorderFactory.createMatteBorder(0,0,0,1, Color.BLACK) );
                    setText(Integer.toString(a));
                    setToolTipText( labelManager.getSkillLevelName(a) );
                }
            }
            else if (value instanceof Integer) {
                if ( ((Integer)value).intValue()==NO_DATA ) setText("-?-");
                else setText( value.toString() );
            }
            else setText(value.toString());
        }

        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setFont(table.getFont());
            setBorder(null);
            setToolTipText(null);
            fg = table.getForeground();
            setValue(value);
            if (column==0) setHorizontalAlignment(JLabel.TRAILING);
            else setHorizontalAlignment(JLabel.CENTER);


            int train = coachOffice.getWeeklyData().getData(row, 0, 0);
            int trainAnalisis = trainingAnalisisCombo.getSelectedIndex();
            if (train==NO_DATA || (trainAnalisis!=0 && train!=trainAnalisis)) {
                train = 0;
                if (column==12) setText("");
            }
            else if (column==13 || column==14) setText("");
            if (column == trTypeMap[trainAnalisis]-3) train = trainAnalisis;
            Color bg = skillBgColour[ train ];

            if (isSelected) {
                bg = bg.darker();
                fg = table.getSelectionForeground();
            }
            setForeground(fg);
            setBackground(bg);
            return this;
        }
    }

    /* ####################################################################### */
    /* ####################################################################### */
    private class PlayerTrainingData {
        private PlayerProfile player;
        private MatchRepository.PlayerProfile mrpp;

        PlayerTrainingData(PlayerProfile pp) {
            player = pp;
            mrpp = matchRepo.getPlayer(pp.getId());
        }

        public int dataSize() { return player.getGraphicableData().size(); }

        public Object getData(int row, int column) {
            switch(column) {
            case  0: /* week */
                SokkerWeek skWeek = new SokkerWeek( SokkerCalendar.getDateOfWeeksAgo(row) );
                return row + ":  " + DFORMAT.format(skWeek.getBeginDate()) + " \u2192 " +
                    DFORMAT.format(skWeek.getEndDateInclusive());
                /* age */
            case  1: return player.getGraphicableData().getData(row, 11);
            case  2:
            case  3:
            case  4:
            case  5:
            case  6:
            case  7:
            case  8:
            case  9: return new DataPair(DATA_COMPARABLE_NUMBER, player.getGraphicableData().getData(row-1, column+1),
                                         player.getGraphicableData().getData(row, column+1));
                /* training type */
            case 10: return getTrainingLabel(coachOffice.getWeeklyData().getData(row, 0, 0)-1);
                /* training pos (order) */
            case 11: return getOrderLabel(coachOffice.getWeeklyData().getData(row, 1, 0));
                /* head coach level for training*/
            case 12: return coachOffice.getWeeklyData().getData(row, 2, 0);
                /* head coach level residual training */
            case 13: return coachOffice.getWeeklyData().getData(row, 3, trainingAnalisisCombo.getSelectedIndex());
                /* assistants sum */
            case 14: return coachOffice.getWeeklyData().getData(row, 4, 0);
            case 15: /* minutes trained */
                if (mrpp==null) return 0;
                else return mrpp.getWeeklyStats(row).getMinutesPlayed();
            case 16: /* order trained */
                if (mrpp==null) return "---";
                int _ord = mrpp.getWeeklyStats(row).getOrderTrained();
                if (_ord<0 || 3<_ord) return "---";
                else return orderLabels[_ord];
            default:
            return " ";
            }
        }

    }


}
