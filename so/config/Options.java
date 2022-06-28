package so.config;
/**
 * Options.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.data.TableColumnData;
import so.data.CountryDataUnit;
import so.util.SokkerCalendar;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.BorderFactory;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.table.TableColumn;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JCalendar;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.UIManager;

public class Options extends so.data.AbstractData implements java.io.Serializable {
    private static final long serialVersionUID = -5569852465661198918L;

    /* automatic */
    private String logonUsername;
    private String logonPassword;
    private Dimension mainWindowDimension;
    private int mainWindowState;
    private ArrayList<TableColumnData> playerColumns;
    private ArrayList<TableColumnData> juniorColumns;
    private ArrayList<TableColumnData> lineupColumns;
    private boolean dontSavePassword; // not used yet
    private boolean useProxy;
    private String proxyHostname;
    private String proxyPort;
    private boolean useProxyAuth;
    private String proxyUsername;
    private String proxyPassword;
    private Date lastAutoCheck;

    /* user selectable */
    private String language;
    private double currencyConversionRate;
    private String currencySymbol;
    private int lowSkill;
    private int highSkill;

    private int initialSeason;
    private int dayInitialSeason;
    private int monthInitialSeason;
    private int yearInitialSeason;

    private int countryId;
    private String plaf;
    private boolean autoSendToNTDB;
    private boolean autoCheckVersion;

    public Options() {
        super(FILENAME_CONFIG);
        language = DEFAULT_LANG;
        mainWindowDimension = new Dimension(720,540);
        mainWindowState = JFrame.NORMAL;
        logonUsername = "";
        logonPassword = "";
        dontSavePassword = false;
        useProxy = false;
        proxyHostname = null;
        proxyPort = null;
        useProxyAuth = false;
        proxyUsername = "";
        proxyPassword = "";
        currencyConversionRate = 1.0;
        playerColumns = null;
        juniorColumns = null;
        currencySymbol = "$";
        lowSkill = 4;
        highSkill = 9;
        retrieveCalendarSettings();
        countryId = NO_COUNTRY_SET;
        plaf = null;
        autoSendToNTDB = false;
        autoCheckVersion = true;
        lastAutoCheck = new Date();
    }

    public boolean isNewDefaultOptions() {
        return (playerColumns==null && juniorColumns==null);
    }

    public String getLanguage() { return language; }
    public void setLanguage(String l) { language = l; }

    public Dimension getWindowSize() {
        if (mainWindowDimension == null) return new Dimension(720,540);
        else return mainWindowDimension;
    }
    public void setWindowSize(Dimension d) { mainWindowDimension = d; }
    public int getWindowState() { return mainWindowState; }
    public void setWindowState(int s) { mainWindowState = s; }

    public String getLogonUsername() {
        if (logonUsername == null) return "";
        return decryptString(logonUsername);
    }
    public String getLogonPassword() {
        if (logonPassword == null) return "";
        return decryptString(logonPassword);
    }
    public boolean getUseProxy() { return useProxy; }
    public String getProxyHostname() { return proxyHostname; }
    public String getProxyPort() { return proxyPort; }
    public boolean getUseProxyAuth() { return useProxyAuth; }
    public String getProxyUsername() {
        if (proxyUsername == null) return "";
        return decryptString(proxyUsername);
    }
    public String getProxyPassword() {
        if (proxyPassword == null) return "";
        return decryptString(proxyPassword);
    }
    public void setLogonUsername(String username) { logonUsername = encryptString(username); }
    public void setLogonPassword(String password) { logonPassword = encryptString(password); }
    public void setProxyUsername(String username) { proxyUsername = encryptString(username); }
    public void setProxyPassword(String password) { proxyPassword = encryptString(password); }
    public double getCurrencyConversionRate() { return currencyConversionRate; }
    public void setCurrencyConversionRate(double ccr) { currencyConversionRate = ccr; }
    public void setPlaf(String p) { plaf = p; }
    public String getPlaf() { return plaf; }
    public boolean getAutoSendToNTDB() { return autoSendToNTDB; }
    public boolean getAutoCheckVersion() { return autoCheckVersion; }
    public Date getLastAutoCheck() { return lastAutoCheck; }
    public void setLastAutoCheck(Date lac) { lastAutoCheck = lac; }

    public ArrayList<TableColumnData> getPlayerTableColumns() {
        if (playerColumns == null) {
            playerColumns = new ArrayList<TableColumnData>();
            playerColumns.ensureCapacity(PLAYERCOLUMNS_COUNT);
            for (int i=0; i<PLAYERCOLUMNS_COUNT ; i++) {
                playerColumns.add(new TableColumnData(i,75));
            }
        }
        return playerColumns;
    }
    public void setPlayerTableColumns(ArrayList<TableColumnData> pc) {
        playerColumns = pc;
    }

    public ArrayList<TableColumnData> getJuniorTableColumns() {
        if (juniorColumns == null) {
            juniorColumns = new ArrayList<TableColumnData>();
            juniorColumns.ensureCapacity(JUNIORCOLUMNS_COUNT);
            for (int i=0; i<JUNIORCOLUMNS_COUNT ; i++) {
                juniorColumns.add(new TableColumnData(i,110));
            }
        }
        return juniorColumns;
    }
    public void setJuniorTableColumns(ArrayList<TableColumnData> jc) {
        juniorColumns = jc;
    }

    public ArrayList<TableColumnData> getLineupTableColumns() {
        if (lineupColumns == null) {
            lineupColumns = new ArrayList<TableColumnData>();
            lineupColumns.ensureCapacity(LINEUPCOLUMNS_COUNT);
            int width = 160;
            for (int i=0; i<LINEUPCOLUMNS_COUNT ; i++) {
                if (9<=i || i==5) width = 40;
                else if (0<i && i<9) width = 20;
                lineupColumns.add(new TableColumnData(i,width));
            }
        }
        return lineupColumns;
    }
    public void setLineupTableColumns(ArrayList<TableColumnData> jc) {
        lineupColumns = jc;
    }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String cs) { currencySymbol = cs; }

    public int getLowSkill() { return lowSkill; }
    public int getHighSkill() { return highSkill; }
    public void setLowSkill(int low) { lowSkill = low; }
    public void setHighSkill(int high) { highSkill = high; }

    public void retrieveCalendarSettings() {
        initialSeason = SokkerCalendar.initialSeason;
        dayInitialSeason = SokkerCalendar.dayInitialSeason;
        monthInitialSeason = SokkerCalendar.monthInitialSeason;
        yearInitialSeason = SokkerCalendar.yearInitialSeason;
    }
    public void setCalendarSettings() {
        SokkerCalendar.initialSeason = initialSeason;
        SokkerCalendar.dayInitialSeason = dayInitialSeason;
        SokkerCalendar.monthInitialSeason = monthInitialSeason;
        SokkerCalendar.yearInitialSeason = yearInitialSeason;
    }

    public int getCountry() { return countryId; }
    public void setCountry(int id) {
        if (countryId == NO_COUNTRY_SET) {
            currencySymbol = CountryUtils.getCurrencySymbolForCountry(id);
            currencyConversionRate = CountryUtils.getCurrencyConversionRateForCountry(id);
            if (currencyConversionRate == 0.0) currencyConversionRate = 1.0;
            initialSeason = CountryUtils.getInitialReferenceSeasonForCountry(id);
            Calendar _cal = CountryUtils.getReferenceSeasonStartForCountry(id);
            dayInitialSeason = _cal.get(Calendar.DAY_OF_MONTH);
            monthInitialSeason = _cal.get(Calendar.MONTH);
            yearInitialSeason = _cal.get(Calendar.YEAR);
            setCalendarSettings();
        }
        countryId = id;
    }
    /* ****************** utility methods ****************** */
    private String encryptString(String in) {
        if (in == null) return "";
        char [] letras = in.toCharArray();
        StringBuffer out = new StringBuffer(letras.length);
        for (int i=0; i<letras.length; i++) {
            char c = (char)((letras[i] + 23)%Character.MAX_VALUE);
            out.append(c);
        }
        return out.toString();
    }
    private String decryptString(String in) {
        if (in == null) return "";
        char [] letras = in.toCharArray();
        StringBuffer out = new StringBuffer(letras.length);
        for (int i=0; i<letras.length; i++) {
            char c = (char)((letras[i] - 23)%Character.MAX_VALUE);
            out.append(c);
        }
        return out.toString();
    }

    public int showLoginDialog(java.awt.Component parent, so.text.LabelManager labelManager) {
        JLabel userLabel = new JLabel( labelManager.getLabel(TXT_USERNAME) + ":  " );
        JLabel passLabel = new JLabel( labelManager.getLabel(TXT_PASSWORD) + ":  " );
        JTextField userTf = new JTextField(getLogonUsername(), 16);
        JPasswordField passPf = new JPasswordField(getLogonPassword(), 16);
        final JCheckBox chkUseProxy = new JCheckBox( labelManager.getLabel(TXT_USE_PROXY), useProxy );
        final JLabel hostLabel = new JLabel( labelManager.getLabel(TXT_PROXY_HOST) + ":  " );
        final JLabel portLabel = new JLabel( labelManager.getLabel(TXT_PROXY_PORT) + ":  " );
        final JTextField hostTf = new JTextField(proxyHostname, 16);
        final JTextField portTf = new JTextField(proxyPort, 16);
        final JCheckBox chkUseProxyAuth = new JCheckBox( labelManager.getLabel(TXT_USE_PROXY_AUTH), useProxyAuth );
        final JLabel proxyUserLabel = new JLabel( labelManager.getLabel(TXT_PROXY_USERNAME) + ":  " );
        final JLabel proxyPassLabel = new JLabel( labelManager.getLabel(TXT_PROXY_PASSWORD) + ":  " );
        final JTextField proxyUserTf = new JTextField(getProxyUsername(), 16);
        final JPasswordField proxyPassPf = new JPasswordField(getProxyPassword(), 16);

        Box loginBox = new Box(javax.swing.BoxLayout.Y_AXIS);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridwidth = GridBagConstraints.REMAINDER;
        gbc2.gridheight = 1;
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;

        JPanel jp = new JPanel(new GridBagLayout());
        jp.add(userLabel, gbc);
        jp.add(userTf, gbc2);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc2.gridheight = GridBagConstraints.REMAINDER;
        jp.add(passLabel, gbc);
        jp.add(passPf, gbc2);

        Box proxyBox = new Box(javax.swing.BoxLayout.Y_AXIS);
        proxyBox.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
        JPanel proxyPanel = new JPanel(new GridBagLayout());
        gbc.gridheight = 1;
        gbc2.gridheight = 1;
        proxyPanel.add(hostLabel, gbc);
        proxyPanel.add(hostTf, gbc2);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc2.gridheight = GridBagConstraints.REMAINDER;
        proxyPanel.add(portLabel, gbc);
        proxyPanel.add(portTf, gbc2);
        JPanel authPanel = new JPanel(new GridBagLayout());
        authPanel.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
        gbc.gridheight = 1;
        gbc2.gridheight = 1;
        authPanel.add(proxyUserLabel, gbc);
        authPanel.add(proxyUserTf, gbc2);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc2.gridheight = GridBagConstraints.REMAINDER;
        authPanel.add(proxyPassLabel, gbc);
        authPanel.add(proxyPassPf, gbc2);

        proxyBox.add(proxyPanel);
        proxyBox.add(chkUseProxyAuth);
        proxyBox.add(authPanel);
        loginBox.add(jp);
        loginBox.add(chkUseProxy);
        loginBox.add(proxyBox);

        hostLabel.setEnabled(useProxy);
        portLabel.setEnabled(useProxy);
        hostTf.setEnabled(useProxy);
        portTf.setEnabled(useProxy);
        chkUseProxyAuth.setEnabled(useProxy);
        proxyUserLabel.setEnabled(useProxyAuth);
        proxyPassLabel.setEnabled(useProxyAuth);
        proxyUserTf.setEnabled(useProxy && useProxyAuth);
        proxyPassPf.setEnabled(useProxy && useProxyAuth);
        chkUseProxy.addItemListener( new ItemListener()
            {
                public void itemStateChanged(ItemEvent ie) {
                    boolean up = chkUseProxy.isSelected();
                    boolean upa = chkUseProxyAuth.isSelected();
                    hostLabel.setEnabled(up);
                    portLabel.setEnabled(up);
                    hostTf.setEnabled(up);
                    portTf.setEnabled(up);
                    chkUseProxyAuth.setEnabled(up);
                    proxyUserLabel.setEnabled(up && upa);
                    proxyPassLabel.setEnabled(up && upa);
                    proxyUserTf.setEnabled(up && upa);
                    proxyPassPf.setEnabled(up && upa);
                }
            } );
        chkUseProxyAuth.addItemListener( new ItemListener()
            {
                public void itemStateChanged(ItemEvent ie) {
                    boolean upa = chkUseProxyAuth.isSelected();
                    proxyUserLabel.setEnabled(upa);
                    proxyPassLabel.setEnabled(upa);
                    proxyUserTf.setEnabled(upa);
                    proxyPassPf.setEnabled(upa);
                }
            } );

        int ret = JOptionPane.showConfirmDialog(parent, loginBox, labelManager.getLabel(TXT_LOGIN),
                                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret!=JOptionPane.CANCEL_OPTION && ret!=JOptionPane.CLOSED_OPTION) {
            setLogonUsername( userTf.getText() );
            setLogonPassword( new String(passPf.getPassword()) );
            useProxy = chkUseProxy.isSelected();
            proxyHostname = hostTf.getText();
            proxyPort = portTf.getText();
            useProxyAuth = chkUseProxyAuth.isSelected();
            setProxyUsername( proxyUserTf.getText() );
            setProxyPassword( new String(proxyPassPf.getPassword()) );
        }
        return ret;
    }

    public int showOptionsDialog(java.awt.Component parent, so.text.LabelManager labelManager, boolean forceOk) {
        JLabel languageLabel = new JLabel( labelManager.getLabel(TXT_LANGUAGE) + ":  ", JLabel.LEFT);
        JLabel plafLabel = new JLabel( "Pluggable Look and Feel (PLaF):  ", JLabel.LEFT);
        JLabel countryLabel = new JLabel( labelManager.getLabel(TXT_COUNTRY) + ":  ", JLabel.LEFT);
        JLabel currencyConvRateLabel = new JLabel( labelManager.getLabel(TXT_CURRENCY_CONV_RATE) + ":  ", JLabel.LEFT );
        JLabel currencySymbolLabel = new JLabel( labelManager.getLabel(TXT_CURRENCY_SYMBOL) + ":  ", JLabel.LEFT );
        JLabel lowSkillLabel = new JLabel( labelManager.getLabel(TXT_LOW_SKILL) + ":  ", JLabel.LEFT );
        lowSkillLabel.setToolTipText(labelManager.getLabel(TT+TXT_LOW_SKILL));
        JLabel highSkillLabel = new JLabel( labelManager.getLabel(TXT_HIGH_SKILL) + ":  ", JLabel.LEFT );
        highSkillLabel.setToolTipText(labelManager.getLabel(TT+TXT_HIGH_SKILL));
        JLabel seasonReferenceLabel = new JLabel( labelManager.getLabel(TXT_SEASON_REFERENCE) + ":  ", JLabel.LEFT );
        seasonReferenceLabel.setToolTipText( labelManager.getLabel(TT+TXT_SEASON_REFERENCE) );
        JLabel seasonStartLabel = new JLabel( labelManager.getLabel(TXT_SEASON_REFERENCE_START) + ":  ", JLabel.LEFT );
        seasonStartLabel.setToolTipText( labelManager.getLabel(TT+TXT_SEASON_REFERENCE_START) );
        JCheckBox chkAutoSendToNTDB = new JCheckBox( labelManager.getLabel(TXT_AUTOSEND_TO_NTDB), autoSendToNTDB );
        chkAutoSendToNTDB.setToolTipText( labelManager.getLabel(TT+TXT_AUTOSEND_TO_NTDB) );
        JCheckBox chkAutoCheckVersion = new JCheckBox(labelManager.getLabel(TXT_AUTOCHECK_NEW_VERSION), autoCheckVersion);
        chkAutoCheckVersion.setToolTipText( labelManager.getLabel(TT+TXT_AUTOCHECK_NEW_VERSION) );

        JComboBox languageCombo = new JComboBox( labelManager.getLanguages() );
        languageCombo.setSelectedItem( language );
        JComboBox countryCombo = new JComboBox( labelManager.getCountriesVector() );
        countryCombo.setRenderer(new so.gui.render.CountryListCellRenderer());
        if (countryId > 0) countryCombo.setSelectedItem(new CountryDataUnit(countryId));
        else countryCombo.setSelectedItem(null);
        final JTextField cRateField = new JTextField( Double.toString(currencyConversionRate) );
        final JTextField cSymbolField = new JTextField( currencySymbol );

        Vector<String> skillNames = labelManager.getSkillLevelNames();
        JComboBox lowSkillCombo  = new JComboBox( skillNames );
        lowSkillCombo.setSelectedItem( labelManager.getSkillLevelName(lowSkill) );
        lowSkillCombo.setToolTipText(labelManager.getLabel(TT+TXT_LOW_SKILL));
        JComboBox highSkillCombo = new JComboBox( skillNames );
        highSkillCombo.setSelectedItem( labelManager.getSkillLevelName(highSkill) );
        highSkillCombo.setToolTipText(labelManager.getLabel(TT+TXT_HIGH_SKILL));

        JComboBox plafCombo = new JComboBox( UIManager.getInstalledLookAndFeels() );
        plafCombo.setRenderer( new javax.swing.plaf.basic.BasicComboBoxRenderer()
            {
                public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index,
                                                                       boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof UIManager.LookAndFeelInfo) value = ((UIManager.LookAndFeelInfo)value).getName();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            } );
        plafCombo.setSelectedItem( getLookAndFeelInfoFromClassName(plaf) );

        Calendar seasonStartCalendar = new GregorianCalendar(yearInitialSeason, monthInitialSeason, dayInitialSeason);
        final JTextField seasonReferenceField = new JTextField( Integer.toString(initialSeason) );
        seasonReferenceField.setToolTipText( labelManager.getLabel(TT+TXT_SEASON_REFERENCE) );
        final JDateChooser seasonStartChooser = new JDateChooser(new JCalendar(null, null, true, false),
                                                                 seasonStartCalendar.getTime(), null, null);
        seasonStartChooser.setToolTipText( labelManager.getLabel(TT+TXT_SEASON_REFERENCE_START) );
        seasonReferenceField.setEnabled(false);
        seasonStartChooser.setEnabled(false);
        java.awt.event.MouseAdapter _ma = new java.awt.event.MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    if (me.getButton()==MouseEvent.BUTTON1 && me.getClickCount()==2) {
                        seasonReferenceField.setEnabled(true);
                        seasonStartChooser.setEnabled(true);
                    }
                }
            };
        seasonReferenceField.addMouseListener(_ma);
        seasonStartChooser.addMouseListener(_ma);

        countryCombo.addItemListener( new ItemListener()
            {
                public void itemStateChanged(ItemEvent ie) {
                    if (ie.getStateChange() == ItemEvent.SELECTED) {
                        CountryDataUnit cdu = (CountryDataUnit)ie.getItem();
                        short _id = (short)cdu.getId();
                        cSymbolField.setText( CountryUtils.getCurrencySymbolForCountry(_id) );
                        double _rate = CountryUtils.getCurrencyConversionRateForCountry(_id);
                        if (_rate == 0.0) _rate = 1.0;
                        cRateField.setText( Double.toString(_rate) );
                        seasonReferenceField.setText( Integer.toString(CountryUtils.getInitialReferenceSeasonForCountry(_id)) );
                        seasonStartChooser.setCalendar( CountryUtils.getReferenceSeasonStartForCountry(_id) );

                    }
                }
            } );

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel jp = new JPanel(gbl);



        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new java.awt.Insets(2,2,2,2);
        addLabelOptionPair(plafLabel, plafCombo, jp, gbc);
        addLabelOptionPair(languageLabel, languageCombo, jp, gbc);
        addLabelOptionPair(countryLabel, countryCombo, jp, gbc);
        addLabelOptionPair(currencyConvRateLabel, cRateField, jp, gbc);
        addLabelOptionPair(currencySymbolLabel, cSymbolField, jp, gbc);
        addLabelOptionPair(seasonReferenceLabel, seasonReferenceField, jp, gbc);
        addLabelOptionPair(seasonStartLabel, seasonStartChooser, jp, gbc);
        addLabelOptionPair(lowSkillLabel, lowSkillCombo, jp, gbc);
        addLabelOptionPair(highSkillLabel, highSkillCombo, jp, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(chkAutoSendToNTDB, gbc);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        jp.add(chkAutoCheckVersion, gbc);

        int dlg = JOptionPane.showConfirmDialog(parent, jp, labelManager.getLabel(TXT_OPTIONS),
                            (forceOk?JOptionPane.DEFAULT_OPTION:JOptionPane.OK_CANCEL_OPTION), JOptionPane.PLAIN_MESSAGE);
        int ret = OPT_NO_CHANGE;
        if (dlg==JOptionPane.OK_OPTION || forceOk) {
            /* set the options */
            // 1 language
            String lang = (String)languageCombo.getSelectedItem();
            if ( !language.equals(lang) ) {
                if (labelManager.changeLanguage(lang)) {
                    language = lang;
                    ret = ret | OPT_LANGUAGE;
                }
            }
            // 2 currency conversion rate
            double rate = currencyConversionRate;
            try {
                rate = Double.parseDouble( cRateField.getText() );
            } catch (NumberFormatException nfe) { }
            if (currencyConversionRate != rate) {
                currencyConversionRate = rate;
                ret = ret | OPT_CUR_CONV_RATE;
            }
            // 4 currency Symbol
            if ( !currencySymbol.equals(cSymbolField.getText()) ) {
                currencySymbol = cSymbolField.getText();
                ret = ret | OPT_CUR_SYMBOL;
            }
            // 8 low junior skill
            int low = lowSkillCombo.getSelectedIndex();
            if (lowSkill != low) {
                lowSkill = low;
                ret = ret | OPT_LOW_SKILL;
            }
            // 16 high junior skill
            int high = highSkillCombo.getSelectedIndex();
            if (highSkill != high) {
                highSkill = high;
                ret = ret | OPT_HIGH_SKILL;
            }
            // 32 season reference & start
            int season = initialSeason;
            try {
                season = Integer.parseInt( seasonReferenceField.getText() );
            } catch (NumberFormatException nfe) { }
            Calendar cal = seasonStartChooser.getCalendar();
            if (initialSeason != season || seasonStartCalendar.compareTo(cal)!=0) {
                initialSeason = season;
                dayInitialSeason = cal.get(Calendar.DAY_OF_MONTH);
                monthInitialSeason = cal.get(Calendar.MONTH);
                yearInitialSeason = cal.get(Calendar.YEAR);
                setCalendarSettings();
                ret = ret | OPT_SEASON_REF;
            }
            // 64 plaf
            String plafClass;
            try {
                plafClass = ((UIManager.LookAndFeelInfo)plafCombo.getSelectedItem()).getClassName();
            } catch (Exception e) { plafClass = UIManager.getSystemLookAndFeelClassName(); }
            if ( !plafClass.equals(plaf) ) {
                try {
                    UIManager.setLookAndFeel(plafClass);
                    plaf = plafClass;
                    ret = ret | OPT_PLAF;
                } catch (Exception e) { e.printStackTrace(); }
            }
            // no real change - country
            if (countryCombo.getSelectedIndex() != -1) {
                int country = ((CountryDataUnit)countryCombo.getSelectedItem()).getId();
                countryId = country;
            }
            //no real change - autoSendToNTDB
            autoSendToNTDB = chkAutoSendToNTDB.isSelected();
            autoCheckVersion = chkAutoCheckVersion.isSelected();
        }
        return ret;
    }

    private void addLabelOptionPair(JLabel lbl, javax.swing.JComponent comp, JPanel jp, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        jp.add(lbl, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(comp, gbc);
    }

    public static UIManager.LookAndFeelInfo getLookAndFeelInfoFromClassName(String className) {
        UIManager.LookAndFeelInfo [] infos = UIManager.getInstalledLookAndFeels();
        if (className != null) {
            for (UIManager.LookAndFeelInfo lafi : infos) {
                if (className.equals(lafi.getClassName())) return lafi;
            }
        }
        return infos[0];
    }
    /* **************************************************************** */
    /* **************************************************************** */
    public static Options load() {
        Options ret = loadObject( new Options() );
        ret.setCalendarSettings();
        return ret;
    }

    public boolean save() {
        if (dontSavePassword) logonPassword = "";
        return super.save();
    }
}
