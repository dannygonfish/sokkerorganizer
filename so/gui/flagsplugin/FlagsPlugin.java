package so.gui.flagsplugin;
/**
 * FlagsPlugin.java
 *
 * @author Daniel González Fisher
 */

import so.data.*;
import so.text.LabelManager;
import so.config.Options;
import so.util.ExampleFileFilter;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JComponent;
import java.awt.Insets;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.RenderingHints;
import static so.Constants.Labels.*;


public class FlagsPlugin extends JPanel implements ActionListener {
    public static int FLAGS_PER_ROW = 6;
    public static boolean DOUBLECLICK_SELECTION = true;
    // public static boolean OWN_FLAG = false;

    private static LogoMakerPanel LMP = null;

    private LabelManager labelManager;
    private Options options;
    private TeamDetails teamDetails;
    private PlayerRoster playerRoster;
    private MatchRepository matchRepo;

    private JList listAway, listHome;
    private FlagGridPanel fgpAway, fgpHome;
    private PlayersFlagPanel pfpCurrent, pfpFormer;
    private JButton btnUpdate, btnMakelogo;

    /** Creates a new instance of FlagsPlugin */
    public FlagsPlugin(LabelManager lm, Options opt, TeamDetails td, PlayerRoster ros, MatchRepository repo) {
        super(new java.awt.BorderLayout());
        labelManager = lm;
        options = opt;
        teamDetails = td;
        playerRoster = ros;
        matchRepo = repo;

        Vector<CountryDataUnit> paises = labelManager.getCountriesVector();
        listAway = new JList(paises);
        listHome = new JList(paises);

        fgpAway = new FlagGridPanel(lm, repo, matchRepo.getVisitedFlags(), FlagGridPanel.T_AWAY);
        fgpHome = new FlagGridPanel(lm, repo, matchRepo.getHostedFlags(), FlagGridPanel.T_HOME);
        pfpCurrent = new PlayersFlagPanel(lm, ros, PlayersFlagPanel.T_CURRENT);
        pfpFormer = new PlayersFlagPanel(lm, ros, PlayersFlagPanel.T_OLD);
        createGUI();
    }

    private void createGUI() {
        //removeAll();
        JPanel p = new JPanel(new java.awt.GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        /* command panel */
        gbc.weightx = 1.0;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(createCommandPanel(), gbc);

        /* visited Countries */
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridwidth = 1;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.NONE;
        p.add( createGrid(fgpAway, labelManager.getLabel(TXT_FP_VISITED)) , gbc );

        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.VERTICAL;
        p.add( createScroll(fgpAway, listAway) , gbc);

        /* hosted Countries */
        gbc.fill = GridBagConstraints.NONE;
        p.add( createGrid(fgpHome, labelManager.getLabel(TXT_FP_HOSTED)) , gbc );

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.VERTICAL;
        p.add( createScroll(fgpHome,listHome) , gbc);

        /* players flags */
        gbc.weighty = 0.0;
        gbc.gridwidth = 2;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        p.add(pfpCurrent, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        p.add(pfpFormer, gbc);

        add(new JScrollPane(p));
    }

    private Box createGrid(FlagGridPanel fgp, String title) {
        Box box = new Box(BoxLayout.Y_AXIS);
        box.add(Box.createHorizontalStrut(28 * FLAGS_PER_ROW));
        box.add(fgp);
        box.add(Box.createVerticalGlue());
        box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title));
        return box;
    }

    private JScrollPane createScroll(FlagGridPanel fgp, JList list) {
        FlagRenderer frender = new FlagRenderer(fgp);
        list.setCellRenderer(frender);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
//         Color fondo = new Color(214,214,192);
//         lista.setSelectionForeground(Color.WHITE);
//         lista.setSelectionBackground(fondo.darker());
//         lista.setBackground(fondo);
        fgp.setList(list);
        return new JScrollPane(list);
    }

    private JPanel createCommandPanel() {
        JPanel p = new JPanel();
        btnUpdate = new JButton(labelManager.getLabel(TXT_FP_UPDATE_FLAGS));
        btnUpdate.setToolTipText(labelManager.getLabel(TT+TXT_FP_UPDATE_FLAGS));
        btnUpdate.setFocusPainted(false);
        btnMakelogo = new JButton("Make Logo");
        btnUpdate.addActionListener(this);
        btnMakelogo.addActionListener(this);
        p.add(btnUpdate);
        //p.add(btnMakelogo);
        return p;
    }

    public void refresh() {
        pfpCurrent.countPlayers();
        pfpFormer.countPlayers();
        pfpCurrent.refreshFlags();
        pfpFormer.refreshFlags();
        doLayout();
    }

    /* ********************************************************************************** */
    /* ********************** Interface ActionListener ******************************** */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource().equals(btnMakelogo)) {
            showLogoMakerDialog();
        }
        else if (e.getSource().equals(btnUpdate)) {
            try {
                if (updateFlags()) {
                    listAway.repaint();
                    listHome.repaint();
                }
            } catch (Exception exx) {
                new so.util.DebugFrame(exx);
            }
        }
    }
    /* ********************************************************************************** */
    public void showLogoMakerDialog() {
        if (LMP != null) return;
        /* choose source */
        JFileChooser chooser = new JFileChooser();
        ExampleFileFilter filter = new ExampleFileFilter();
        filter.addExtension("png");
        filter.addExtension("jpg");
        filter.addExtension("jpeg");
        filter.addExtension("gif");
        filter.setDescription("JPEG, GIF, PNG");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Open logo Image");
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        File src = chooser.getSelectedFile();
        if (!filter.accept(src)) {
            //INVALID FILE
            JOptionPane.showMessageDialog(this, src.getName(),labelManager.getLabel(TXT_ERROR),JOptionPane.ERROR_MESSAGE);
            return;
        }
        /* load the source */
        BufferedImage logo = null;
        try {
            logo = ImageIO.read(src);
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
            return;
        }
        /* choose destination */
        filter = new ExampleFileFilter();
        filter.addExtension("png");
        filter.addExtension("jpg");
        filter.addExtension("jpeg");
        chooser = new JFileChooser(src);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        //chooser.setDialogTitle("Save as...");
        returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        File dest = chooser.getSelectedFile();
        if (!filter.accept(dest)) {
            //INVALID FILE
            JOptionPane.showMessageDialog(this, dest.getName(),labelManager.getLabel(TXT_ERROR),JOptionPane.ERROR_MESSAGE);
            return;
        }
        String extension = filter.getExtension(dest);

        /* show Logo Maker */
        LMP = new LogoMakerPanel(logo, extension);
        int dlg = JOptionPane.showConfirmDialog(this, LMP, labelManager.getLabel(TXT_OPTIONS),
                                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        BufferedImage bi = LMP.getLogo();
        LMP = null;
        if (dlg != JOptionPane.OK_OPTION) return;

        /* save */
        try{
            ImageIO.write(bi, extension, dest);
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
            return;
        }
    }

    private class LogoMakerPanel extends JPanel implements ActionListener, ChangeListener {
        private Vector<CountryDataUnit> paises;
        private java.util.TreeSet<CountryDataUnit> visitedFlags, hostedFlags;

        private BufferedImage src;
        private BufferedImage logo;
        private int type;
        private JRadioButton radVisited, radHosted;
        private JCheckBox chkTitle, chkTotal, chkPercent, chkBackgroundLogo;
        private JTextField txtTitle;
        private JSpinner spiFlagsPerRow;
        private JLabel preview, dimensions;

        public LogoMakerPanel(BufferedImage sourceImage, String format) {
            super(new java.awt.GridBagLayout());
            src = sourceImage;
            if (format.equals("png")) type = BufferedImage.TYPE_INT_ARGB;
            else type = BufferedImage.TYPE_INT_RGB;
            paises = labelManager.getCountriesVector();
            visitedFlags = matchRepo.getVisitedFlags();
            hostedFlags = matchRepo.getHostedFlags();

            //Create the radio buttons.
            radVisited = new JRadioButton(labelManager.getLabel(TXT_FP_VISITED));
            radVisited.setSelected(true);
            radHosted = new JRadioButton(labelManager.getLabel(TXT_FP_HOSTED));
            //Group the radio buttons.
            ButtonGroup group = new ButtonGroup();
            group.add(radVisited);
            group.add(radHosted);

            chkTitle = new JCheckBox();
            chkTitle.setSelected(true);
            txtTitle = new JTextField(radVisited.getText());
            chkTotal = new JCheckBox("", true);
            chkPercent = new JCheckBox("", true);
            chkBackgroundLogo = new JCheckBox("Logo as background", false); //!
            spiFlagsPerRow = new JSpinner(new javax.swing.SpinnerNumberModel(10, 6, 12, 1));
            preview = new JLabel();
            preview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            dimensions = new JLabel(" x ", JLabel.CENTER);
            setFlagSet();
            redrawLogo();
            JPanel previewPanel = new JPanel();
            previewPanel.setPreferredSize(new java.awt.Dimension(270, 290));
            previewPanel.add(preview);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(Box.createHorizontalGlue(), gbc);
            add(radVisited, gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(radHosted, gbc);

            gbc.gridwidth = 1;
            add(new JLabel("flags per row"), gbc); //!
            add(spiFlagsPerRow, gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(chkBackgroundLogo, gbc);

            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            add(chkTitle, gbc);
            gbc.weightx = 2.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtTitle, gbc);
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.CENTER;
            add(chkTotal, gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(chkPercent, gbc);

            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridheight = GridBagConstraints.RELATIVE;
            add(previewPanel, gbc);

            gbc.weighty = 0.0;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            add(dimensions, gbc);

            radVisited.addActionListener(this);
            radHosted.addActionListener(this);
            txtTitle.addActionListener(this);
            chkTitle.addActionListener(this);
            chkTotal.addActionListener(this);
            chkPercent.addActionListener(this);
            chkBackgroundLogo.addActionListener(this);
            spiFlagsPerRow.addChangeListener(this);
        }

        public BufferedImage getLogo() { return logo; }

        private void setFlagSet(){
            if (radVisited.isSelected()) {
                txtTitle.setText(radVisited.getText());
                chkTotal.setText( visitedFlags.size() + " / " + paises.size() );
                chkPercent.setText( '(' + Integer.toString(visitedFlags.size()*100/paises.size()) + "%)" );
            }
            else {
                txtTitle.setText(radHosted.getText());
                chkTotal.setText( hostedFlags.size() + " / " + paises.size() );
                chkPercent.setText( '(' + Integer.toString(hostedFlags.size()*100/paises.size()) + "%)" );
            }
        }

        private void redrawLogo() {
            makeLogo();
            preview.setIcon(new javax.swing.ImageIcon(logo));
        }

        private void makeLogo() {
            /* prepare the image */
            final boolean drawBanner = (chkTitle.isSelected() || chkTotal.isSelected() || chkPercent.isSelected());
            final boolean logoAsBackground = chkBackgroundLogo.isSelected();
            final int N = paises.size();
            final int fwidth = 19;
            final int fheight = 12;
            final int hpad = 1;
            final int vpad = 2;
            final int flagsPerRow = (Integer)spiFlagsPerRow.getValue();
            String banner = "";
            Font font = new Font("Serif", Font.PLAIN, 12);
            int txtHeight=0; //, txtWidth=0;
            int srcPad = 0;
            int flagSetPad = 0;
            if (drawBanner) {
                java.awt.Graphics2D _g = new BufferedImage(20, 20, type).createGraphics();
                banner = (chkTitle.isSelected()?txtTitle.getText()+"      ":"") +
                    (chkTotal.isSelected()?chkTotal.getText()+"      ":"") +
                    (chkPercent.isSelected()?chkPercent.getText():"");
                _g.setFont(font);
                java.awt.font.LineMetrics _lm = font.getLineMetrics(banner, _g.getFontRenderContext());
                txtHeight = (int)_lm.getHeight();
                //txtWidth = (int)font.getStringBounds(banner, _g.getFontRenderContext()).getWidth();
            }

            //final int W = Math.max(Math.max(src.getWidth(), txtWidth), (fwidth+hpad) * flagsPerRow);
            final int W = Math.max(src.getWidth(), (fwidth+hpad) * flagsPerRow);
            final boolean srcIsWider = (W==src.getWidth());
            if (srcIsWider) flagSetPad = (W - (fwidth+hpad) * flagsPerRow) / 2;
            else srcPad = (W - src.getWidth()) / 2;
            final int flagsPerSideRow = (srcPad > fwidth+hpad) ? ((srcPad)/(fwidth+hpad)) : 0;
            final int rightSideRowPad = srcPad - flagsPerSideRow*(fwidth+hpad);
            final int sideRows = logoAsBackground ? 0 : (src.getHeight()/(fheight+vpad)+1);
            final int flagsOnSideRows = flagsPerSideRow * 2 * sideRows;
            int rows = (int)Math.ceil((float)(N-flagsOnSideRows) / (float)flagsPerRow);
            int _auxh1 = Math.max(src.getHeight(), (fheight+vpad) * rows);
            int _auxh2 = (flagsOnSideRows>0)?((fheight+vpad) * (sideRows + rows)):((fheight+vpad)*rows + src.getHeight());
            final int H = txtHeight + (logoAsBackground?_auxh1:_auxh2);
            logo = new BufferedImage(W, H, type);
            java.awt.Graphics2D g = logo.createGraphics();
            /* update dimensions Label */
            dimensions.setText("<html>" + (W>230?"<font color=red>":"") + W + (W>230?"</font>":"") + " x " +
                               (H>240?"<font color=red>":"") + H + (H>240?"</font>":"") + "</html>");
            /* draw the source logo */
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(src, srcPad, 0, null);
            /* add the text */
            if (drawBanner) {
                g.setFont(font);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                if (type==BufferedImage.TYPE_INT_ARGB) g.setColor(java.awt.Color.BLACK);
                else g.setColor(java.awt.Color.WHITE);
                java.awt.font.LineMetrics _lm = font.getLineMetrics(banner, g.getFontRenderContext());
                //g.drawString(banner, 1, txtHeight - _lm.getDescent() );
                g.drawString(banner, 1, H - _lm.getDescent() );
            }
            /* generate the flags */
            int i=0, r=0, w=0, h=0;
            final AlphaComposite fade = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
            for (CountryDataUnit cdu : paises) {
                if (r<sideRows && flagsOnSideRows>0) {
                    // left side row
                    if (i%(flagsPerSideRow*2)<flagsPerSideRow) w = hpad + i%(flagsPerSideRow*2)*(fwidth+hpad);
                    // right side row
                    else w = W - srcPad + rightSideRowPad + hpad + (i%(flagsPerSideRow*2)-flagsPerSideRow) * (fwidth+hpad);
                    h = r*(fheight+vpad) + vpad/2;
                    if ( i%(flagsPerSideRow*2) == (flagsPerSideRow*2-1) ) r++;
                }
                else {
                    w = ((i-flagsOnSideRows)%flagsPerRow) * (fwidth+hpad) + hpad + flagSetPad;
                    h = r*(fheight+vpad) + vpad/2 + ((flagsOnSideRows==0 && !logoAsBackground)?src.getHeight():0);
                    if ((i-flagsOnSideRows)%flagsPerRow == flagsPerRow-1) r++;
                }
                if ((radVisited.isSelected()?visitedFlags:hostedFlags).contains(cdu)) g.setComposite(AlphaComposite.Src);
                else g.setComposite( fade );
                g.drawImage(cdu.getFlag().getImage(), w, h, null);
                i++;
            }
        }

        /* ********************** Interface ActionListener ******************************** */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (e.getSource() instanceof JRadioButton) setFlagSet();
            redrawLogo();
        }
        /* ********************** Interface ChangeListener ******************************** */
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            redrawLogo();
        }
    }

    //============================================================
    public boolean updateFlags() {
        int myTeamId = teamDetails.getId();
        List<MatchRepository.MatchData> partidos = matchRepo.getMatchesForTeam(myTeamId, so.Constants.MT_FRIENDLY);
        TreeSet<CountryDataUnit> awayFlags = new TreeSet<CountryDataUnit>();
        TreeSet<CountryDataUnit> hostedFlags = new TreeSet<CountryDataUnit>();

        for (MatchRepository.MatchData md : partidos) {
            boolean playedHome = md.isLocalTeam(myTeamId);
            int oppoId = 0, oppoCountryId = 0;
            if (playedHome) oppoId = md.getVisitorTid();
            else oppoId = md.getLocalTid();
            try {
                oppoCountryId = matchRepo.getTeam(oppoId).getCountryId();
            } catch (Exception e) { continue; }
            //boolean national = (countryId == myCountryId);
            /* add to list */
            if (playedHome) hostedFlags.add(new CountryDataUnit(oppoCountryId));
            else awayFlags.add(new CountryDataUnit(oppoCountryId));
        }

        /* get new flags */
        Collection<CountryDataUnit> newAway = fgpAway.getMissing(awayFlags);
        Collection<CountryDataUnit> newHosted = fgpHome.getMissing(hostedFlags);
        Collection<CountryDataUnit> surplusAway = fgpAway.getSurplus(awayFlags);
        Collection<CountryDataUnit> surplusHosted = fgpHome.getSurplus(hostedFlags);

        /* show new flags obtained */
        JCheckBox chbx1 = new JCheckBox(labelManager.getLabel(TXT_FP_ADD_FLAGS), true);
        JCheckBox chbx2 = new JCheckBox(labelManager.getLabel(TXT_FP_ADD_FLAGS), true);
        JCheckBox chbx3 = new JCheckBox(labelManager.getLabel(TXT_FP_REMOVE_FLAGS), false);
        JCheckBox chbx4 = new JCheckBox(labelManager.getLabel(TXT_FP_REMOVE_FLAGS), false);

        JPanel cp = new JPanel(new GridLayout(2,2,4,4));
        cp.add(createFlagPanel(newAway, labelManager.getLabel(TXT_FP_NEW_VISITED), chbx1));
        cp.add(createFlagPanel(newHosted, labelManager.getLabel(TXT_FP_NEW_HOSTED), chbx2));
        cp.add(createFlagPanel(surplusAway, labelManager.getLabel(TXT_FP_NOT_VISITED), chbx3));
        cp.add(createFlagPanel(surplusHosted, labelManager.getLabel(TXT_FP_NOT_HOSTED), chbx4));

        /* public static int showOptionDialog(Component parentComponent,
                                           Object message,
                                           String title,
                                           int optionType,
                                           int messageType,
                                           Icon icon,
                                           Object[] options,
                                           Object initialValue) */
        int opcion = JOptionPane.showOptionDialog(so.gui.MainFrame.getOwnerForDialog(), cp ,
                                                  labelManager.getLabel(TXT_FP_NEW_FLAGS),
                                                  JOptionPane.OK_CANCEL_OPTION, 
                                                  JOptionPane.PLAIN_MESSAGE, null, null, null);
        boolean updated = false;
        if (opcion == JOptionPane.OK_OPTION) {
            /* add flags */
            if (chbx1.isSelected()) updated = fgpAway.addAll(newAway) || updated;
            if (chbx2.isSelected()) updated = fgpHome.addAll(newHosted) || updated;
            /* remove flags */
            if (chbx3.isSelected()) updated = fgpAway.removeAll(surplusAway) || updated;
            if (chbx4.isSelected()) updated = fgpHome.removeAll(surplusHosted) || updated;
        }
        return updated;
    }

    protected JComponent createFlagPanel(Collection<CountryDataUnit> flags, String title, JComponent jc) {
        JPanel p = new JPanel(new GridLayout(0,FlagsPlugin.FLAGS_PER_ROW,3,6));
        for (CountryDataUnit cdu : flags) p.add(new FlagLabel(cdu));

        java.awt.GridBagLayout gbl = new java.awt.GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        JPanel pq = new JPanel(gbl);

        gc.anchor = GridBagConstraints.NORTH;
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(p, gc);
        pq.add(p);

        gc.anchor = GridBagConstraints.CENTER;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.gridheight = GridBagConstraints.RELATIVE;
        gc.fill = GridBagConstraints.VERTICAL;
        java.awt.Component glue = Box.createVerticalGlue();
        gbl.setConstraints(glue, gc);
        pq.add(glue);

        gc.weightx = 0.0;
        gc.weighty = 0.0;
        gc.gridheight = GridBagConstraints.REMAINDER;
        gc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(jc, gc);
        pq.add(jc);
        pq.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK,1), title));
        return pq;
    }

}//end class FlagsPlugin

