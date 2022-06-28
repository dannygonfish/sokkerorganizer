package so.gui;

import static so.Constants.*;
import static so.Constants.Labels.*;
import static so.Constants.Colors.*;
import so.data.*;
import so.text.LabelManager;
import so.config.Options;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import so.util.TableSorter;
import java.util.TreeSet;
import javax.swing.table.*;
import javax.swing.BorderFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;


public class MatchesPanel extends JPanel implements ItemListener, PropertyChangeListener {

    private LabelManager labelManager;
    private Options options;
    private TeamDetails teamDetails;
    private MatchRepository matchRepo;

    private JTable matchesTable;
    private MatchesTableModel tableModel;
    private TableSorter tableSorter;
    private NumberFormat numberFormat;
    private NumberFormat percentFormat;

    private JCheckBox chkLocal;
    private JCheckBox chkVisitor;
    private JCheckBox chkVictory;
    private JCheckBox chkDefeat;
    private JCheckBox chkDraw;
    private JCheckBox chkNational;
    private JCheckBox chkInternational;
    private JCheckBox chkOfficial;
    private JCheckBox chkFriendly;
    private JCheckBox chkLeague;
    private JCheckBox chkCup;
    private JCheckBox chkQualification;
    private JComboBox cbxLeague;
    private JComboBox cbxOpponent;
    private JDateChooser jdcStartDate;
    private JDateChooser jdcEndDate;
    private JCheckBox chkStartDate;
    private JCheckBox chkEndDate;

    private JLabel lblMatchesTotal;
    private JLabel lblMatchesWon;
    private JLabel lblMatchesDrawn;
    private JLabel lblMatchesLost;
    private JLabel lblGoalsFavour;
    private JLabel lblGoalsAgainst;
    private JLabel lblGoalDiff;
    private JLabel lblAvgSpectators;
    private JLabel lblAvgPossession;
    private JLabel lblAvgPlayInHalf;
    private JLabel lblTotShots;
    private JLabel lblTotFouls;
    private JLabel lblTotCardsYellow;
    private JLabel lblTotCardsRed;
    private JLabel lblAvgScoring;
    private JLabel lblAvgPassing;
    private JLabel lblAvgDefending;
    private JLabel lblShootingEff;

    private java.util.List<MatchRepository.MatchData> matchesList;

    public MatchesPanel(LabelManager lm, Options opt, TeamDetails team, MatchRepository repo) {
        super(new GridBagLayout());
        labelManager = lm;
        options = opt;
        teamDetails = team;
        matchRepo = repo;
        matchesList = null;
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);

        lblMatchesTotal = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblMatchesWon = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblMatchesDrawn = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblMatchesLost = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblGoalsFavour = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblGoalsAgainst = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblGoalDiff = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblAvgSpectators = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblAvgPossession = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblAvgPlayInHalf = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblTotShots = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblTotFouls = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblTotCardsYellow = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblTotCardsRed = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblAvgScoring = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblAvgPassing = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblAvgDefending = createStLabel("0", JLabel.TRAILING, Color.WHITE);
        lblShootingEff = createStLabel("0", JLabel.TRAILING, Color.WHITE);

        tableModel = new MatchesTableModel();
        tableSorter = new TableSorter(tableModel);
        matchesTable = new JTable();
        matchesTable.setModel(tableSorter);
        tableSorter.setTableHeader(matchesTable.getTableHeader());
        //matchesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        matchesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        initColumns();
        matchesTable.getTableHeader().setReorderingAllowed(false);
        createGUI();
        refreshData();
     }

    private void createGUI() {
        chkVictory       = new JCheckBox(labelManager.getLabel(TXT_VICTORY), true);
        chkDraw          = new JCheckBox(labelManager.getLabel(TXT_DRAW), true);
        chkDefeat        = new JCheckBox(labelManager.getLabel(TXT_DEFEAT), true);
        chkLocal         = new JCheckBox(labelManager.getLabel(TXT_LOCAL), true);
        chkVisitor       = new JCheckBox(labelManager.getLabel(TXT_VISITOR), true);
        chkNational      = new JCheckBox(labelManager.getLabel(TXT_NATIONAL), true);
        chkInternational = new JCheckBox(labelManager.getLabel(TXT_INTERNATIONAL), true);
        chkOfficial      = new JCheckBox(labelManager.getLabel(TXT_OFFICIAL), true);
        chkFriendly      = new JCheckBox(labelManager.getLabel(TXT_FRIENDLY), true);
        chkLeague        = new JCheckBox(labelManager.getLabel(TXT_LEAGUE), true);
        chkCup           = new JCheckBox(labelManager.getLabel(TXT_CUP), true);
        chkQualification = new JCheckBox(labelManager.getLabel(TXT_QUALI), true);
        JPanel vddPanel = new JPanel(new GridBagLayout());
        vddPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel lvPanel  = new JPanel(new GridBagLayout());
        lvPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel niPanel  = new JPanel(new GridBagLayout());
        niPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel ofPanel  = new JPanel(new GridBagLayout());
        ofPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel lcqPanel = new JPanel(new GridBagLayout());
        lcqPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel filterPanel = new JPanel(new GridBagLayout());
        JPanel filter1Panel = new JPanel(new GridBagLayout());
        filter1Panel.setBorder(BorderFactory.createEtchedBorder());
        JPanel filter2Panel = new JPanel(new GridBagLayout());
        filter2Panel.setBorder(BorderFactory.createEtchedBorder());
        JPanel statsPanel  = new JPanel(new GridBagLayout());
        statsPanel.setBorder(BorderFactory.createEtchedBorder());
        cbxLeague = new JComboBox();
        cbxOpponent = new JComboBox();
        chkStartDate = new JCheckBox(labelManager.getLabel(TXT_DATE_BEGIN), false);
        chkEndDate = new JCheckBox(labelManager.getLabel(TXT_DATE_END), false);
        Date _today = new Date();
        jdcStartDate = new JDateChooser(new Date(_today.getTime()-9676800000L)); /* 16 weeks before */
        jdcEndDate = new JDateChooser(_today);
        javax.swing.ListCellRenderer lcr = new FilterComboBoxRenderer();
        cbxLeague.setRenderer(lcr);
        cbxOpponent.setRenderer(lcr);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        vddPanel.add(chkVictory, gbc);
        vddPanel.add(chkDraw, gbc);
        vddPanel.add(chkDefeat, gbc);
        lvPanel.add(chkLocal, gbc);
        lvPanel.add(chkVisitor, gbc);
        niPanel.add(chkNational, gbc);
        niPanel.add(chkInternational, gbc);
        ofPanel.add(chkOfficial, gbc);
        ofPanel.add(chkFriendly, gbc);
        gbc.gridwidth = 1;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        lcqPanel.add(chkLeague, gbc);
        lcqPanel.add(chkCup, gbc);
        lcqPanel.add(chkQualification, gbc);

        /* filter Panels */
        filter1Panel.add(new JLabel(labelManager.getLabel(TXT_COMPETITION)), gbc);
        filter1Panel.add(cbxLeague, gbc);
        filter1Panel.add(javax.swing.Box.createHorizontalGlue(), gbc);
        filter1Panel.add(new JLabel(labelManager.getLabel(TXT_OPPONENT)), gbc);
        filter1Panel.add(cbxOpponent, gbc);
        filter2Panel.add(chkStartDate, gbc);
        filter2Panel.add(jdcStartDate, gbc);
        filter2Panel.add(javax.swing.Box.createHorizontalGlue(), gbc);
        filter2Panel.add(javax.swing.Box.createHorizontalStrut(20), gbc);
        filter2Panel.add(chkEndDate, gbc);
        filter2Panel.add(jdcEndDate, gbc);

        filterPanel.add(filter2Panel, gbc);
        filterPanel.add(filter1Panel, gbc);

        /* stats Panel */
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        GridBagConstraints gbc2 = (GridBagConstraints)gbc.clone();
        GridBagConstraints gbc3 = (GridBagConstraints)gbc.clone();
        gbc3.weightx = 1.0;
        gbc.insets = new java.awt.Insets(0, 4, 0, 2);
        gbc2.insets = new java.awt.Insets(0, 2, 0, 4);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_MATCHES_TOTAL), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_MATCHES_WON), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_MATCHES_DRAWN), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_MATCHES_LOST), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        gbc2.gridx = 1;
        gbc.gridheight = 1;
        statsPanel.add(lblMatchesTotal, gbc2);
        statsPanel.add(lblMatchesWon, gbc2);
        statsPanel.add(lblMatchesDrawn, gbc2);
        statsPanel.add(lblMatchesLost, gbc2);

        gbc3.gridheight = GridBagConstraints.REMAINDER;
        gbc3.gridx = 2;
        statsPanel.add(javax.swing.Box.createGlue(), gbc3);

        gbc.gridx = 3;
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_GOALS_FAVOUR), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_GOALS_AGAINST), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_GOAL_DIFFERENCE), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_AVG_ATTENDANCE), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        gbc2.gridx = 4;
        statsPanel.add(lblGoalsFavour, gbc2);
        statsPanel.add(lblGoalsAgainst, gbc2);
        statsPanel.add(lblGoalDiff, gbc2);
        statsPanel.add(lblAvgSpectators, gbc2);

        gbc3.gridx = 5;
        statsPanel.add(javax.swing.Box.createGlue(), gbc3);

        gbc.gridx = 6;
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_SHOTS), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_FOULS), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_YELLOW_CARDS), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_RED_CARDS), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        gbc2.gridx = 7;
        statsPanel.add(lblTotShots, gbc2);
        statsPanel.add(lblTotFouls, gbc2);
        statsPanel.add(lblTotCardsYellow, gbc2);
        statsPanel.add(lblTotCardsRed, gbc2);

        gbc3.gridx = 8;
        statsPanel.add(javax.swing.Box.createGlue(), gbc3);

        gbc.gridx = 9;
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_SHOOT_EFFICIENCY), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_AVG_POSSESSION), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_AVG_PLAYINHALF), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(javax.swing.Box.createGlue(), gbc);
        gbc2.gridx = 10;
        statsPanel.add(lblShootingEff, gbc2);
        statsPanel.add(lblAvgPossession, gbc2);
        statsPanel.add(lblAvgPlayInHalf, gbc2);
        statsPanel.add(javax.swing.Box.createGlue(), gbc2);

        gbc3.gridx = 11;
        statsPanel.add(javax.swing.Box.createGlue(), gbc3);

        gbc.gridx = 12;
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_AVG_SCORING), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_AVG_PASSING), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(createStLabel(labelManager.getLabel(TXT_AVG_DEFENDING), JLabel.LEADING, COLOR_LIGHT_YELLOW), gbc);
        statsPanel.add(javax.swing.Box.createGlue(), gbc);
        gbc2.gridx = 13;
        statsPanel.add(lblAvgScoring, gbc2);
        statsPanel.add(lblAvgPassing, gbc2);
        statsPanel.add(lblAvgDefending, gbc2);
        statsPanel.add(javax.swing.Box.createGlue(), gbc2);

        /* main */
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        gbc.weightx = 1.0;
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        add(vddPanel, gbc);
        gbc.gridheight = 2;
        add(lvPanel, gbc);
        add(niPanel, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(ofPanel, gbc);
        gbc.gridheight = 1;
        add(lcqPanel, gbc);
        add(filterPanel, gbc);
        gbc.gridheight = GridBagConstraints.RELATIVE;
        add(statsPanel, gbc);

        JScrollPane scroll = new JScrollPane(matchesTable);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        add(scroll, gbc);

        chkLocal.addItemListener(this);
        chkVisitor.addItemListener(this);
        chkVictory.addItemListener(this);
        chkDraw.addItemListener(this);
        chkDefeat.addItemListener(this);
        chkNational.addItemListener(this);
        chkInternational.addItemListener(this);
        chkOfficial.addItemListener(this);
        chkFriendly.addItemListener(this);
        chkLeague.addItemListener(this);
        chkCup.addItemListener(this);
        chkQualification.addItemListener(this);
        cbxLeague.addItemListener(this);
        cbxOpponent.addItemListener(this);
        chkStartDate.addItemListener(this);
        chkEndDate.addItemListener(this);
        jdcStartDate.addPropertyChangeListener("date", this);
        jdcEndDate.addPropertyChangeListener("date", this);
    }

    private JLabel createStLabel(String txt, int halign, Color bgc) {
        JLabel lbl = new JLabel("  " + txt + "  ", halign);
        lbl.setOpaque(true);
        lbl.setBackground(bgc);
        return lbl;
    }

    private void initColumns() {
        TableColumn column = null;
        MatchesTableRenderer mtr = new MatchesTableRenderer();
        int cC = matchesTable.getColumnModel().getColumnCount();
        for (int i = 0; i < cC; i++) {
            column = matchesTable.getColumnModel().getColumn(i);
            column.setCellRenderer(mtr);
            switch (i) {
            case 0:  /* date */
                column.setPreferredWidth(80);
                break;
            case 1: /* league */
            case 2: /* local team */
            case 4: /* visitor team */
            case 7: /* opponent */
                column.setPreferredWidth(120);
                break;
            case 3: /* score */
                column.setPreferredWidth(40);
                break;
            case 6: /* weather */
                column.setPreferredWidth(40);
                break;
            case 5:  /* spectators */
            case 8: /* matchid */
            default:
                column.setPreferredWidth(60);
            }
        }
    }

    public void refreshData() {
        refreshComboBoxes();
        refreshTable();
        refreshStats();
    }

    public void refreshTable() {
        int type = (chkLeague.isSelected()?MT_LEAGUE:0) | (chkCup.isSelected()?MT_CUP:0) |
            (chkQualification.isSelected()?MT_QUALI:0);
        int official = (chkOfficial.isSelected()?MT_OFFICIAL:0) | (chkFriendly.isSelected()?MT_FRIENDLY:0);
        int localy = (chkLocal.isSelected()?MT_LOCAL:0) | (chkVisitor.isSelected()?MT_VISITOR:0);
        int result = (chkVictory.isSelected()?MT_VICTORY:0) | (chkDraw.isSelected()?MT_DRAW:0) |
            (chkDefeat.isSelected()?MT_DEFEAT:0);
        int nat = (chkNational.isSelected()?MT_NATIONAL:0) | (chkInternational.isSelected()?MT_INTERNATIONAL:0);
        int lid = -1;
        int oppoId = -1;
        Date startDate = chkStartDate.isSelected() ? jdcStartDate.getDate() : null;
        Date endDate   = chkEndDate.isSelected() ? jdcEndDate.getDate() : null;
        Object _item;
        try{ 
            if (cbxLeague.getSelectedIndex()>0) {
                _item = cbxLeague.getSelectedItem();
                if (_item instanceof Integer) lid = ((Integer)_item).intValue();
                else if (_item instanceof MatchRepository.LeagueData) {
                    lid = ((MatchRepository.LeagueData)cbxLeague.getSelectedItem()).getId();
                }
            }
        } catch (Exception e) { }
        try {
            if (cbxOpponent.getSelectedIndex()>0) {
                oppoId = ((MatchRepository.TeamProfile)cbxOpponent.getSelectedItem()).getId();
            }
        } catch (Exception e) { }
        matchesList = matchRepo.getMatchesForTeam(teamDetails.getId(), type, official, localy, result, nat, lid, oppoId, startDate, endDate);
        tableModel.fireTableDataChanged();
    }

    public void refreshComboBoxes() {
        int tid = teamDetails.getId();
        java.util.List<MatchRepository.MatchData> ml = matchRepo.getMatchesForTeam(tid);
        TreeSet<Integer> leagueIds = new TreeSet<Integer>();
        TreeSet<Integer> oppoIds = new TreeSet<Integer>();
        cbxLeague.removeItemListener(this);
        cbxOpponent.removeItemListener(this);
        cbxLeague.removeAllItems();
        cbxOpponent.removeAllItems();
        cbxLeague.addItem("");
        cbxOpponent.addItem("");
        for (MatchRepository.MatchData md : ml) {
            leagueIds.add(md.getLeagueId());
            oppoIds.add(md.getOpponentId(tid));
        }
        for (Integer lid : leagueIds) {
            if (lid==3 || lid==200) continue;
            MatchRepository.LeagueData ld = matchRepo.getLeague(lid);
            if (ld == null) cbxLeague.addItem(lid);
            else cbxLeague.addItem(ld);
        }
        TreeSet<MatchRepository.TeamProfile> opponents =
            new TreeSet<MatchRepository.TeamProfile>(new java.util.Comparator<MatchRepository.TeamProfile>() {
                public int compare(MatchRepository.TeamProfile tp1, MatchRepository.TeamProfile tp2) {
                    return tp1.getName().compareToIgnoreCase(tp2.getName());
                }
            } );

        for (Integer oid : oppoIds) {
            MatchRepository.TeamProfile tp = matchRepo.getTeam(oid);
            if (tp == null) continue;
            opponents.add(tp);
        }
        for (MatchRepository.TeamProfile tp : opponents) {
            cbxOpponent.addItem(tp);
        }
        cbxLeague.setSelectedIndex(0);
        cbxOpponent.setSelectedIndex(0);
        cbxLeague.addItemListener(this);
        cbxOpponent.addItemListener(this);
    }

    public void refreshStats() {
        int _tid = teamDetails.getId();
        int matTot = 0;
        int matWon = 0;
        int matDraw = 0;
        int matLost = 0;
        int gF = 0;
        int gA = 0;
        int gDiff = 0;
        float avgSpect = 0.0f;
        int totShots = 0;
        int totFouls = 0;
        int totYellows = 0;
        int totReds = 0;
        float shootEff = 0.0f;
        float avgPoss = 0.0f;
        float avgPlayInHalf = 0.0f;
        float avgScoring = 0.0f;
        float avgPassing = 0.0f;
        float avgDefending = 0.0f;
        Score score;
        MatchRepository.TeamProfile.TeamStats ts;
        for (MatchRepository.MatchData md : matchesList) {
            matTot++;
            score = md.getScore();
            if (md.isLocalTeam(_tid)) {
                gF += score.getLocal();
                gA += score.getVisitor();
                if (score.getDifference() > 0) matWon++;
                else if (score.getDifference() == 0) matDraw++;
                else matLost++;
            }
            else {
                gF += score.getVisitor();
                gA += score.getLocal();
                if (score.getDifference() < 0) matWon++;
                else if (score.getDifference() == 0) matDraw++;
                else matLost++;   
            }
            avgSpect += md.getSpectators();
            ts = md.getTeamStats(_tid);
            if (ts==null) continue;
            totShots += ts.getShots();
            totFouls += ts.getFouls();
            totYellows += ts.getYellowCards();
            totReds += ts.getRedCards();
            avgPoss += ts.getPossession();
            avgPlayInHalf += ts.getPlayInHalf();
            avgScoring += ts.getRatingScoring();
            avgPassing += ts.getRatingPassing();
            avgDefending += ts.getRatingDefending();
        }
        gDiff = gF - gA;
        avgSpect /= (float)matTot;
        shootEff = (float)gF / (float)totShots;
        avgPoss /= (float)matTot;
        avgPlayInHalf /= (float)matTot;
        avgScoring /= (float)matTot;
        avgPassing /= (float)matTot;
        avgDefending /= (float)matTot;

        lblMatchesTotal.setText("  " + Integer.toString(matTot) + "  ");
        lblMatchesWon.setText("  " + Integer.toString(matWon) + "  ");
        lblMatchesDrawn.setText("  " + Integer.toString(matDraw) + "  ");
        lblMatchesLost.setText("  " + Integer.toString(matLost) + "  ");
        lblGoalsFavour.setText("  " + Integer.toString(gF) + "  ");
        lblGoalsAgainst.setText("  " + Integer.toString(gA) + "  ");
        lblGoalDiff.setText("  " +  (gDiff<0?"":'+') + Integer.toString(gDiff) + "  ");
        lblTotShots.setText("  " + Integer.toString(totShots) + "  ");
        lblTotFouls.setText("  " + Integer.toString(totFouls) + "  ");
        lblTotCardsYellow.setText("  " + Integer.toString(totYellows) + "  ");
        lblTotCardsRed.setText("  " + Integer.toString(totReds) + "  ");
        lblAvgSpectators.setText("  " + numberFormat.format(avgSpect) + "  ");
        lblAvgPossession.setText("  " + percentFormat.format(avgPoss) + "  ");
        lblAvgPlayInHalf.setText("  " + percentFormat.format(avgPlayInHalf) + "  ");
        lblAvgScoring.setText("  " + labelManager.getSkillLevelName(Math.round(avgScoring)) + "  ");
        lblAvgPassing.setText("  " + labelManager.getSkillLevelName(Math.round(avgPassing)) + "  ");
        lblAvgDefending.setText("  " + labelManager.getSkillLevelName(Math.round(avgDefending)) + "  ");
        lblShootingEff.setText("  " + percentFormat.format(shootEff) + "  ");
    }

    /* interface ItemListener */
    public void itemStateChanged(ItemEvent ie) {
        refreshTable();
        refreshStats();
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
		if (evt.getPropertyName().equals("date")) {
            if (evt.getSource().equals(jdcStartDate) && chkStartDate.isSelected() ||
                evt.getSource().equals(jdcEndDate) && chkEndDate.isSelected()) {
                refreshTable();
                refreshStats();
            }
        }
    }
    /* ####################################################################### */
    /* ####################################################################### */
    private class FilterComboBoxRenderer extends javax.swing.DefaultListCellRenderer {
        private MatchRepository.LeagueData _ld;
        private MatchRepository.TeamProfile _tp;

        public FilterComboBoxRenderer() {
            super();
        }

        public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setIcon(null);
            Component c = super.getListCellRendererComponent(list, " ", index, isSelected, cellHasFocus);
            if (value == null) return c;
            if (value instanceof MatchRepository.TeamProfile) {
                _tp = (MatchRepository.TeamProfile)value;
                setText("[" + _tp.getId() + "] " + _tp.getName());
                //if (_tp.getCountryId() != teamDetails.getCountryId()) setIcon(MainFrame.getFlagIcon(_tp.getCountryId()));
                setIcon(MainFrame.getFlagIcon(_tp.getCountryId()));
            }
            else if (value instanceof MatchRepository.LeagueData) {
                _ld = (MatchRepository.LeagueData)value;
                setText(_ld.getName());
            }
            else if (value instanceof Integer) {
                setText(labelManager.getLabel(TXT_LEAGUE) + ' ' + ((Integer)value).toString());
            }
            return c;
        }

    }

    /* ####################################################################### */
    private class MatchesTableRenderer extends DefaultTableCellRenderer {
        DateFormat dateFormatter;
        NumberFormat intFormatter;

        MatchesTableRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            dateFormatter = DateFormat.getDateInstance();
            intFormatter = NumberFormat.getIntegerInstance();
        }

        public void setValue(Object value) {
            setIcon(null);
            if (value == null) setText("");
            else if (value instanceof Date) setText(dateFormatter.format(value));
            else if (value instanceof Long) setText(intFormatter.format( ((Long)value).longValue() ));
            else if (value instanceof Float) {
                int tid = ((Float)value).intValue();
                MatchRepository.TeamProfile tp = matchRepo.getTeam(tid);
                if (tp == null) setText( Integer.toString(tid) );
                else {
                    setText(tp.getName());
                    if (tp.getCountryId() != teamDetails.getCountryId()) setIcon(MainFrame.getFlagIcon(tp.getCountryId()));
                }
            }
            //else if (value instanceof Byte) setText(  );
            else setText(value.toString());
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            //super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = tableSorter.modelIndex(row);
            setFont(table.getFont());
            setValue(value);
            MatchRepository.MatchData md = matchesList.get(modelRow);
            Color bg = table.getBackground();
            Color fg = table.getForeground();
            switch (md.getType()) {
            case MATCH_CUP:
                bg = COLOR_LIGHT_GREEN;
                break;
            case MATCH_QUALIFICATION:
                bg = COLOR_LIGHT_BLUE;
                break;
            case MATCH_FRIENDLY_NORMAL:
            case MATCH_FRIENDLY_CUPRULES:
            case MATCH_FRIENDLY_LEAGUE:
                bg = COLOR_LIGHT_YELLOW;
                break;
            case MATCH_LEAGUE:
            default:
            }
            boolean local = md.isLocalTeam(teamDetails.getId());
            int aux = md.getScore().getDifference() * (local?1:-1);
            if (aux > 0) {
                if (local && column==2) setFont(table.getFont().deriveFont(Font.BOLD));
                else if (!local && column==4) setFont(table.getFont().deriveFont(Font.BOLD));
                else if (column == 3) fg = Color.GREEN.darker();
            }
            else if (aux<0) {
                if (local && column==4) setFont(table.getFont().deriveFont(Font.BOLD));
                else if (!local && column==2) setFont(table.getFont().deriveFont(Font.BOLD));
                else if (column==3) fg = Color.RED;
            }
            if (column == 8) fg = Color.GRAY;
            else if (column == 3) setFont(table.getFont().deriveFont(Font.BOLD));

            if (isSelected) {
                bg = bg.darker();
                //fg = table.getSelectionForeground();
            }
            setForeground(fg);
            setBackground(bg);
            switch (column) {
            case 7:
                setHorizontalAlignment(JLabel.LEADING);
                break;
            case 8:
                setHorizontalAlignment(JLabel.TRAILING);
                break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            default:
                setHorizontalAlignment(JLabel.CENTER);
            }
            return this;
        }
    }
    /* ####################################################################### */
    private class MatchesTableModel extends AbstractTableModel {
        private Object [][] cache;
        private int rows;
        private int cols;

        public MatchesTableModel() {
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
                        cache[r][c] = matchesList.get(r).getData(c, teamDetails.getId());
                    }
                }
            }
            else cache = null;
        }

        /* */
        public int getRowCount() {
            if (matchesList==null) return 0;
            return matchesList.size();
        }
        public int getColumnCount() { return MATCHESCOLUMNS_COUNT; }

        public Object getValueAt(int row, int column) {
            //return roster.getPlayersList().get(row).getData(column);
            if (cache!=null && row<rows && column<cols) return cache[row][column];
            else return "";
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  0: return labelManager.getLabel(TXT_CH_DATE);
            case  1: return labelManager.getLabel(TXT_COMPETITION);
            case  2: return labelManager.getLabel(TXT_LOCAL);
            case  3: return labelManager.getLabel(TXT_SCORE);
            case  4: return labelManager.getLabel(TXT_VISITOR);
            case  5: return labelManager.getLabel(TXT_SPECTATORS);
            case  6: return labelManager.getLabel(TXT_WEATHER);
            case  7: return labelManager.getLabel(TXT_OPPONENT);
            case  8: return "matchID";
            default:
                return " ";
            }
        }

        public void fireTableDataChanged() {
            rebuildDataCache();
            super.fireTableDataChanged();
        }

    }

}
