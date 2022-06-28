package so.text;
/**
 * ReportFrame.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.config.Options;
import so.data.DataPair;
import so.data.CountryDataUnit;
import so.data.PlayerProfile;
import so.data.JuniorProfile;
import so.data.PlayerRoster;
import so.data.JuniorSchool;
import so.data.MatchRepository;
import so.data.GraphicableData;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import java.awt.Font;
import java.awt.Dimension;


public class ReportFrame extends JFrame {
    private static LabelManager LM;
    private static Options OPT;
    private static JFrame FRAME;
    private JTextPane jtpHtml;
    private JTextPane jtpBbcode;

    private static ReportFrame trainingReport = null;
    private static ReportFrame juniorReport = null;
    private static ReportFrame schoolReport = null;
    private static ReportFrame flagsReport = null;

    final private static NumberFormat intFormat = NumberFormat.getIntegerInstance();
    final private static NumberFormat floatFormat = NumberFormat.getInstance();

    private static final int PLAIN = 0;
    private static final int BOLD = 1;
    private static final int UNDERLINE = 2;
    private static final int ITALIC = 4;

    public static void initReports(Options opt, LabelManager lm, JFrame frame) {
        LM = lm;
        OPT = opt;
        FRAME = frame;
        floatFormat.setMaximumFractionDigits(2);
    }

    private ReportFrame(String title, JComponent northComponent, String htmlReport, String bbcodeReport) {
        this(title, northComponent, htmlReport, bbcodeReport, true);
    }

    private ReportFrame(String title, JComponent northComponent, String htmlReport, String bbcodeReport, boolean doPack) {
        super(title);
        jtpHtml = new JTextPane();
        jtpHtml.setContentType("text/html");
        jtpHtml.setText(htmlReport);
        jtpHtml.setEditable(false);
        jtpBbcode = new JTextPane();
        jtpBbcode.setText(bbcodeReport);
        jtpBbcode.setEditable(false);
        JPopupMenu popup = new JPopupMenu();
        Action action = new DefaultEditorKit.CopyAction();
        action.putValue(Action.NAME, LM.getLabel(TXT_COPY));
        popup.add(new JMenuItem(action));
        jtpBbcode.setComponentPopupMenu(popup);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("HTML", null, jtpHtml, null);
        tabbedPane.addTab("bbCode", null, jtpBbcode, null);
        add(new JScrollPane(tabbedPane));
        if (northComponent != null) add(northComponent, java.awt.BorderLayout.NORTH);
        setIconImage(so.So.loadSoImage());
        refresh();
        if (!doPack) setSize(new Dimension(665,500));
        setLocationRelativeTo(FRAME);
        setVisible(true);
    }

    protected void refresh() {
        refresh(true);
    }
    protected void refresh(boolean doPack) {
        jtpBbcode.selectAll();
        jtpBbcode.getTransferHandler().exportToClipboard(jtpBbcode, jtpBbcode.getToolkit().getSystemClipboard(),
                                                         javax.swing.TransferHandler.COPY);
        if (doPack) pack();
    }

    protected void setHtmlReport(String text) { jtpHtml.setText(text); }
    protected void setBbcodeReport(String text) { jtpBbcode.setText(text); }

    /* ============================================================================ */
    public static void showTrainingReport(final PlayerRoster roster) {
        if (trainingReport != null) {
            trainingReport.toFront();
            return;
        }
        if (roster == null) return;
        /* build report */
        StringBuilder report = new StringBuilder("");
        StringBuilder bbcode = new StringBuilder("");
        generateTrainingReport(roster, false, report, bbcode);
        final JCheckBox check = new JCheckBox(LM.getLabel(TXT_WITH_FORM));
        /* create frame */
        trainingReport = new ReportFrame(LM.getLabel(TXT_TRAINING_REPORT), check, report.toString(), bbcode.toString());
        trainingReport.addWindowListener( new WindowAdapter()
            {
                public void windowClosing(WindowEvent we) { trainingReport = null; }
            } );
        check.addItemListener( new ItemListener()
            {
                public void itemStateChanged(ItemEvent e) {
                    StringBuilder report = new StringBuilder("");
                    StringBuilder bbcode = new StringBuilder("");
                    generateTrainingReport(roster, check.isSelected(), report, bbcode);
                    trainingReport.setHtmlReport(report.toString());
                    trainingReport.setBbcodeReport(bbcode.toString());
                    trainingReport.refresh(false);
                }
            } );
    }

    private static void generateTrainingReport(PlayerRoster roster, boolean withForm,
                                               StringBuilder report, StringBuilder bbcode) {
        List<PlayerProfile> players = roster.getPlayersList();
        if (players==null || players.isEmpty()) return;
        report.append("<HTML><FONT FACE=\"courier\">");
        int [] plus  = new int[9];
        int [] minus = new int[9];
        report.append("<TABLE>");
        for (PlayerProfile p : players) {
            GraphicableData data = p.getGraphicableData();
            int a, b;
            StringBuilder playerSkill = new StringBuilder();
            // skip 0 (value) and 1 (experience)
            for (int skill=(withForm?0:1); skill<=8; skill++) { //% para incluir FORMA empezar de 2
                a = data.getData(0, skill+2);
                b = data.getData(1, skill+2);
                if (a==NO_DATA || b==NO_DATA || a==b) continue;
                if (playerSkill.length()>0) playerSkill.append(", ");
                playerSkill.append( getSkillDiffString(skill, a, b) );
                if (a>b) plus[skill] += (a-b);
                else minus[skill] += (a-b);
            }
            if (playerSkill.length()>0) {
                report.append("<TR><TD><B>" + p.getFullName() + "</B>:</TD><TD>");
                report.append(playerSkill);
                report.append("</TD></TR>");
                bbcode.append("[b]" + p.getFullName() + "[/b]: ");
                bbcode.append(getAsBBCode(playerSkill.toString()));
                bbcode.append("\n");
            }
        }
        report.append("</TABLE><BR><TABLE>");
        bbcode.append("\n");
        for (int i=0; i<9; i++) {
            if (plus[i]==0 && minus[i]==0) continue;
            report.append("<TR><TD><B>" + getSkillName(i) +"</B>:</TD><TD>(+" +
                          plus[i] + '/' + minus[i] + ")</TD><TD>= " + (plus[i]+minus[i]));
            report.append("</TD></TR>");
            bbcode.append("[b]" + getSkillName(i) +"[/b]: (+" + plus[i] + '/' + minus[i] + ") = " + (plus[i]+minus[i]));
            bbcode.append("\n");
        }
        report.append("</TABLE>");
        report.append("</FONT></HTML>");
    }
    /* ============================================================================ */
    public static void showJuniorReport(final JuniorSchool school) {
        if (juniorReport != null) {
            juniorReport.toFront();
            return;
        }
        if (school == null) return;
        /* build report */
        StringBuilder report = new StringBuilder("");
        StringBuilder bbcode = new StringBuilder("");
        generateJuniorReport(school, report, bbcode);
        /* create frame */
        juniorReport = new ReportFrame(LM.getLabel(TXT_JUNIOR_REPORT), null, report.toString(), bbcode.toString());
        juniorReport.addWindowListener( new WindowAdapter()
            {
                public void windowClosing(WindowEvent we) { juniorReport = null; }
            } );
    }

    private static void generateJuniorReport(JuniorSchool school, StringBuilder report, StringBuilder bbcode) {
        List<JuniorProfile> juniors = school.getJuniorsList();
        if (juniors==null || juniors.isEmpty()) return;
        report.append("<HTML><FONT FACE=\"courier\">");
        report.append("<TABLE>");
        DataPair dp;
        StringBuilder juniorPops = new StringBuilder();
        StringBuilder juniorPops2 = new StringBuilder();
        int pops = 0;
        for (JuniorProfile j : juniors) {
            try {
                dp = (DataPair)j.getData(2);
            } catch (ClassCastException cce) { continue; }
            int a = dp.getValue();
            int b = dp.getSecondValue();
            if (a>b) {
                juniorPops.append("<TR><TD>" + j.getFullName() + " (" + j.getWeeks() + "):</TD><TD>");
                juniorPops.append(LM.getSkillLevelName(b) + " \u2192 <B>" + LM.getSkillLevelName(a) + "</B>");
                juniorPops.append("</TD></TR>");
                juniorPops2.append(j.getFullName() + " ([i]" + j.getWeeks() + "[/i]): ");
                juniorPops2.append(LM.getSkillLevelName(b) + " \u2192 [b]" + LM.getSkillLevelName(a) + "[/b]");
                juniorPops2.append("\n");
                pops++;
            }
        }
        report.append("<TR><TD COLSPAN=2><U>" +LM.getLabel(TXT_CH_SKILLUP_COUNT)+ " (<B>" +pops+ "</B>)</U>:</TD></TR>");
        bbcode.append("[u]" + LM.getLabel(TXT_CH_SKILLUP_COUNT) + " ([b]" + pops + "[/b])[/u]:\n\n");
        if (pops > 0) {
            report.append( juniorPops );
            bbcode.append( juniorPops2 );
        }
        report.append("</TABLE></FONT></HTML>");
        bbcode.append("\n");
    }
    /* ============================================================================ */
    public static void showPlayerReport(final PlayerProfile player) {
        if (player == null) return;
        /* build report */
        StringBuilder report = new StringBuilder("");
        StringBuilder bbcode = new StringBuilder("");
        CountryDataUnit country = new CountryDataUnit(OPT.getCountry());
        String language = OPT.getLanguage();
        generatePlayerReport(player, country, language, report, bbcode);
        final JComboBox countryCombo = new JComboBox(LM.getCountriesVector());
        countryCombo.setRenderer(new so.gui.render.CountryListCellRenderer());
        countryCombo.setSelectedItem( country );
        final JComboBox languageCombo = new JComboBox(LM.getLanguages());
        languageCombo.setSelectedItem( language );
        JPanel panel = new JPanel();
        panel.add(countryCombo);
        panel.add(languageCombo);
        /* create frame */
        final ReportFrame playerReport;
        playerReport = new ReportFrame(LM.getLabel(TXT_PLAYER_REPORT), panel, report.toString(), bbcode.toString());

        ItemListener countryLangUpdater = new ItemListener()
            {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getItem()==null) return;
                    if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                        StringBuilder report = new StringBuilder("");
                        StringBuilder bbcode = new StringBuilder("");
                        generatePlayerReport(player, (CountryDataUnit)countryCombo.getSelectedItem(),
                                             (String)languageCombo.getSelectedItem(), report, bbcode);
                        playerReport.setHtmlReport(report.toString());
                        playerReport.setBbcodeReport(bbcode.toString());
                        playerReport.refresh();
                    }
                }
            };
        countryCombo.addItemListener( countryLangUpdater );
        languageCombo.addItemListener( countryLangUpdater );
    }

    private static void generatePlayerReport(PlayerProfile player, CountryDataUnit cdu, String language,
                                             StringBuilder report, StringBuilder bbcode) {
        int country = cdu.getId();
        double rate = so.config.CountryUtils.getCurrencyConversionRateForCountry(country);
        if (rate == 0.0) rate = 1.0; //% MENSAJE O ALGO!!!
        String symbol = so.config.CountryUtils.getCurrencySymbolForCountry(country);
        LabelManager lm = null;
        if (language.equals(OPT.getLanguage())) lm = LM;
        else lm = new LabelManager(language);

        String bbsep = ", &nbsp; ";
        report.append("<HTML><FONT FACE=\"courier\"><TABLE>");
        report.append("<TR><TD COLSPAN=2><U>" + player.getFullName() + "</U></TD>");
        report.append("<TD COLSPAN=2 align=CENTER>" + lm.getLabel(TXT_CH_AGE) + ": " + player.getAge() + "</TD></TR>");
        bbcode.append("[b][pid=" + player.getId() + ']' + player.getFullName() + "[/pid][/b], " +
                      lm.getLabel(TXT_CH_AGE) + ": " + player.getAge() + "\n");
        /* value, wage */
        String value = intFormat.format(player.getValue()*rate) + ' ' + symbol;
        String salary = intFormat.format(player.getSalary()*rate) + ' ' + symbol;
        report.append("<TR><TD><B>" + lm.getLabel(TXT_CH_VALUE) + "</B>:</TD><TD>" + value + "</TD>");
        report.append("<TD><B>" + lm.getLabel(TXT_CH_SALARY) + "</B>:</TD><TD>" + salary + "</TD></TR>");
        bbcode.append(lm.getLabel(TXT_CH_VALUE) + ": [money=1]" + player.getValue() + "[/money]" + bbsep +
                      lm.getLabel(TXT_CH_SALARY) +": [money=1]" + player.getSalary() + "[/money]\n");
        /* team , country */
        int _pc = player.getCountryFrom();
        CountryDataUnit _pcdu = new CountryDataUnit(_pc);
        java.net.URL url = so.gui.MainFrame.getResourceURL( DIRNAME_FLAGS + "/" + Integer.toString(_pc) + ".png" );
        String flag = "<IMG src='" + url.toString() + "' alt='" + _pcdu.getName() + "'>";
        int _tid = player.getOwnerTeamId();
        report.append("<TR><TD<B>" + lm.getLabel(TXT_CH_TEAM) + "</B>:</TD><TD>" + so.gui.MainFrame.getTeamName(_tid) + "</TD>");
        bbcode.append(lm.getLabel(TXT_CH_TEAM) + ": [tid=" + _tid + ']' + so.gui.MainFrame.getTeamName(_tid) + "[/tid]" + bbsep);
        report.append("<TD><B>" + lm.getLabel(TXT_COUNTRY) + "</B>:</TD><TD align=CENTER>" + flag + "</TD></TR>");
        bbcode.append(lm.getLabel(TXT_COUNTRY) + ": " + _pcdu.getName() + "\n");
        /* form */
        report.append("<TR><TD><B>" + lm.getLabel(TXT_FORM) + "</B>:</TD><TD>" + skillStr(player.getForm(), lm) + "</TD>");
        report.append("<TD>&nbsp;</TD><TD>&nbsp;</TD></TR>");
        bbcode.append(lm.getLabel(TXT_FORM) + ": " + skillStr(player.getForm(), lm) + "\n");
        /* other skills */
        String _aux = "</B>:</TD><TD>";
        report.append("<TR><TD><B>" + lm.getLabel(TXT_STAMINA) + _aux + skillStr(player.getStamina(), lm) + "</TD>");
        report.append("<TD><B>" + lm.getLabel(TXT_KEEPER) + _aux + skillStr(player.getKeeper(), lm) + "</TD></TR>");
        bbcode.append(bbSkillStr( lm.getLabel(TXT_STAMINA), player.getStamina(), lm ) + bbsep);
        bbcode.append(bbSkillStr( lm.getLabel(TXT_KEEPER), player.getKeeper(), lm ) + "\n");
        report.append("<TR><TD><B>" + lm.getLabel(TXT_PACE) + _aux + skillStr(player.getPace(), lm) + "</TD>");
        report.append("<TD><B>" + lm.getLabel(TXT_DEFENDER) + _aux + skillStr(player.getDefender(), lm) + "</TD></TR>");
        bbcode.append(bbSkillStr( lm.getLabel(TXT_PACE), player.getPace(), lm ) + bbsep);
        bbcode.append(bbSkillStr( lm.getLabel(TXT_DEFENDER), player.getDefender(), lm ) + "\n");
        report.append("<TR><TD><B>" + lm.getLabel(TXT_TECHNIQUE) + _aux + skillStr(player.getTechnique(), lm) + "</TD>");
        report.append("<TD><B>" + lm.getLabel(TXT_PLAYMAKER) + _aux + skillStr(player.getPlaymaker(), lm) + "</TD></TR>");
        bbcode.append(bbSkillStr( lm.getLabel(TXT_TECHNIQUE), player.getTechnique(), lm ) + bbsep);
        bbcode.append(bbSkillStr( lm.getLabel(TXT_PLAYMAKER), player.getPlaymaker(), lm ) + "\n");
        report.append("<TR><TD><B>" + lm.getLabel(TXT_PASSING) + _aux + skillStr(player.getPassing(), lm) + "</TD>");
        report.append("<TD><B>" + lm.getLabel(TXT_SCORER) + _aux + skillStr(player.getScorer(), lm) + "</TD></TR>");
        bbcode.append(bbSkillStr( lm.getLabel(TXT_PASSING), player.getPassing(), lm ) + bbsep);
        bbcode.append(bbSkillStr( lm.getLabel(TXT_SCORER), player.getScorer(), lm ) + "\n");

        report.append("</TABLE></FONT></HTML>");
        //bbcode.append("\n");
    }
    private static String skillStr(int lvl, LabelManager lm) {
        return lm.getSkillLevelName(lvl) + " (" + lvl + ')';
    }
    private static String bbSkillStr(String skn, int lvl, LabelManager lm) {
        String pre  = (lvl>=OPT.getHighSkill())?"[b]":"";
        String post = (lvl>=OPT.getHighSkill())?"[/b]":"";
        return pre + skn + ": " + skillStr(lvl, lm) + post;
    }

    /* ============================================================================ */
    public static void showSchoolReport(final JuniorSchool school) {
        if (schoolReport != null) {
            schoolReport.toFront();
            return;
        }
        if (school == null) return;
        /* build report */
        StringBuilder report = new StringBuilder("");
        StringBuilder bbcode = new StringBuilder("");
        generateSchoolReport(school, report, bbcode);
        /* create frame */
        schoolReport = new ReportFrame(LM.getLabel(TXT_SCHOOL_REPORT), null, report.toString(), bbcode.toString(), false);
        schoolReport.addWindowListener( new WindowAdapter()
            {
                public void windowClosing(WindowEvent we) { schoolReport = null; }
            } );
    }

    private static void generateSchoolReport(JuniorSchool school, StringBuilder report, StringBuilder bbcode) {
        List<JuniorProfile> juniors = school.getJuniorsList();
        if (juniors==null || juniors.isEmpty()) return;
        report.append("<HTML><FONT FACE=\"Verdana\">");
        report.append("<TABLE>");
        DataPair dp;
        Float _avg;

        Font auxFont = new Font("Verdana", Font.PLAIN, 9);
        javax.swing.JLabel jLabel = new javax.swing.JLabel("jGuru");
        jLabel.setFont(auxFont);
        java.awt.FontMetrics fm = jLabel.getFontMetrics(auxFont);

        int maxLenName  = fm.stringWidth( LM.getLabel(TXT_CH_NAME) );
        int maxLenWeeks = fm.stringWidth( LM.getLabel(TXT_CH_WEEKS) );
        int maxLenLevel = fm.stringWidth( LM.getLabel(TXT_CH_SKILL) );
        int maxLenPops  = fm.stringWidth( LM.getLabel(TXT_CH_SKILLUP_COUNT) );
        int maxLenAvg   = fm.stringWidth( LM.getLabel(TXT_CH_AVG_WEEKS) );
        int maxLenProjection = fm.stringWidth( LM.getLabel(TXT_CH_PROJECTED_LEVEL) );
        int aux = 0;

        for (JuniorProfile j : juniors) {
            aux = fm.stringWidth( j.getFullName() );
            if (aux > maxLenName) maxLenName = aux;
            aux = fm.stringWidth( Short.toString(j.getWeeks()) );
            if (aux > maxLenWeeks) maxLenWeeks = aux;
            aux = fm.stringWidth( LM.getSkillLevelName(j.getSkill()) );
            if (aux > maxLenLevel) maxLenLevel = aux;
            aux = fm.stringWidth( Short.toString(j.getSkillupCount()) );
            if (aux > maxLenPops) maxLenPops = aux;

            try {
                _avg = (Float)j.getData(4);
            } catch (ClassCastException cce) { continue; }
            aux = fm.stringWidth( floatFormat.format( _avg.floatValue() ) );
            if (_avg.floatValue()<0) aux++; // 1 space between symbol and number
            if (aux > maxLenAvg) maxLenAvg = aux;

            try {
                dp = (DataPair)j.getData(5);
            } catch (ClassCastException cce) { continue; }
            if (dp == null) continue;
            aux = fm.stringWidth( LM.getSkillLevelName(dp.getValue()) );
            if (dp.getDecimalValue() > 0) aux += fm.stringWidth( floatFormat.format(dp.getDecimalValue()) ) + 3;
            else aux += 2;
            if (aux > maxLenProjection) maxLenProjection = aux;
        }

        final String begTags1 = "<TD><B><U>";
        final String endTags1 = "</U></B></TD>";
        report.append("<TR>");
        report.append(begTags1); report.append(LM.getLabel(TXT_CH_NAME)); report.append(endTags1);
        report.append(begTags1); report.append(LM.getLabel(TXT_CH_WEEKS)); report.append(endTags1);
        report.append(begTags1); report.append(LM.getLabel(TXT_CH_SKILL)); report.append(endTags1);
        report.append(begTags1); report.append(LM.getLabel(TXT_CH_SKILLUP_COUNT)); report.append(endTags1);
        report.append(begTags1); report.append(LM.getLabel(TXT_CH_AVG_WEEKS)); report.append(endTags1);
        report.append(begTags1); report.append(LM.getLabel(TXT_CH_PROJECTED_LEVEL)); report.append(endTags1);
        report.append("</TR>");
        final char div = ' ';
        final String begTags2 = "[size=9px]";
        final String endTags2 = "[/size]\n";
        bbcode.append(begTags2);
        //bbcode.append("[b][u]");
        bbcode.append(nbspPad(fm, LM.getLabel(TXT_CH_NAME), maxLenName, LEFT_ALIGNMENT, PLAIN));
        bbcode.append(div);
        bbcode.append(nbspPad(fm, LM.getLabel(TXT_CH_WEEKS), maxLenWeeks, CENTER_ALIGNMENT, PLAIN));
        bbcode.append(div);
        bbcode.append(nbspPad(fm, LM.getLabel(TXT_CH_SKILL), maxLenLevel, CENTER_ALIGNMENT, PLAIN));
        bbcode.append(div);
        bbcode.append(nbspPad(fm, LM.getLabel(TXT_CH_SKILLUP_COUNT), maxLenPops, CENTER_ALIGNMENT, PLAIN));
        bbcode.append(div);
        bbcode.append(nbspPad(fm, LM.getLabel(TXT_CH_AVG_WEEKS), maxLenAvg, CENTER_ALIGNMENT, PLAIN));
        bbcode.append(div);
        bbcode.append(nbspPad(fm, LM.getLabel(TXT_CH_PROJECTED_LEVEL), maxLenProjection, RIGHT_ALIGNMENT, PLAIN));
        //bbcode.append("[/u][/b]");
        bbcode.append(endTags2);
        bbcode.append(begTags2);
        aux = maxLenName + maxLenWeeks + maxLenLevel + maxLenPops + maxLenAvg + maxLenProjection + 5;
        int dashLen = fm.stringWidth("-");
        for (int i=0; i<aux; i+=dashLen) bbcode.append('-');
        bbcode.append(endTags2);
        String _str;
        for (JuniorProfile j : juniors) {
            report.append("<TR>");
            bbcode.append(begTags2);
            _str = j.getFullName();
            report.append("<TD>"); report.append(_str); report.append("</TD>");
            bbcode.append(nbspPad(fm, _str, maxLenName, LEFT_ALIGNMENT, PLAIN));
            bbcode.append(div);
            _str = Short.toString(j.getWeeks());
            report.append("<TD align=CENTER>"); report.append(_str); report.append("</TD>");
            bbcode.append(nbspPad(fm, _str, maxLenWeeks, CENTER_ALIGNMENT, PLAIN));
            bbcode.append(div);
            _str = LM.getSkillLevelName(j.getSkill());
            report.append("<TD align=CENTER>"); report.append(_str); report.append("</TD>");
            bbcode.append(nbspPad(fm, _str, maxLenLevel, CENTER_ALIGNMENT, PLAIN));
            bbcode.append(div);
            _str = Short.toString(j.getSkillupCount());
            report.append("<TD align=CENTER>"); report.append(_str); report.append("</TD>");
            bbcode.append(nbspPad(fm, _str, maxLenPops, CENTER_ALIGNMENT, PLAIN));
            bbcode.append(div);
            try {
                _avg = (Float)j.getData(4);
            } catch (ClassCastException cce) { continue; }
            if (_avg.floatValue()>=0) _str = floatFormat.format( _avg.floatValue() );
            else _str = "\u2265 " + floatFormat.format( -_avg.floatValue() );
            report.append("<TD align=CENTER>"); report.append(_str); report.append("</TD>");
            bbcode.append(nbspPad(fm, _str, maxLenAvg, CENTER_ALIGNMENT, PLAIN));
            bbcode.append(div);

            try {
                dp = (DataPair)j.getData(5);
            } catch (ClassCastException cce) { continue; }
            if (dp == null) continue;
            _str = LM.getSkillLevelName(dp.getValue());
            if (dp.getDecimalValue() > 0) _str += " (" + floatFormat.format(dp.getDecimalValue()) + ")";
            else if (dp.getDecimalValue() == JR_GUESSED_SKILL) _str = "\u2264 " + _str; // less or equal
            else if (dp.getDecimalValue() == JR_ESTIMATED_SKILL) _str = "\u2248 " + _str; // similar
            report.append("<TD align=CENTER>"); report.append(_str); report.append("</TD>");
            bbcode.append(nbspPad(fm, _str, maxLenProjection, RIGHT_ALIGNMENT, PLAIN));

            report.append("</TR>");
            bbcode.append(endTags2);
        }
//       report.append("<TR><TD COLSPAN=2><U>" +LM.getLabel(TXT_CH_SKILLUP_COUNT)+ " (<B>" +pops+ "</B>)</U>:</TD></TR>");
//       bbcode.append("[u]" + LM.getLabel(TXT_CH_SKILLUP_COUNT) + " ([b]" + pops + "[/b])[/u]:\n\n");
        report.append("</TABLE></FONT></HTML>");
        bbcode.append("\n");
    }

    /* ============================================================================ */

    public static void showFlagsReport(final MatchRepository matchRepo) {
        if (flagsReport != null) {
            flagsReport.toFront();
            return;
        }
        if (matchRepo == null) return;
        /* build report */
        StringBuilder report = new StringBuilder("");
        StringBuilder bbcode = new StringBuilder("");
        generateFlagsReport(matchRepo, true, false, true, true, report, bbcode);
        final JCheckBox chkMissing = new JCheckBox("\u00b7missing flags", true);
        final JCheckBox chkIncludeNames = new JCheckBox("\u00b7include names", false);
        final JCheckBox chkVisited = new JCheckBox(LM.getLabel(TXT_FP_VISITED), true);
        final JCheckBox chkHosted = new JCheckBox(LM.getLabel(TXT_FP_HOSTED), true);
        JPanel panel = new JPanel();
        panel.add(chkMissing);
        panel.add(chkIncludeNames);
        panel.add(chkVisited);
        panel.add(chkHosted);

        /* create frame */
        flagsReport = new ReportFrame(LM.getLabel(TXT_FLAGS_REPORT), panel, report.toString(), bbcode.toString(), false);
        flagsReport.addWindowListener( new WindowAdapter()
            {
                public void windowClosing(WindowEvent we) { flagsReport = null; }
            } );

        ItemListener flagsReportUpdater = new ItemListener()
            {
                public void itemStateChanged(ItemEvent e) {
                    StringBuilder report = new StringBuilder("");
                    StringBuilder bbcode = new StringBuilder("");
                    generateFlagsReport(matchRepo, chkMissing.isSelected(), chkIncludeNames.isSelected(), chkVisited.isSelected(), chkHosted.isSelected(), report, bbcode);
                    flagsReport.setHtmlReport(report.toString());
                    flagsReport.setBbcodeReport(bbcode.toString());
                    flagsReport.refresh(false);
                }
            };
        chkMissing.addItemListener( flagsReportUpdater );
        chkIncludeNames.addItemListener( flagsReportUpdater );
        chkVisited.addItemListener( flagsReportUpdater );
        chkHosted.addItemListener( flagsReportUpdater );
    }

    private static void generateFlagsReport(MatchRepository matchRepo, boolean missing, boolean includeNames, boolean visited, boolean hosted,
                                            StringBuilder report, StringBuilder bbcode) {
        TreeSet<CountryDataUnit> visitedFlags = matchRepo.getVisitedFlags();
        TreeSet<CountryDataUnit> hostedFlags = matchRepo.getHostedFlags();
        java.util.Vector<CountryDataUnit> paises = LM.getCountriesVector();
        String title;

        report.append("<HTML><FONT FACE=\"Verdana\" SIZE=\"3\">");

        if (missing) {
            TreeSet<CountryDataUnit> _auxFlags = matchRepo.getVisitedFlags();
            if (visited) {
                visitedFlags = new TreeSet<CountryDataUnit>();
                for (CountryDataUnit cdu : paises) {
                    if (!_auxFlags.contains(cdu)) visitedFlags.add(cdu);
                }
            }
            _auxFlags = matchRepo.getHostedFlags();
            if (hosted) {
                hostedFlags = new TreeSet<CountryDataUnit>();
                for (CountryDataUnit cdu : paises) {
                    if (!_auxFlags.contains(cdu)) hostedFlags.add(cdu);
                }
            }
        }
        // ................................................................
        if (visited) {
            if (missing) title = "Countries not yet visited";
            else title ="Countries already visited";
            _subGenFlagColRep(report, bbcode, title, visitedFlags, includeNames);
        }

        report.append("<BR>");
        bbcode.append("\n");

        if (hosted) {
            if (missing) title = "Have not yet hosted teams from";
            else title ="Already hosted teams from";
            _subGenFlagColRep(report, bbcode, title, hostedFlags, includeNames);
        }

        report.append("</FONT></HTML>");
        bbcode.append("\n");
    }

    private static void _subGenFlagColRep(StringBuilder report, StringBuilder bbcode, String title, TreeSet<CountryDataUnit> flags, boolean includeNames) {
        report.append(title + ":<BR>");
        bbcode.append(title + ":\n");
        int i = 0;
        int lastidx = flags.size() - 1;
        for (CountryDataUnit cdu : flags) {
            report.append("<IMG SRC=\'" + so.gui.MainFrame.getResourceURL(DIRNAME_FLAGS + '/' + Integer.toString(cdu.getId()) + ".png") + "\'>&nbsp;");
            bbcode.append("[img]http://files.sokker.org/pic/flags/" + Integer.toString(cdu.getId()) + ".png[/img] ");
            if (includeNames) {
                report.append(cdu.getName());
                bbcode.append(cdu.getName());
                if (i < lastidx) {
                    report.append(", &nbsp;");
                    bbcode.append(", &nbsp;");
                }
                if (i%6 == 5) {
                    report.append("<BR>");
                    bbcode.append("\n");
                }
            }
            else {
                if (i%28 == 27) report.append("<BR>");
            }
            i++;
        }
        report.append("<BR>");
        bbcode.append("\n");
    }

    /* ============================================================================ */
    /* ============================================================================ */
    private static String getSkillDiffString(int skill, int currentValue, int previousValue) {
        String value = ((currentValue - previousValue>0)?'+':"") + Integer.toString(currentValue - previousValue);
        String color = (currentValue - previousValue>0)?"GREEN":"RED";
        return "<FONT color=" + color + '>' + value + ' ' + getSkillName(skill) + "</FONT>";
    }
    private static String getAsBBCode(String htmlCode) {
        htmlCode = htmlCode.replaceAll("<FONT color=GREEN>", "[color=green]");
        htmlCode = htmlCode.replaceAll("<FONT color=RED>", "[color=red]");
        htmlCode = htmlCode.replaceAll("</FONT>", "[/color]");
        return htmlCode;
    }
    private static String getSkillName(int idx) {
        switch (idx) {
        case 0: return LM.getLabel(TXT_FORM);
        case 1: return LM.getLabel(TXT_STAMINA);
        case 2: return LM.getLabel(TXT_PACE);
        case 3: return LM.getLabel(TXT_TECHNIQUE);
        case 4: return LM.getLabel(TXT_PASSING);
        case 5: return LM.getLabel(TXT_KEEPER);
        case 6: return LM.getLabel(TXT_DEFENDER);
        case 7: return LM.getLabel(TXT_PLAYMAKER);
        case 8: return LM.getLabel(TXT_SCORER);
        default:
            return "";
        }
    }
    private static String nbspPad(java.awt.FontMetrics fm, String txt, int width, float alignment, int style) {
        int txtLen = fm.stringWidth(txt);
        int spaceLen = fm.stringWidth(" ");
        if ((style & UNDERLINE) == UNDERLINE) txt = "[u]" + txt + "[/u]";
        if ((style & ITALIC) == ITALIC) txt = "[i]" + txt + "[/i]";
        if ((style & BOLD) == BOLD) txt = "[b]" + txt + "[/b]";
        if (txtLen >= width) return txt;
        int lpad = 0;
        int rpad = 0;
        if (alignment == LEFT_ALIGNMENT) rpad = width - txtLen;
        else if (alignment == RIGHT_ALIGNMENT) lpad = width - txtLen;
        else {
            lpad = (int)Math.ceil((width - txtLen) / 2.0);
            rpad = (int)Math.floor((width - txtLen) / 2.0);
        }
        final String nbsp = "&nbsp;";
        StringBuilder str = new StringBuilder("");
        for (int i=0; i<lpad; i+=spaceLen) str.append(nbsp);
        str.append(txt);
        for (int i=0; i<rpad; i+=spaceLen) str.append(nbsp);
        return str.toString();
    }
}
