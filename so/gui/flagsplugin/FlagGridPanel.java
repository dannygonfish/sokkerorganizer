package so.gui.flagsplugin;
/**
 * FlagGridPanel.java
 *
 * @author Daniel González Fisher
 */

import so.data.CountryDataUnit;
import so.data.MatchRepository;
import so.text.LabelManager;
import so.util.Dialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Date;
import java.text.DateFormat;
import static so.Constants.Labels.*;

public class FlagGridPanel extends JPanel implements MouseListener {
    public static final int T_AWAY = 1;
    public static final int T_HOME = 2;

    private LabelManager labelManager;
    private MatchRepository matchRepo;
    private JList lista;
    private JPanel grid;
    private TreeSet<CountryDataUnit> flagCollection;
    private HashMap teams;
    private int type;

    public FlagGridPanel(LabelManager lm, MatchRepository repo, TreeSet<CountryDataUnit> flags, int t) {
        super(new java.awt.GridLayout(0,FlagsPlugin.FLAGS_PER_ROW,3,6));
        labelManager = lm;
        matchRepo = repo;
        flagCollection = flags;
        setOpaque(false);
        type = t;
        intRefreshFlags();
        refreshTitle();
    }

    /* called from FlagsPlugin.createScroll */
    public void setList(JList l) {
        lista = l;
        lista.addMouseListener(this);
    }

    /* ok */
    private FlagLabel createMyFlag(CountryDataUnit cdu) {
        FlagLabel fl = new FlagLabel(cdu);
        fl.addMouseListener(this);
        return fl;
    }

    /* ok */
    public void refreshFlags() {
        intRefreshFlags();
        refreshTitle();
        revalidate();
        repaint();
    }

    /* ok */
    protected void intRefreshFlags() {
        removeAll();
        for (CountryDataUnit cdu : flagCollection) add(createMyFlag(cdu));
    }

    /* ok */
    public void refreshTitle() {
        int q = flagCollection.size();
        String title = '(' + Integer.toString(q) + ' ' + labelManager.getPluralityLabel(TXT_FP_FLAG, q!=1) + ')';
        setBorder( BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title) );
    }

    /* ok */
    public boolean addFlag(CountryDataUnit cdu) {
        boolean added = flagCollection.add(cdu);
        if (added) refreshFlags();
        return added;
    }
    /* ok */
    public boolean removeFlag(CountryDataUnit cdu) {
        boolean res = flagCollection.remove(cdu);
        if (res) refreshTitle();
        return res;
    }

    /* ok, called from FlagRenderer */
    public boolean contains(Object pais) {
        return flagCollection.contains(pais);
    }

    /* ok */
    public boolean addAll(Collection<CountryDataUnit> col) {
        boolean added = flagCollection.addAll(col);
        if (added) refreshFlags();
        return added;
    }
    /* ok */
    public boolean removeAll(Collection<CountryDataUnit> col) {
        boolean removed = false;
        for (CountryDataUnit cdu : col) removed = flagCollection.remove(cdu) || removed;
        if (removed) refreshFlags();
        return removed;
    }

    public Collection<CountryDataUnit> getMissing(Collection<CountryDataUnit> c) {
        ArrayList<CountryDataUnit> missing = new ArrayList<CountryDataUnit>();
        for (CountryDataUnit cdu : c)
            if (!flagCollection.contains(cdu)) missing.add(cdu);
        return missing;
    }
    public Collection<CountryDataUnit> getSurplus(Collection c) {
        ArrayList<CountryDataUnit> surplus = new ArrayList<CountryDataUnit>();
        for (CountryDataUnit cdu : flagCollection)
            if (!c.contains(cdu)) surplus.add(cdu);
        return surplus;
    }

    protected JComponent createTripsPanel(CountryDataUnit cdu, int minWidth) {
        Box box = new Box(BoxLayout.Y_AXIS);
        List<MatchRepository.MatchData> matches = null;
        if (type == T_HOME) matches = matchRepo.getMatchesHostingCountry(cdu.getId());
        else matches = matchRepo.getMatchesVisitingCountry(cdu.getId());
        for (MatchRepository.MatchData md : matches) {
            if (!md.isFriendly()) continue;
            Box tripInfo = new Box(BoxLayout.X_AXIS);
            tripInfo.add(createDateLabel(md.getDate()));
            tripInfo.add(Box.createHorizontalStrut(8));
            if (type == T_HOME) tripInfo.add(new JLabel( md.getVisitorName() ));
            else tripInfo.add(new JLabel( md.getLocalName() ));
            tripInfo.add(Box.createHorizontalStrut(10));
            tripInfo.add(Box.createHorizontalGlue());
            if (type == T_HOME) tripInfo.add(createScoreLabel(md.getScore().getLocal(), md.getScore().getVisitor()));
            else tripInfo.add(createScoreLabel(md.getScore().getVisitor(), md.getScore().getLocal()));
            box.add(tripInfo);
        }

        JPanel jp = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(Box.createHorizontalStrut(minWidth + 24), gbc);

        gbc.insets = new Insets(3,4,3,4);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.gridwidth = 1;
        jp.add(new JLabel(cdu.getFlag()));

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        jp.add(box, gbc);

        return jp;
    }

    /* ok, maybe use the DateFormat as a shared static field */
    protected JComponent createDateLabel(Date timestamp) {
        String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(timestamp);
        JLabel label = new JLabel(date);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    /* ok */
    protected JComponent createScoreLabel(int myGoals, int oppoGoals) {
        String score = Integer.toString(myGoals) + " : " + Integer.toString(oppoGoals);
        JLabel label = new JLabel(score);
        if (myGoals > oppoGoals) label.setForeground(Color.BLUE);
        else if (myGoals < oppoGoals) label.setForeground(Color.RED);
        return label;
    }

    /* *********************** */
    /* Interface MOUSELISTENER */
    public void mouseClicked(MouseEvent e) {
        if (lista==null) return;
        FlagLabel fl;
        switch (e.getButton()) {
        case MouseEvent.BUTTON3:
            if (e.getClickCount()>1 || !(e.getSource() instanceof FlagLabel)) return;
            fl = (FlagLabel)e.getSource();
            String flagname = fl.getFlagName();
            String title = labelManager.getExtendedLabel(TXT_FP_TRIPTO, flagname);
            if (type == T_HOME) title = labelManager.getExtendedLabel(TXT_FP_VISITFROM, flagname);

            Dialog jdMatches = new Dialog(title, false);
            jdMatches.setLayout(new java.awt.FlowLayout());
            JComponent jcomp = createTripsPanel(fl.getCountryDataUnit(),
                                                (int) (new JLabel(title)).getPreferredSize().getWidth() );
            jdMatches.add(jcomp);
            jdMatches.pack();
            jdMatches.setLocationRelativeTo(fl);
            jdMatches.setResizable(false);
            jdMatches.setVisible(true);
            return;
        case MouseEvent.BUTTON1:
            if ((FlagsPlugin.DOUBLECLICK_SELECTION == true) && (e.getClickCount() != 2)) return;

            // Add flag
            if (e.getSource() == lista) {
                CountryDataUnit cdu = (CountryDataUnit)lista.getSelectedValue();
                addFlag(cdu);
                lista.repaint();
            }
            // Remove flag
            else if (e.getSource() instanceof FlagLabel) {
                fl = (FlagLabel)e.getSource();
                if ( removeFlag(fl.getCountryDataUnit()) ) {
                    remove(fl);
                    revalidate();
                    repaint();
                    lista.repaint();
                }
            }
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

}
