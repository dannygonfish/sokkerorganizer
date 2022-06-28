package so.gui.flagsplugin;
/**
 * PlayersFlagPanel.java
 *
 * @author Daniel González Fisher
 */

import so.data.CountryDataUnit;
import so.text.LabelManager;
import so.data.PlayerRoster;
import so.data.PlayerProfile;
import so.util.Dialog;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import static so.Constants.Labels.*;


public class PlayersFlagPanel extends JPanel implements MouseListener {
    static protected int T_CURRENT = 1;
    static protected int T_OLD = 2;

    private LabelManager labelManager;
    private PlayerRoster roster;
    private HashMap<Integer,Integer> playersPerCountryId; // countryId, # of players
    private String title;
    protected int type;
    private JPanel flagsGrid;

    public PlayersFlagPanel(LabelManager lm, PlayerRoster pr, int t) {
        //super(new java.awt.GridLayout(0, FlagsPlugin.FLAGS_PER_ROW, 3, 6));
        flagsGrid = new JPanel(new java.awt.GridLayout(0, FlagsPlugin.FLAGS_PER_ROW, 3, 6));
        labelManager = lm;
        roster = pr;
        type = t;
        playersPerCountryId = new HashMap<Integer,Integer>();
        if (type == T_CURRENT) title = lm.getLabel(TXT_FP_CUR_P_FLAGS);
        else if (type == T_OLD) title = lm.getLabel(TXT_FP_FOR_P_FLAGS);
        setOpaque(false);
        add(flagsGrid);
        countPlayers();
        refreshFlags();
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
        flagsGrid.removeAll();
        ArrayList<CountryDataUnit> cdus = new ArrayList<CountryDataUnit>();
        for (int cid : playersPerCountryId.keySet()) cdus.add(new CountryDataUnit(cid));
        java.util.Collections.sort(cdus);
        for (CountryDataUnit cdu : cdus) {
            FlagLabel fl = new FlagLabel(cdu);
            fl.addMouseListener(this);
            int np = playersPerCountryId.get(cdu.getId());
            fl.setToolTipText(cdu.getName() + ", " + np + ' ' + labelManager.getPluralityLabel(TXT_FP_PLAYER, np!=1));
            flagsGrid.add(fl);
        }
    }

    /* ok */
    public void countPlayers() {
        playersPerCountryId.clear();
        if (roster == null) return;
        List<PlayerProfile> players = null;
        if (type == T_CURRENT) players = roster.getPlayersList();
        else if (type == T_OLD) players = roster.getFormerPlayersList();
        if (players==null || players.size()==0) return;
        int cid, n;
        for (PlayerProfile pp : players) {
            cid = pp.getCountryFrom();
            n = 0;
            if (playersPerCountryId.containsKey(cid)) n = playersPerCountryId.get(cid);
            playersPerCountryId.put(cid, n+1);
        }
        if (type == T_OLD) {
            for (PlayerProfile pp : roster.getPlayersList()) {
                cid = pp.getCountryFrom();
                if (playersPerCountryId.containsKey(cid)) playersPerCountryId.remove(cid);
            }
        }
    }

//     public boolean contains(String pais) {
//         return playersPerCountryId.containsKey(pais);
//     }

    /* ok */
    public void refreshTitle() {
        if (title == null) return;
        Border btitulo = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),title);
        int n = playersPerCountryId.size();
        Border bcontador = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),"(" + n + ' ' + labelManager.getPluralityLabel(TXT_FP_FLAG, n!=1) + ')');
        setBorder(BorderFactory.createCompoundBorder(btitulo,bcontador));
        java.awt.Dimension dim = new java.awt.Dimension( (new JLabel(title)).getPreferredSize() );
        dim.setSize(dim.getWidth()+20, dim.getHeight()*2 + 2 + flagsGrid.getHeight());
        setMinimumSize(dim);
    }

    /* ok */
    protected JComponent createPlayerPanel(CountryDataUnit cdu, int minWidth) {
        Box box = new Box(BoxLayout.Y_AXIS);
        List<PlayerProfile> plist = null;
        if (type == T_CURRENT) plist = roster.getPlayersList();
        else /*if (type == T_OLD)*/ plist = roster.getFormerPlayersList();
        for (PlayerProfile pp : plist) {
            if (pp.getCountryFrom()==cdu.getId()) box.add(new JLabel(pp.getFullName()));
        }

        JPanel jp = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(Box.createHorizontalStrut(minWidth + 24), gbc);

        gbc.insets = new Insets(3,4,3,4);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.gridwidth = 1;
        jp.add(new JLabel(cdu.getFlag()), gbc);

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        jp.add(box, gbc);

        return jp;
    }

    /* *********************** */
    /* Interface MOUSELISTENER */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (e.getClickCount() > 1) return;
            FlagLabel fl = (FlagLabel)e.getSource();
            String dtitle = "";
            if (type == T_OLD) dtitle = labelManager.getExtendedLabel(TXT_FP_FOR_P_FROM, fl.getFlagName());
            else dtitle = labelManager.getExtendedLabel(TXT_FP_CUR_P_FROM, fl.getFlagName());
            Dialog jdPlayers = new Dialog(dtitle, false);
            jdPlayers.getContentPane().setLayout(new java.awt.FlowLayout());
            JComponent jcomp = createPlayerPanel(fl.getCountryDataUnit(),
                                                 (int) (new JLabel(dtitle)).getPreferredSize().getWidth() );
            jdPlayers.getContentPane().add(jcomp);
            jdPlayers.pack();
            jdPlayers.setLocationRelativeTo(fl);
            jdPlayers.setResizable(false);
            jdPlayers.setVisible(true);
            return;
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

}
