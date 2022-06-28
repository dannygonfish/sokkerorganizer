package so.gui;

import static so.Constants.Labels.*;
import so.data.*;
import so.text.LabelManager;
import so.config.Options;
import java.util.List;
import java.util.HashMap;
import java.text.NumberFormat;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.JTabbedPane;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Insets;
import java.beans.PropertyChangeListener;

public class LeagueTablePanel extends JPanel implements MouseListener, LeagueGraphPanel.RoundChangeListener, ChangeListener, ItemListener {
    private static final String VALUE = "value";

    private LabelManager labelManager;
    private Options options;
    private LeagueEncapsulator leagues;
    private LeagueDetails league;

    private int displayRound;
    private boolean fixtureMode;
    private JTable table;
    private LeagueTableModel tableModel;
    private LeagueTableSorter tableSorter;
    private FixturePanel fixPanel;
    private LeagueGraphPanel graphPanel;
    private JComboBox leagueSelectorCombo;

    public LeagueTablePanel(LabelManager lm, Options opt, LeagueEncapsulator le) {
        super(new GridBagLayout());
        labelManager = lm;
        options = opt;
        leagues = le;
        league = leagues.getMyLatestLeague();
        if (league == null) displayRound = 0;
        else displayRound = league.getRound();
        fixtureMode = false;

        leagueSelectorCombo = new JComboBox();
        leagueSelectorCombo.setPreferredSize(new Dimension(120,16));
        fillLeagueSelectorCombo();
        leagueSelectorCombo.setSelectedItem(league);

        tableModel = new LeagueTableModel();
        tableSorter = new LeagueTableSorter(tableModel);
        tableSorter.setSortingStatus(9, -1);
        tableSorter.setSortingStatus(8, -1);
        tableSorter.setSortingStatus(6, -1);
        tableSorter.setSortingStatus(0, 1);

        table = new JTable( tableSorter );
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        setColumnWidths();
        //table.setPreferredScrollableViewportSize(new Dimension(500, 132));
        so.util.Utils.setJTableVisibleRowCount(table, 8);
        table.addMouseListener(this);

        JScrollPane tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.setMinimumSize(new Dimension(500, 150));
        tableScroll.setPreferredSize(new Dimension(500, 150));
        graphPanel = new LeagueGraphPanel();
        graphPanel.setToolTips(lm.getLabel(TXT_TT_ROUND), lm.getLabel(TXT_TT_PLACE));
        graphPanel.setColors(new Color[] { Color.RED, Color.GREEN.darker(), new Color(120,120,255), Color.ORANGE,
                                               Color.MAGENTA, Color.PINK, Color.ORANGE.darker(), Color.GRAY } );
        graphPanel.setRoundChangeListener(this);
        refreshGraph();

        fixPanel = new FixturePanel();
        JScrollPane fixtureScroll = new JScrollPane(fixPanel);
        JTabbedPane tabbedPane = new JTabbedPane();
        /* tabbedPane.addTab( label, icon, component, tooltip ); */
        tabbedPane.addTab( lm.getLabel(TXT_GRAPH), null, graphPanel, null );
        tabbedPane.addTab( lm.getLabel(TXT_FIXTURE), null, fixtureScroll, null );
        tabbedPane.addChangeListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,16,10,16);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(leagueSelectorCombo, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        add(tableScroll, gbc);
        gbc.weighty = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        add(tabbedPane, gbc);

        leagueSelectorCombo.addItemListener(this);
    }

    private void setColumnWidths() {
        TableColumn column = null;
        LeagueTableCellRenderer ltcr = new LeagueTableCellRenderer();
        int cC = table.getColumnModel().getColumnCount();
        for (int i = 0; i < cC; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 1) {
                column.setPreferredWidth(240);
                column.setCellRenderer(new LeagueTableCellRenderer(javax.swing.JLabel.LEFT));
            } else {
                column.setPreferredWidth(40);
                column.setCellRenderer(ltcr);
            }
        }

    }

    private void fillLeagueSelectorCombo() {
        List<LeagueDetails> _ldl = leagues.getLeagues();
        if (_ldl==null || _ldl.size()==0) ; //return;
        else {
            java.util.Collections.reverse(_ldl);
            for (LeagueDetails _ld : _ldl) leagueSelectorCombo.addItem(_ld);
        }
    }

    public void refreshData() {
        leagueSelectorCombo.removeItemListener(this);
        leagueSelectorCombo.removeAllItems();
        fillLeagueSelectorCombo();
        league = leagues.getMyLatestLeague();
        leagueSelectorCombo.setSelectedItem(league);
        leagueSelectorCombo.addItemListener(this);
        refreshView();
    }
    protected void refreshView() {
        // the order is important!!!
        displayRound = league.getRound();
        fixPanel.refreshFixture();
        tableModel.clearProjectedData();
        refreshTable();
        refreshGraph();
        repaint();
    }


    public void refreshGraph() {
        if (league!=null) {
            List<LeagueDetails.TeamProfile> teamList = league.getTeamsList(0);
            String nameLabels [] = new String[teamList.size()];
            for (int i=0; i<teamList.size(); i++) nameLabels[i] = teamList.get(i).getName();
            graphPanel.setLabels( nameLabels );
            graphPanel.setCurrentRound( displayRound );
            graphPanel.setData( league.getGraphicableData() );
        }
    }
    public void refreshTable() {
        tableModel.fireTableDataChanged();
    }

    /* interface MouseListener */
    public void mouseClicked(MouseEvent e) {
        if (league==null) return;
        try {
            //int lsp = league.getTeamsList(displayRound).get( table.getSelectedRow() ).getLastSeasonPos() - 1;
            if (table.getSelectedRow()<0) return;
            int lsp = ((Integer)tableModel.getValueAt( tableSorter.modelIndex(table.getSelectedRow()), 0 )).intValue()-1;
            graphPanel.setSelectedTeam( lsp );
        } catch (NullPointerException npe) { }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    /* interface RoundChangeListener */
    public void roundChanged(int newRound) {
        if (league==null || newRound<0 || league.getRound()<newRound) return;
        graphPanel.setCurrentRound( newRound );
        displayRound = newRound;
        refreshTable();
        graphPanel.repaint();
    }

    /* interface ChangeListener */
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        int idx = ((JTabbedPane)e.getSource()).getSelectedIndex();
        if (idx==0) {
            /* grafico */
            fixtureMode = false;
            refreshTable();
        }
        else if (idx==1) {
            /* fixture */
            fixtureMode = true;
            refreshTable();
        }
    }
    /* interface ItemListener */
    public void itemStateChanged(java.awt.event.ItemEvent ie) {
        if (ie.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (ie.getSource().equals(leagueSelectorCombo)) {
                league = (LeagueDetails)leagueSelectorCombo.getSelectedItem();
                refreshView();
            }
        }
    }

    // ===============================================================================
    protected class LeagueTableModel extends javax.swing.table.AbstractTableModel {
        private HashMap<Integer,ProjectedData> idToProjectedData;

        public LeagueTableModel() {
            super();
            idToProjectedData = new HashMap<Integer,ProjectedData>();
        }

        public int getRowCount() { return 8; }

        public int getColumnCount() { return 10; }

        public Object getValueAt(int row, int column) {
            //moved to tableSorter.- if (column == 0) return new Integer(row+1);
            if (league==null) return "";
            //% ACA INTERCEPT si se ponen resultados futuros
            if (fixtureMode) {
                int tid = league.getTeamsList( league.getRound() ).get(row).getId();
                Object ret = league.getTeamsList( league.getRound() ).get(row).getData(league.getRound(), column);
                if (column==0 || column==1 || !idToProjectedData.containsKey(tid)) return ret;
                return (Integer)ret + idToProjectedData.get(tid).getInteger(column);
            }
            else return league.getTeamsList(displayRound).get(row).getData(displayRound, column);
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int col) {
            switch(col) {
            case  1: return " ";
            case  2: return labelManager.getLabel(TXT_CH_MATCHESPLAYED);
            case  3: return labelManager.getLabel(TXT_CH_MATCHWINS);
            case  4: return labelManager.getLabel(TXT_CH_MATCHDRAWS);
            case  5: return labelManager.getLabel(TXT_CH_MATCHLOSSES);
            case  6: return labelManager.getLabel(TXT_CH_GOALS_FAVOUR);
            case  7: return labelManager.getLabel(TXT_CH_GOALS_AGAINST);
            case  8: return labelManager.getLabel(TXT_CH_GOAL_DIFFERENCE);
            case  9: return labelManager.getLabel(TXT_CH_POINTS);
            default:
                return " ";
            }
        }

        public void clearProjectedData() {
            idToProjectedData.clear();
        }

        public void removeProjectedMatch(int round, int lid, int vid) {
            boolean refresh = false;
            if (idToProjectedData.containsKey(lid)) refresh = idToProjectedData.get(lid).removeMatchData(round) || refresh;
            if (idToProjectedData.containsKey(vid)) refresh = idToProjectedData.get(vid).removeMatchData(round) || refresh;
            if (refresh) fireTableDataChanged();
        }

        public void putProjectedMatch(LeagueDetails.MatchData md) {
            int ltid = md.getLocalTID();
            int vtid = md.getVisitorTID();
            if (idToProjectedData.containsKey(ltid)) idToProjectedData.get(ltid).putMatchData(md);
            else idToProjectedData.put(ltid, new ProjectedData(ltid, md));
            if (idToProjectedData.containsKey(vtid)) idToProjectedData.get(vtid).putMatchData(md);
            else idToProjectedData.put(vtid, new ProjectedData(vtid, md));
            //refreshTable
            fireTableDataChanged();
        }
    }
    // ===============================================================================
    private static class ProjectedData {
        private int teamId;
        private HashMap<Integer,LeagueDetails.MatchData> matches;
        private int matchCount;
        private int wins;
        private int draws;
        private int losses;
        private int goalsFavour;
        private int goalsAgainst;

        public ProjectedData(int tid) {
            teamId = tid;
            matches = new HashMap<Integer,LeagueDetails.MatchData>();
            matchCount = 0;
            wins = 0;
            draws = 0;
            losses = 0;
            goalsFavour = 0;
            goalsAgainst = 0;
        }
        public ProjectedData(int tid, LeagueDetails.MatchData md) {
            this(tid);
            putMatchData(md);
        }

        public boolean removeMatchData(int round) {
            if (!matches.containsKey(round)) return false;
            matches.remove(round);
            updateData();
            return true;
        }
        public void putMatchData(LeagueDetails.MatchData md) {
            matches.put(md.getRound(), md);
            updateData();
        }

        private void updateData() {
            matchCount = 0;
            wins = 0;
            draws = 0;
            losses = 0;
            goalsFavour = 0;
            goalsAgainst = 0;
            for (LeagueDetails.MatchData md : matches.values()) {
                boolean local = (teamId == md.getLocalTID());
                int gf = local ? md.getLocalGoals()   : md.getVisitorGoals();
                int ga = local ? md.getVisitorGoals() : md.getLocalGoals();
                matchCount++;
                if (gf > ga) wins++;
                else if (gf < ga) losses++;
                else draws++;
                goalsFavour += gf;
                goalsAgainst += ga;
            }
        }

        public Integer getInteger(int col) {
            switch(col) {
            case 2: return matchCount;
            case 3: return wins;
            case 4: return draws;
            case 5: return losses;
            case 6: return goalsFavour;
            case 7: return goalsAgainst;
            case 8: return (goalsFavour-goalsAgainst);
            case 9: return (wins*3 + draws);
            default:
                return 0;
            }
        }
    }
    // ===============================================================================
    private static class LeagueTableSorter extends so.util.TableSorter {
        public LeagueTableSorter(javax.swing.table.TableModel model) {
            super(model);
        }

        public Object getValueAt(int row, int column) {
            if (column==0) return Integer.valueOf(row+1);
            return super.getValueAt(row, column);
        }
    }
    // ===============================================================================
    private static class LeagueTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public LeagueTableCellRenderer() {
            this(javax.swing.JLabel.CENTER);
        }
        public LeagueTableCellRenderer(int alignment) {
            super();
            setHorizontalAlignment(alignment);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 9) setFont( table.getFont().deriveFont(Font.BOLD) );
            if (row == 0) setBackground( so.Constants.Colors.COLOR_LIGHT_GREEN );
            else if (row>0 && row <4) setBackground( so.Constants.Colors.COLOR_LIGHT_YELLOW );
            else setBackground( so.Constants.Colors.COLOR_LIGHT_PINK );
            if (isSelected) setBackground( getBackground().darker() );
            return c;
        }
    }

    // ===============================================================================
    private class FixturePanel extends JPanel {
        java.util.ArrayList<RoundPanel> panels;

        public FixturePanel() {
            super( new GridLayout(7,2,8,8) );
            panels = new java.util.ArrayList<RoundPanel>();
            JPanel p1, p2;
            RoundPanel r1, r2;
            for (int i=1; i<=7; i++) {
                p1 = new JPanel();
                p2 = new JPanel();
                r1 = new RoundPanel(i);
                r2 = new RoundPanel(15-i);
                p1.add(r1);
                p2.add(r2);
                add(p1);
                add(p2);
                panels.add(r1);
                panels.add(r2);
            }
        }

        public void refreshFixture() {
            for (RoundPanel rp : panels) rp.updateValues(true);
            repaint();
        }

        private class RoundPanel extends JPanel {
            private int round;
            private JLabel [] localTeam;
            private JLabel [] visitorTeam;
            private JFormattedTextField [] localScore;
            private JFormattedTextField [] visitorScore;

            public RoundPanel(int r) {
                super(new GridBagLayout());
                round = r;
                localTeam   = new JLabel[4];
                visitorTeam = new JLabel[4];
                localScore   = new JFormattedTextField[4];
                visitorScore = new JFormattedTextField[4];
                Dimension dim = new Dimension(140,20);
                for (int i=0; i<4; i++) {
                    localTeam[i]   = new JLabel();
                    visitorTeam[i] = new JLabel();
                    localTeam[i].setPreferredSize(dim);
                    visitorTeam[i].setPreferredSize(dim);
                    //visitorTeam[i].setOpaque(true);
                    //visitorTeam[i].setBackground(Color.RED);
                    localScore[i]   = createJFormattedTextField();
                    visitorScore[i] = createJFormattedTextField();
                }
                updateValues(true);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0,12,0,12);
                GridBagConstraints gbc2 = new GridBagConstraints();
                gbc2.gridwidth = 1;
                gbc2.weightx = 0.0;
                GridBagConstraints gbc3 = (GridBagConstraints)gbc.clone();
                gbc3.gridwidth = GridBagConstraints.REMAINDER;
                for (int i=0; i<4; i++) {
                    add(localTeam[i], gbc);
                    add(localScore[i], gbc2);
                    add(new JLabel(" : "), gbc2);
                    add(visitorScore[i], gbc2);
                    add(visitorTeam[i], gbc3);
                }
                setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                                                            labelManager.getLabel(TXT_TT_ROUND)+' '+round) );
            }

            private void updateValues(boolean clear) {
                if (league == null) return;
                for (int i=0; i<4; i++) {
                    LeagueDetails.MatchData match = league.getMatch(round, i+1);
                    removeScoreChangeListeners( localTeam[i] );
                    removeScoreChangeListeners( visitorTeam[i] );
                    localTeam[i].setText(league.getTeamName(match.getLocalTID()));
                    visitorTeam[i].setText(league.getTeamName(match.getVisitorTID()));
                    if (match.hasBeenPlayed()) {
                        int gl = match.getLocalGoals();
                        int gv = match.getVisitorGoals();
                        localScore[i].setValue(Integer.valueOf(gl));
                        visitorScore[i].setValue(Integer.valueOf(gv));
                        // bold the name of the winner
                        if (gl>gv) {
                            boldComponent(localTeam[i], true);
                            boldComponent(visitorTeam[i], false);
                        }
                        else if (gl<gv) {
                            boldComponent(localTeam[i], false);
                            boldComponent(visitorTeam[i], true);
                        }
                        else {
                            boldComponent(localTeam[i], false);
                            boldComponent(visitorTeam[i], false);
                        }
                        // uneditable
                        localScore[i].setEditable(false);
                        visitorScore[i].setEditable(false);
                        localScore[i].setFocusable(false);
                        visitorScore[i].setFocusable(false);
                    }
                    else {
                        ScoreChangeListener scl = new ScoreChangeListener(round, i+1, localScore[i], visitorScore[i],
                                                                          match.getLocalTID(), match.getVisitorTID());
                        localScore[i].setEditable(true);
                        visitorScore[i].setEditable(true);
                        localScore[i].setFocusable(true);
                        visitorScore[i].setFocusable(true);
                        boldComponent(localTeam[i], false);
                        boldComponent(visitorTeam[i], false);
                        if (clear) {
                            localScore[i].setText("");
                            visitorScore[i].setText("");
                        }
                        localScore[i].addPropertyChangeListener(VALUE, scl);
                        visitorScore[i].addPropertyChangeListener(VALUE, scl);
                    }
                }
            }

        } // end class RoundPanel

    }

    // ===============================================================================
    private class ScoreChangeListener implements PropertyChangeListener {
        private int round;
        private int nir;
        private JFormattedTextField localTf;
        private JFormattedTextField visitorTf;
        private int localId;
        private int visitorId;

        public ScoreChangeListener(int r, int n, JFormattedTextField ltf, JFormattedTextField vtf, int lid, int vid) {
            round = r;
            nir = n;
            localTf = ltf;
            visitorTf = vtf;
            localId = lid;
            visitorId = vid;
        }

        /* interface PropertyChangeListener */
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(VALUE)) {
                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                if (oldValue==null && newValue==null) return;
                //% System.out.println("cond : " + localTf.getText().equals("") + " : " + visitorTf.getText().equals(""));
                if (localTf.getText().equals("") || visitorTf.getText().equals("")) {
                    tableModel.removeProjectedMatch(round, localId, visitorId);
                }
                else {
                    //try {
                    int gl = ((Integer)localTf.getValue()).intValue();
                    int gv = ((Integer)visitorTf.getValue()).intValue();
                    tableModel.putProjectedMatch(new LeagueDetails.MatchData(0,round,localId,visitorId,gl,gv,false,nir));
                    //} catch
                }
            }
        }

    }

    // ===============================================================================
    private static void boldComponent(javax.swing.JComponent comp, boolean bold) {
        Font f = comp.getFont();
        int style = bold?Font.BOLD:Font.PLAIN;
        f = f.deriveFont(style);
        comp.setFont(f);
    }
    private static JFormattedTextField createJFormattedTextField() {
        javax.swing.text.NumberFormatter nfr = new javax.swing.text.NumberFormatter(NumberFormat.getIntegerInstance())
            {
                public Object stringToValue(String text) throws java.text.ParseException {
                    if (text==null || text.equals("")) return "";
                    return super.stringToValue(text);
                }
                public String valueToString(Object value) throws java.text.ParseException {
                    if (value==null || value.equals("")) return "";
                    return super.valueToString(value);
                }
            };
        nfr.setMinimum(0);
        nfr.setMaximum(99);
        JFormattedTextField ftf = new JFormattedTextField(nfr);
        ftf.setPreferredSize(new Dimension(20,20));
        ftf.setHorizontalAlignment(JFormattedTextField.CENTER);
        return ftf;
    }
    private static void removeScoreChangeListeners(java.awt.Component comp) {
        PropertyChangeListener [] pclArray = comp.getPropertyChangeListeners(VALUE);
        for (PropertyChangeListener pcl : pclArray) {
            if (pcl instanceof ScoreChangeListener) comp.removePropertyChangeListener(VALUE, pcl);
        }
    }

}
