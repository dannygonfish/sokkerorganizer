package so.gui;

import static so.Constants.*;
import static so.Constants.Colors.*;
import static so.Constants.Labels.*;
import static so.Constants.Positions.*;
import so.data.*;
import so.text.LabelManager;
import so.config.Options;
import so.util.FormattedDateHolder;
import so.util.TableSorter;
import so.util.SokkerCalendar;
import java.util.Date;
import java.util.Vector;
import java.util.List;
import java.text.DateFormat;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JComboBox;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Component;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


public class PlayerStatsPanel extends JPanel implements ItemListener, ActionListener, MouseListener, GraphPanel.GraphDateListener {
    private static final Insets LEFT_INSETS        = new Insets(0,8,0,0);
    private static final Insets RIGHT_INSETS       = new Insets(0,0,0,8);
    private static final Insets NO_INSETS          = new Insets(0,0,0,0);
    private static final Insets PADDING_INSETS     = new Insets(8,14,8,14);

    private static final java.awt.Dimension DIM_LABEL = new java.awt.Dimension(140,18);

    private LabelManager labelManager;
    private Options options;
    private PlayerRoster roster;
    private MatchRepository matchRepo;
    private NumberFormat intFormat;

    private GraphPanel graphPanel;
    private JPanel playerSummaryPanel;
    private JPanel ratingsPanel;
    private JComboBox playerSelectorCombo;
    private JComboBox dateSelectorCombo;
    private FaceCanvas faceCanvas;
    private JButton jbNotes;

    private JTable ratingsTable;
    private RatingsTableModel tableModel;
    private TableSorter tableSorter;
    private MixedPlayerData mixedPlayerData;

    private JLabel nameVarLabel;
    private JLabel flagVarLabel;
    private JLabel ageVarLabel;
    private JLabel valueVarLabel;
    private JLabel salaryVarLabel;
    private JLabel formVarLabel;
    private JLabel cardsVarLabel;
    private JLabel healthVarLabel;
    private JLabel stamVarLabel;
    private JLabel techVarLabel;
    private JLabel paceVarLabel;
    private JLabel passVarLabel;
    private JLabel keepVarLabel;
    private JLabel defeVarLabel;
    private JLabel playVarLabel;
    private JLabel scorVarLabel;

    private int auxColorBack;

    public PlayerStatsPanel(LabelManager lm, Options opt, PlayerRoster ros, MatchRepository repo) {
        super(new GridBagLayout());
        auxColorBack = 1;
        labelManager = lm;
        options = opt;
        roster = ros;
        matchRepo = repo;
        GridBagConstraints gbc = new GridBagConstraints();
        intFormat = NumberFormat.getIntegerInstance();

        faceCanvas = new FaceCanvas();
        /* player Selector ComboBox */
        playerSelectorCombo = new JComboBox();
        fillPlayerSelectorCombo();
        /* non reusable */
        playerSelectorCombo.setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer()
            {
                public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof javax.swing.JPopupMenu.Separator) return (Component)value;
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof PlayerProfile) {
                        if ( !((PlayerProfile)value).isActive() ) {
                            this.setFont( this.getFont().deriveFont(java.awt.Font.ITALIC) );
                        }
                    }
                    return c;
                }
            } );

        dateSelectorCombo = new JComboBox();
        playerSelectorCombo.setSelectedIndex(-1);
        playerSelectorCombo.addItemListener(this);
        dateSelectorCombo.addItemListener(this);
        playerSelectorCombo.setBackground(COLOR_LIGHT_YELLOW);
        dateSelectorCombo.setBackground(Color.WHITE);

        jbNotes = new JButton(so.gui.MainFrame.getImageIcon(FILENAME_IMG_MANAGER_NO_NOTES));
        jbNotes.setToolTipText(labelManager.getLabel(TXT_MANAGER_NOTES));
        jbNotes.setFocusPainted(false);
        //jbNotes.setBorderPainted(false);
        jbNotes.addActionListener(this);

        createVarLabels();
        playerSummaryPanel = createPlayerSummaryPanel();

        tableModel = new RatingsTableModel();
        tableSorter = new TableSorter(tableModel);
        tableSorter.setReverseSorting(true);
        ratingsTable = new JTable(tableSorter);
        ratingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ratingsTable.addMouseListener(this);
//         JTableHeader tableHeader = new JTableHeader(ratingsTable.getColumnModel()) {
//                 public String getToolTipText(MouseEvent e) {
//                     java.awt.Point p = e.getPoint();
//                     int index = columnModel.getColumnIndexAtX(p.x);
//                     if (index < 0) return null;
//                     int realIndex = columnModel.getColumn(index).getModelIndex();
//                     return tableModel.getColumnName(realIndex);
//                 }
//             };
        ToolTipTableHeader tableHeader = new ToolTipTableHeader(ratingsTable.getColumnModel());
        tableHeader.setReorderingAllowed(false);
        //tableHeader().setResizingAllowed(false);
        ratingsTable.setTableHeader(tableHeader);
        tableSorter.setTableHeader(tableHeader);
        setColumnWidths();
        JScrollPane ratingsScroll = new JScrollPane(ratingsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //ratingsScroll.setMinimumSize(new Dimension(500, 146));
        //ratingsScroll.setPreferredSize(new Dimension(500, 146));

        graphPanel = new GraphPanel(new String[] { labelManager.getLabel(TXT_CH_VALUE),
                                                   labelManager.getLabel(TXT_CH_EXPERIENCE),
                                                   labelManager.getLabel(TXT_FORM),
                                                   labelManager.getLabel(TXT_STAMINA),
                                                   labelManager.getLabel(TXT_PACE),
                                                   labelManager.getLabel(TXT_TECHNIQUE),
                                                   labelManager.getLabel(TXT_PASSING),
                                                   labelManager.getLabel(TXT_KEEPER),
                                                   labelManager.getLabel(TXT_DEFENDER),
                                                   labelManager.getLabel(TXT_PLAYMAKER),
                                                   labelManager.getLabel(TXT_SCORER),
                                                   labelManager.getLabel(TXT_CH_MATCHES) } );
        graphPanel.setColors(new Color[] { new Color(190, 160, 145), Color.PINK, Color.GRAY, Color.MAGENTA, Color.GREEN,
                                               Color.ORANGE, Color.CYAN, new Color(120,120,255), CELLCOLOR_DEF,
                                               CELLCOLOR_MID, CELLCOLOR_ATT, Color.GRAY.brighter() } );
        graphPanel.setToolTips(labelManager.getLabel(TXT_CH_WEEKS), labelManager.getLabel(TXT_CH_SKILL));
        graphPanel.addGraphDateListener(this);
        graphPanel.useGraphPopup( labelManager.getLabel(TXT_WEEKS_IN_GRAPH), true );
        graphPanel.useLabelFilterPopup( new String[] { labelManager.getLabel(TXT_FILTER_ALL_SKILLS),
                                                       labelManager.getLabel(TXT_FILTER_OVERALL),
                                                       "GK", "DEF", "MID", "ATT" },
                                        new int[] { 2046, 2053, 208, 304, 624, 1072 } );

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = PADDING_INSETS;
        add(playerSelectorCombo, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = NO_INSETS;
        add(jbNotes, gbc);
        gbc.insets = PADDING_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(dateSelectorCombo, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(ratingsScroll, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        add(playerSummaryPanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 2;
        add(graphPanel, gbc);
    }

    private void setColumnWidths() {
        TableColumn column = null;
        //DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        RatingsTableRenderer tcr = new RatingsTableRenderer();
        int cC = ratingsTable.getColumnModel().getColumnCount();
        for (int i = 0; i < cC; i++) {
            column = ratingsTable.getColumnModel().getColumn(i);
            if (i==0) column.setPreferredWidth(80);
            else column.setPreferredWidth(40);
            column.setCellRenderer(tcr);
        }

    }

    private void createVarLabels() {
        nameVarLabel = new JLabel("", JLabel.CENTER);
        nameVarLabel.setOpaque(true);
        nameVarLabel.setBackground(Color.BLACK);
        nameVarLabel.setForeground(Color.WHITE);
        nameVarLabel.setPreferredSize(DIM_LABEL);
        nameVarLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        ageVarLabel = createLabel("  " + labelManager.getLabel(TXT_CH_AGE) + ": ");
        flagVarLabel = createVarLabel();
        flagVarLabel.setHorizontalAlignment(JLabel.CENTER);
        valueVarLabel = createVarLabel();
        formVarLabel = createVarLabel();
        stamVarLabel = createVarLabel();
        paceVarLabel = createVarLabel();
        techVarLabel = createVarLabel();
        passVarLabel = createVarLabel();
        salaryVarLabel = createVarLabel();
        healthVarLabel = createLabel(" ");
        keepVarLabel = createVarLabel();
        defeVarLabel = createVarLabel();
        playVarLabel = createVarLabel();
        scorVarLabel = createVarLabel();
        cardsVarLabel = createLabel(" ");
        healthVarLabel.setHorizontalAlignment(JLabel.CENTER);
        cardsVarLabel.setHorizontalAlignment(JLabel.CENTER);
    }
    private JLabel createVarLabel() {
        JLabel lbl = new JLabel();
        lbl.setHorizontalAlignment(JLabel.RIGHT);
        lbl.setPreferredSize(DIM_LABEL);
//         lbl.setMaximumSize(new java.awt.Dimension((int)DIM_LABEL.getWidth()+30, (int)DIM_LABEL.getHeight()));
        lbl.setMaximumSize(DIM_LABEL);
        lbl.setMinimumSize(DIM_LABEL);
        lbl.setBackground(alternateBackColor());
        lbl.setOpaque(true);
        return lbl;
    }
    private JLabel createLabel(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setBackground(alternateBackColor());
        lbl.setOpaque(true);
        return lbl;
    }
    private Color alternateBackColor() {
        Color c = null;
        if (auxColorBack == 0) c = Color.WHITE;
        else if (auxColorBack == 1) c = Color.GRAY.brighter();
        auxColorBack = 1 - auxColorBack;
        return c;
    }

    private JPanel createPlayerSummaryPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        
        JLabel lblValue  = createLabel("  " + labelManager.getLabel(TXT_CH_VALUE));
        JLabel lblForm   = createLabel("  " + labelManager.getLabel(TXT_FORM));
        JLabel lblStam   = createLabel("  " + labelManager.getLabel(TXT_STAMINA));
        JLabel lblPace   = createLabel("  " + labelManager.getLabel(TXT_PACE));
        JLabel lblTech   = createLabel("  " + labelManager.getLabel(TXT_TECHNIQUE));
        JLabel lblPass   = createLabel("  " + labelManager.getLabel(TXT_PASSING));
        JLabel lblSalary = createLabel("  " + labelManager.getLabel(TXT_CH_SALARY));
        auxColorBack = 1 - auxColorBack;
        JLabel lblKeep   = createLabel("  " + labelManager.getLabel(TXT_KEEPER));
        JLabel lblDefe   = createLabel("  " + labelManager.getLabel(TXT_DEFENDER));
        JLabel lblPlay   = createLabel("  " + labelManager.getLabel(TXT_PLAYMAKER));
        JLabel lblScor   = createLabel("  " + labelManager.getLabel(TXT_SCORER));

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        //gbc.insets = new Insets(0, LEFT_INSETS.left, 0, RIGHT_INSETS.right);
        gbc.insets = RIGHT_INSETS;
        gbc.ipadx = 4;
        p.add(nameVarLabel, gbc);
        // ----------------------
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        //gbc.insets = LEFT_INSETS;
        gbc.insets = NO_INSETS;
        p.add(ageVarLabel, gbc);
        // ----------------------
        p.add(lblValue, gbc);
        // ----------------------
        p.add(lblSalary, gbc);
        // ----------------------
        p.add(lblForm, gbc);
        // ----------------------
        p.add(lblStam, gbc);
        p.add(lblPace, gbc);
        p.add(lblTech, gbc);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        p.add(lblPass, gbc);
        // ----------------------
        // ----------------------
        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.insets = RIGHT_INSETS;
        p.add(flagVarLabel, gbc);
        // ----------------------
        p.add(valueVarLabel, gbc);
        // ----------------------
        p.add(salaryVarLabel, gbc);
        // ----------------------
        p.add(formVarLabel, gbc);
        // ----------------------
        p.add(stamVarLabel, gbc);
        p.add(paceVarLabel, gbc);
        p.add(techVarLabel, gbc);
        p.add(passVarLabel, gbc);
        // ----------------------
        // ----------------------
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = NO_INSETS;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 4;
        gbc.ipadx = 0;
        p.add(faceCanvas, gbc);
        // ----------------------
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = LEFT_INSETS;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.ipadx = 4;
        p.add(cardsVarLabel, gbc);
        // ----------------------
        p.add(lblKeep, gbc);
        p.add(lblDefe, gbc);
        p.add(lblPlay, gbc);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        p.add(lblScor, gbc);
        // ----------------------
        // ----------------------
        gbc.gridx = 3;
        gbc.gridheight = 1;
        //gbc.insets = RIGHT_INSETS;
        gbc.insets = NO_INSETS;
        p.add(healthVarLabel, gbc);
        // ----------------------
        p.add(keepVarLabel, gbc);
        p.add(defeVarLabel, gbc);
        p.add(playVarLabel, gbc);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        p.add(scorVarLabel, gbc);

        return p;
    }

    private void showPlayerData(PlayerProfile p, Date d) {
        nameVarLabel.setText( p.getFullName() );
        if (p.isNTPlayer()) nameVarLabel.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_NT_PLAYER));
        else nameVarLabel.setIcon(null);
        flagVarLabel.setIcon( so.gui.MainFrame.getFlagIcon(p.getCountryFrom()) );
        ageVarLabel.setText( "  " + labelManager.getLabel(TXT_CH_AGE) + ": " + p.getAge(d) );
        valueVarLabel.setText( intFormat.format(p.getValue(d)*options.getCurrencyConversionRate() ) + " " +
                               options.getCurrencySymbol() + "  " );
        salaryVarLabel.setText( intFormat.format(p.getSalary(d)*options.getCurrencyConversionRate()) + " " +
                                options.getCurrencySymbol() + "  " );
        formVarLabel.setText( getSkillLevel(p.getForm(d), p.getForm()) );
        switch (p.getCards(d)) { // how many yellow cards, 3=red
        case 1:
            cardsVarLabel.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_YELLOWCARD));
            break;
        case 2:
            cardsVarLabel.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_YELLOWCARD2));
            break;
        case 3:
            cardsVarLabel.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_REDCARD));
            break;
        default:
            cardsVarLabel.setIcon(null);
        }
        int injuryDays = p.getInjuryDays(d);
        if (injuryDays>0) {
            healthVarLabel.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_INJURY));
            healthVarLabel.setText(Integer.toString(injuryDays));
        }
        else {
            healthVarLabel.setIcon(null);
            healthVarLabel.setText("");
        }
        stamVarLabel.setText( getSkillLevel(p.getStamina(d), p.getStamina()) );
        paceVarLabel.setText( getSkillLevel(p.getPace(d), p.getPace()) );
        techVarLabel.setText( getSkillLevel(p.getTechnique(d), p.getTechnique()) );
        passVarLabel.setText( getSkillLevel(p.getPassing(d), p.getPassing()) );
        keepVarLabel.setText( getSkillLevel(p.getKeeper(d), p.getKeeper()) );
        defeVarLabel.setText( getSkillLevel(p.getDefender(d), p.getDefender()) );
        playVarLabel.setText( getSkillLevel(p.getPlaymaker(d), p.getPlaymaker()) );
        scorVarLabel.setText( getSkillLevel(p.getScorer(d), p.getScorer()) );
    }

    private void fillPlayerSelectorCombo() {
        List<PlayerProfile> _currentPlayers = roster.getPlayersList();
        if (_currentPlayers==null || _currentPlayers.size()==0) ; //return;
        else {
            for (PlayerProfile _p : _currentPlayers) playerSelectorCombo.addItem(_p);
            javax.swing.JPopupMenu.Separator sep = new javax.swing.JPopupMenu.Separator();
            sep.setPreferredSize(new java.awt.Dimension(0,6));
            playerSelectorCombo.addItem(sep);
            List<PlayerProfile> _formerPlayers = roster.getFormerPlayersList();
            if (_formerPlayers != null) for (PlayerProfile _p : _formerPlayers) playerSelectorCombo.addItem(_p);
        }
    }

    public void refreshData() {
        playerSelectorCombo.removeItemListener(this);
        playerSelectorCombo.removeAllItems();
        fillPlayerSelectorCombo();
        playerSelectorCombo.addItemListener(this);
        playerSelectorCombo.setSelectedIndex(-1);
        dateSelectorCombo.removeAllItems();
        jbNotes.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_MANAGER_NO_NOTES));
        graphPanel.setData(null);
        faceCanvas.setBack(0);
        faceCanvas.setCode(0,0);
        faceCanvas.repaint();
    }
    public void refreshRatingsTable() {
        tableModel.fireTableDataChanged();
    }
    public void refreshNotesIcon() {
        PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
        if (p==null) return;
        if (p.hasManagerNotes()) jbNotes.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_MANAGER_NOTES));
        else jbNotes.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_MANAGER_NO_NOTES));
    }

    public void showPlayer(PlayerProfile p) {
        if (p==null) return;
        playerSelectorCombo.setSelectedItem(p);
    }

    /* ========= interface GraphPanel.GraphDateListener ========= */
    /* change in graph week */
    public void dateChanged(Date newDate) {
        if (newDate == null) return;
        dateSelectorCombo.setSelectedItem( new FormattedDateHolder(newDate) );
    }

    /* ========= interface ActionListener ========= */
    /* "notes" Button pressed */
    public void actionPerformed(ActionEvent actionEvent) {
        PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
        if (p==null) return;
        p.showManagerNotesEditor();
    }
    /* ========= interface ItemListener ========= */
    /* change in Player selector or Date selector */
    public void itemStateChanged(java.awt.event.ItemEvent ie) {
        if (ie.getItem()==null) return;
        if (ie.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (ie.getSource().equals(playerSelectorCombo)) {
                dateSelectorCombo.removeAllItems();
                if (ie.getItem() instanceof javax.swing.JPopupMenu.Separator) {
                    faceCanvas.clear();
                    playerSelectorCombo.setSelectedItem(null);
                    graphPanel.setData(null);
                    tableModel.fireTableDataChanged();
                    return;
                }
                PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
                if (p==null) return;
                Vector<Date> dates = new Vector<Date>(p.getDates());
                if (dates==null || dates.size()==0) return;
                graphPanel.setData(mixedPlayerData = new MixedPlayerData(p)); // could improve efficiency
                java.util.Collections.reverse(dates);
                for (Date d : dates) dateSelectorCombo.addItem(new FormattedDateHolder(d));
                tableModel.fireTableDataChanged();
                if (p.hasManagerNotes()) jbNotes.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_MANAGER_NOTES));
                else jbNotes.setIcon(so.gui.MainFrame.getImageIcon(FILENAME_IMG_MANAGER_NO_NOTES));
            }
            else if (ie.getSource().equals(dateSelectorCombo)) {
                PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
                if (p==null) return;
                int _pos = p.getPreferredPosition();
                if (_pos == NO_POSITION) _pos = p.getBestPosition();
                _pos = _pos & ~(P_LEFT | P_RIGHT | RESERVE);
                faceCanvas.setBack(_pos);
                faceCanvas.setCode(p.getId(), p.getCountryFrom());
                faceCanvas.repaint();
                Date d = ((FormattedDateHolder)dateSelectorCombo.getSelectedItem()).getDate();
                showPlayerData(p, d);
                graphPanel.setCurrentWeekAgo(SokkerCalendar.getWeeksAgo(d));
//                 /* select row in ratings table */
//                 int idx = matchRepo.getPlayer(p.getId()).getIndexOfLastMatchInWeek(d);
            }
        }
    }
    /* ======== interface MouseListener ======== */
    /* click on the ratings table */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2) {
            int row = ratingsTable.getSelectedRow();
            if (row==-1) return;
            PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
            if (p == null) return;
            List<MatchRepository.PlayerProfile.PlayerStats> pslist = matchRepo.getPlayerStats(p.getId());
            if (pslist == null) return;
            Date d = pslist.get( tableSorter.modelIndex(row) ).getDate();
            d = p.getDataDatePreviousTo(d);
            dateChanged(d);
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    // ----------------------------------------

    private String getSkillLevel(int sk, int s0) {
        int diff = s0 - sk;
        String strDiff = "";
        if (diff>0) strDiff = "<FONT color=GREEN>+" + diff + "</FONT>";
        else if (diff<0) strDiff = "<FONT color=RED>" + diff + "</FONT>";
        return "<html>" + labelManager.getSkillLevelName(sk) + " (" + sk + strDiff + ")&nbsp;&nbsp;</html>";
    }

    /* ####################################################################### */
    /* ####################################################################### */
    private class RatingsTableRenderer extends DefaultTableCellRenderer {
        DateFormat formatter;

        RatingsTableRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            formatter = DateFormat.getDateInstance();
        }

        public void setValue(Object value) {
            if (value == null) setText("");
            else if (value instanceof Date) setText(formatter.format(value));
            else if (value instanceof Float) setText( Integer.toString(Math.round( ((Float)value).floatValue() )) + " %");
            else setText(value.toString());
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            //super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = tableSorter.modelIndex(row);
            setFont(table.getFont());
            setValue(value);
            PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
            Color bg = table.getBackground();
            Color fg = table.getForeground();
            if (p != null) {
                try {
                    List<MatchRepository.PlayerProfile.PlayerStats> lps = matchRepo.getPlayerStats(p.getId());
                    if (lps != null) {
                        switch( matchRepo.getMatch(lps.get(modelRow).getMatchId()).getType() ) {
                            /* green */
                        case MATCH_CUP:
                            bg = COLOR_LIGHT_GREEN;
                            break;
                            /* blue */
                        case MATCH_QUALIFICATION:
                            bg = COLOR_LIGHT_BLUE;
                            break;
                            /* yellow */
                        case MATCH_FRIENDLY_NORMAL:
                        case MATCH_FRIENDLY_CUPRULES:
                        case MATCH_FRIENDLY_LEAGUE:
                            bg = COLOR_LIGHT_YELLOW;
                            break;
                            /* red */
                        case MATCH_NT_QUALIFICATION:
                        case MATCH_NT_WORLDCUP:
                        case MATCH_NT_FRIENDLY:
                            bg = COLOR_LIGHT_PINK;
                            break;
                            /* normal, white */
                        case MATCH_LEAGUE:
                        default:
                        }
                        if (isSelected) {
                            bg = bg.darker();
                            fg = table.getSelectionForeground();
                        }
                    }
                } catch (NullPointerException npe) { }
            }
            setForeground(fg);
            setBackground(bg);
            return this;
        }
    }

    /* ####################################################################### */
    private class RatingsTableModel extends AbstractTableModel {

        public RatingsTableModel() {
            super();
        }
        public int getRowCount() {
            PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
            if (p == null) return 0;
            List<MatchRepository.PlayerProfile.PlayerStats> ps = matchRepo.getPlayerStats(p.getId());
            if (ps == null) return 0;
            return ps.size();
        }
        public int getColumnCount() { return PLAYERRATINGSCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            PlayerProfile p = (PlayerProfile)playerSelectorCombo.getSelectedItem();
            if (p == null) return "";
            List<MatchRepository.PlayerProfile.PlayerStats> lps = matchRepo.getPlayerStats(p.getId());
            if (lps == null) return "";
            return lps.get(row).getData(column);
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  0: return labelManager.getLabel(TXT_CH_DATE);
            case  1: return labelManager.getLabel(TXT_CH_RATING);
            case  2: return labelManager.getLabel(TXT_CH_POSITION);
            case  3: return labelManager.getLabel(TXT_CH_ORDER);
            case  4: return labelManager.getLabel(TXT_CH_TIME);
            case  5: return labelManager.getLabel(TXT_CH_OFF);
            case  6: return labelManager.getLabel(TXT_CH_DEF);
            case  7: return labelManager.getLabel(TXT_CH_GOALS);
            case  8: return labelManager.getLabel(TXT_CH_ASSISTS);
            case  9: return labelManager.getLabel(TXT_CH_SHOTS);
            case 10: return labelManager.getLabel(TXT_CH_FOULS);
            default:
                return " ";
            }
        }
    }

    /* ####################################################################### */
    /* ####################################################################### */
    private class MixedPlayerData implements so.data.GraphicableData {
        private PlayerProfile player;
        private MatchRepository.PlayerProfile mrpp;

        MixedPlayerData(PlayerProfile pp) {
            player = pp;
            mrpp = matchRepo.getPlayer(pp.getId());
        }

        private void setPlayer(PlayerProfile pp) {
            player = pp;
            mrpp = matchRepo.getPlayer(pp.getId());
        }

        public int size() { return player.getGraphicableData().size(); }
        public int getFieldsCount() { return player.getGraphicableData().getFieldsCount() + 1; }
        public int getFieldScale(int idx) {
            if (idx == 0) return 1000;
            return 17;
        }
        public String getTitle() { return player.getFullName(); }
        public Date getDate(int week) { return player.getGraphicableData().getDate(week); }
        public int getData(int week, int idx) {
            if (idx == player.getGraphicableData().getFieldsCount()) { // ==11
                if (mrpp == null) return 0;
                else return mrpp.getWeeklyStats(week).getMatchesCount();
                //else return mrpp.getMatchesInWeek(week);
            }
            else return player.getGraphicableData().getData(week, idx);
        }
    }

}
