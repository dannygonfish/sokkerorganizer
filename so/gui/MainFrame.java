package so.gui;

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.text.*;
import so.config.*;
import so.data.*;
import so.util.*;
import so.gui.flagsplugin.FlagsPlugin;
import java.net.URL;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public final class MainFrame extends JFrame implements WindowListener, ActionListener {
    private static final String CMD_DOWNLOADALL      = "downloadall";
    private static final String CMD_DOWNLOADBASIC    = "downloadbasic";
    private static final String CMD_DL_LEAGUE_AND_MATCHES = "dload l&m";
    private static final String CMD_DL_MATCHLIST     = "dload mlist";
    private static final String CMD_EXIT             = "exit";
    private static final String CMD_IMPORTXML        = "importxml";
    private static final String CMD_IMPORT_SOKKERMON = "sokkermon";
    private static final String CMD_IMPORT_APOLLO    = "apollo";
    private static final String CMD_IMPORT_APOLLO2   = "apollo2";
    private static final String CMD_IMPORT_SV        = "skviewer";
    private static final String CMD_OPTIONS          = "options";
    private static final String CMD_FORMULAS         = "formulas";
    private static final String CMD_CREDITS          = "credits";
    private static final String CMD_TEST             = "test";
    private static final String CMD_TRAIN_REP        = "trainingrep";
    private static final String CMD_JUNIOR_REP       = "juniorrep";
    private static final String CMD_SCHOOL_REP       = "schoolrep";
    private static final String CMD_FLAGS_REP        = "flagsrep";
    private static final String CMD_CURRENCY_CONVERT = "c_convert";
    private static final String CMD_LOGOMAKER        = "logomaker";
    private static final String CMD_NOTES            = "notes";
    private static final String CMD_SPECIAL          = "special";
    private static final String CMD_SEND_TO_NTDB     = "sendtoNTDB";
    private static final String CMD_CHECKVERSION     = "checkver";

    private static MainFrame SO = null;
    private static HashMap<String, ImageIcon> IMAGE_ICON_CACHE = new HashMap<String, ImageIcon>(128);

    private LoadingFrame loadingFrame;
    private Options options;
    private XMLHandler xmlHandler;
    private HTMLHandler htmlHandler;
    private LabelManager labelManager;
    private PositionManager positionManager;
    private boolean downloadInProcess;

    private TeamDetails team;
    private PlayerRoster roster;
    private JuniorSchool school;
    private Stadium stadium;
    private CoachOffice office;
    private LeagueEncapsulator leaguesData;
    private MatchRepository matchRepo;
    private LineupManager lineupManager;

    private JMenuBar menuBar;
    private JTabbedPane tabbedPane;
    private SquadPanel squadPanel;
    private JuniorSchoolPanel schoolPanel;
    private PlayerStatsPanel playerStatsPanel;
    private StadiumPanel stadiumPanel;
    private LeagueTablePanel leaguePanel;
    private FlagsPlugin flagsPlugin;
    private TrainingPanel trainingPanel;
    private MatchesPanel matchesPanel;
    private LineupPanel lineupPanel;

    public MainFrame(String title) {
        this(title, null);
    }
    public MainFrame(String title, LoadingFrame loadingFrame) {
        super(title);
        if (SO == null) SO = this;
        else {
            dispose();
            return;
        }
        setIconImage(so.So.loadSoImage());

        this.loadingFrame = loadingFrame;
        final javax.swing.Action abortSO = new javax.swing.AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(1);
                }
            };
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK), "abortSO");
        getRootPane().getActionMap().put("abortSO", abortSO);
        loadingFrame.displayLoadingMessage("Initializing");
        loadingFrame.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK), "abortSO");
        loadingFrame.getRootPane().getActionMap().put("abortSO", abortSO);
        options = Options.load();
        positionManager = PositionManager.load();
        labelManager = new LabelManager( options.getLanguage() );
        if ( options.isNewDefaultOptions() ) options.showOptionsDialog(loadingFrame, labelManager, true);

        setLookAndFeel(options.getPlaf());
        ReportFrame.initReports(options, labelManager, this);
        downloadInProcess = false;

        loadingFrame.displayLoadingMessage("Loading Data");
        loadData();
        xmlHandler = new XMLHandler(options, this, labelManager, team, roster, school, stadium, office,
                                    leaguesData, matchRepo);
        htmlHandler = new HTMLHandler(options, this, labelManager, roster, school);

        //setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(this);
        setSize(options.getWindowSize());
        setExtendedState(options.getWindowState());
        setLocationRelativeTo(null);

        loadingFrame.displayLoadingMessage("Creating GUI");
        createGUI();
        try {
            if (options.getAutoCheckVersion()) {
                Date now = new Date();
                Date lastCheck = options.getLastAutoCheck();
                if ( lastCheck==null || ((lastCheck.getTime() + 518400000L) < now.getTime()) ) {
                    checkSOVersion(true);
                    options.setLastAutoCheck(now);
                }
            }
        } catch (Exception ex) { }
        this.loadingFrame = null;
    }

    private void createGUI() {
        createMenuBar();
        createMainFrame();
    }

    /* **************************************************************************** */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F10"), "none");
        final JMenu mnuFile = new JMenu(labelManager.getLabel( TXT_DATA ));
        mnuFile.setMnemonic(KeyEvent.VK_D);
        final JMenu mnuConfig = new JMenu(labelManager.getLabel( TXT_CONFIG ));
        mnuConfig.setMnemonic(KeyEvent.VK_C);
        final JMenu mnuInfo = new JMenu(labelManager.getLabel( TXT_INFO ));
        mnuInfo.setMnemonic(KeyEvent.VK_N);
        final JMenu mnuReports = new JMenu(labelManager.getLabel( TXT_REPORTS ));
        mnuReports.setMnemonic(KeyEvent.VK_R);
        final JMenu mnuUtils = new JMenu(labelManager.getLabel( TXT_UTILS ));
        mnuUtils.setMnemonic(KeyEvent.VK_U);

        /* Data menu */
        final JMenuItem miDownloadAll = new JMenuItem(labelManager.getLabel( TXT_DOWNLOAD_ALL ));
        miDownloadAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        miDownloadAll.setActionCommand(CMD_DOWNLOADALL);
        miDownloadAll.addActionListener(this);
        final JMenuItem miDownloadBasic = new JMenuItem(labelManager.getLabel( TXT_DOWNLOAD_BASIC ));
        miDownloadBasic.setToolTipText(labelManager.getLabel(TT + TXT_DOWNLOAD_BASIC));
        miDownloadBasic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        miDownloadBasic.setActionCommand(CMD_DOWNLOADBASIC);
        miDownloadBasic.addActionListener(this);
        final JMenuItem miDownloadLeagueAndMatches = new JMenuItem(labelManager.getLabel( TXT_DOWNLOAD_L_M ));
        miDownloadLeagueAndMatches.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        miDownloadLeagueAndMatches.setActionCommand(CMD_DL_LEAGUE_AND_MATCHES);
        miDownloadLeagueAndMatches.addActionListener(this);
        final JMenuItem miDownloadMatchList = new JMenuItem(labelManager.getLabel( TXT_DOWNLOAD_HISTM ));
        miDownloadMatchList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        miDownloadMatchList.setActionCommand(CMD_DL_MATCHLIST);
        miDownloadMatchList.addActionListener(this);
        final JMenuItem miImportXML = new JMenuItem(labelManager.getLabel( TXT_IMPORT_XML ));
        miImportXML.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        miImportXML.setMnemonic(KeyEvent.VK_X);
        miImportXML.setActionCommand(CMD_IMPORTXML);
        miImportXML.addActionListener(this);
        final JMenuItem miImportSokkermon = new JMenuItem("Sokkermon 5");
        miImportSokkermon.setActionCommand(CMD_IMPORT_SOKKERMON);
        miImportSokkermon.addActionListener(this);
        final JMenuItem miImportApollo = new JMenuItem("Apollo 1.0");
        miImportApollo.setActionCommand(CMD_IMPORT_APOLLO);
        miImportApollo.addActionListener(this);
        final JMenuItem miImportApollo2 = new JMenuItem("Apollo 2.0");
        miImportApollo2.setActionCommand(CMD_IMPORT_APOLLO2);
        miImportApollo2.addActionListener(this);
        final JMenuItem miImportSV = new JMenuItem("Sokker Viewer");
        miImportSV.setActionCommand(CMD_IMPORT_SV);
        miImportSV.addActionListener(this);
        final JMenu submnuImportFromExternalProgram = new JMenu(labelManager.getLabel( TXT_IMPORT_EXTERNAL ));
        submnuImportFromExternalProgram.setMnemonic(KeyEvent.VK_E);
        submnuImportFromExternalProgram.add(miImportSokkermon);
        submnuImportFromExternalProgram.add(miImportApollo);
        submnuImportFromExternalProgram.add(miImportApollo2);
        submnuImportFromExternalProgram.add(miImportSV);
        final JMenu submnuImport = new JMenu(labelManager.getLabel( TXT_IMPORT ));
        submnuImport.setMnemonic(KeyEvent.VK_I);
        submnuImport.add(miImportXML);
        submnuImport.add(submnuImportFromExternalProgram);
        final JMenuItem miSendToNTDB = new JMenuItem(labelManager.getLabel( TXT_SEND_TO_NTDB ));
        miSendToNTDB.setActionCommand(CMD_SEND_TO_NTDB);
        miSendToNTDB.addActionListener(this);
        final JMenuItem miExit = new JMenuItem(labelManager.getLabel( TXT_EXIT ));
        miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        miExit.setActionCommand(CMD_EXIT);
        miExit.addActionListener(this);

        /* Config Menu */
        final JMenuItem miOptions = new JMenuItem(labelManager.getLabel( TXT_OPTIONS ));
        miOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        miOptions.setMnemonic(KeyEvent.VK_O);
        miOptions.setActionCommand(CMD_OPTIONS);
        miOptions.addActionListener(this);
        final JMenuItem miFormulas = new JMenuItem(labelManager.getLabel( TXT_FORMULAS ));
        miFormulas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        miFormulas.setMnemonic(KeyEvent.VK_F);
        miFormulas.setActionCommand(CMD_FORMULAS);
        miFormulas.addActionListener(this);

        /* Info Menu */
        final JMenuItem miCredits = new JMenuItem(labelManager.getLabel( TXT_CREDITS ));
        miCredits.setMnemonic(KeyEvent.VK_E);
        miCredits.setActionCommand(CMD_CREDITS);
        miCredits.addActionListener(this);
        final JMenuItem miCheckVersion = new JMenuItem(labelManager.getLabel(TXT_CHECKNEWVERSION));
        miCheckVersion.setActionCommand(CMD_CHECKVERSION);
        miCheckVersion.addActionListener(this);
        final JMenuItem miTest = new JMenuItem("Test");
        miTest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        miTest.setActionCommand(CMD_TEST);
        miTest.addActionListener(this);

        /* Reports Menu */
        final JMenuItem miTrainingReport = new JMenuItem(labelManager.getLabel( TXT_TRAINING_REPORT ));
        miTrainingReport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK));
        miTrainingReport.setMnemonic(KeyEvent.VK_T);
        miTrainingReport.setActionCommand(CMD_TRAIN_REP);
        miTrainingReport.addActionListener(this);
        final JMenuItem miJuniorReport = new JMenuItem(labelManager.getLabel( TXT_JUNIOR_REPORT ));
        miJuniorReport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK));
        miJuniorReport.setMnemonic(KeyEvent.VK_J);
        miJuniorReport.setActionCommand(CMD_JUNIOR_REP);
        miJuniorReport.addActionListener(this);
        final JMenuItem miSchoolReport = new JMenuItem(labelManager.getLabel( TXT_SCHOOL_REPORT ));
        miSchoolReport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
        miSchoolReport.setMnemonic(KeyEvent.VK_R);
        miSchoolReport.setActionCommand(CMD_SCHOOL_REP);
        miSchoolReport.addActionListener(this);
        final JMenuItem miFlagsReport = new JMenuItem(labelManager.getLabel( TXT_FLAGS_REPORT ));
        miFlagsReport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
        miFlagsReport.setMnemonic(KeyEvent.VK_C);
        miFlagsReport.setActionCommand(CMD_FLAGS_REP);
        miFlagsReport.addActionListener(this);

        /* Utilities Menu */
        final JMenuItem miCurrencyConverter = new JMenuItem(labelManager.getLabel( TXT_CCONVERT ));
        miCurrencyConverter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        miCurrencyConverter.setMnemonic(KeyEvent.VK_C);
        miCurrencyConverter.setActionCommand(CMD_CURRENCY_CONVERT);
        miCurrencyConverter.addActionListener(this);
        final JMenuItem miLogoMaker = new JMenuItem("Logo Maker\u2122");
        miLogoMaker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        miLogoMaker.setMnemonic(KeyEvent.VK_L);
        miLogoMaker.setActionCommand(CMD_LOGOMAKER);
        miLogoMaker.addActionListener(this);
        final JMenuItem miTeamNotes = new JMenuItem(labelManager.getLabel( TXT_MANAGER_NOTES ));
        miTeamNotes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        miTeamNotes.setMnemonic(KeyEvent.VK_N);
        miTeamNotes.setActionCommand(CMD_NOTES);
        miTeamNotes.addActionListener(this);

        /* BUILD MENUS */
        mnuFile.add(miDownloadAll);
        mnuFile.addSeparator();
        mnuFile.add(miDownloadBasic);
        mnuFile.add(miDownloadLeagueAndMatches);
        mnuFile.add(miDownloadMatchList);
        mnuFile.add(submnuImport);
        mnuFile.addSeparator();
        mnuFile.add(miSendToNTDB);
        mnuFile.addSeparator();
        mnuFile.add(miExit);
        mnuConfig.add(miOptions);
        mnuConfig.add(miFormulas);
        mnuReports.add(miTrainingReport);
        mnuReports.add(miJuniorReport);
        mnuReports.add(miSchoolReport);
        mnuReports.add(miFlagsReport);
        mnuInfo.add(miCredits);
        mnuInfo.add(miCheckVersion);
        mnuUtils.add(miCurrencyConverter);
        mnuUtils.add(miLogoMaker);
        mnuUtils.add(miTeamNotes);
        if (so.So.DEBUG) mnuInfo.add(miTest);

        menuBar.add(mnuFile);
        menuBar.add(mnuConfig);
        menuBar.add(mnuReports);
        menuBar.add(mnuUtils);
        menuBar.add(javax.swing.Box.createHorizontalGlue());
        menuBar.add(mnuInfo);
        setJMenuBar(menuBar);

        if (team.getId()==22733) {
            java.util.Date _d = new java.util.Date();
            java.util.GregorianCalendar _greg = new java.util.GregorianCalendar(2007, 3, 11); // 11 de abril
            if (_d.after(_greg.getTime())) {
                miTrainingReport.setEnabled(false);
                miJuniorReport.setEnabled(false);
                miCurrencyConverter.setEnabled(false);
                miLogoMaker.setEnabled(false);
                miImportXML.setEnabled(false);
            }
        }
    }

    private void createMainFrame() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        /* tabbedPane.addTab( label, icon, component, tooltip ); */
        String title = labelManager.getLabel(TXT_SQUAD);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        squadPanel = new SquadPanel(labelManager, options, team, roster, matchRepo);
        tabbedPane.addTab(title, null, squadPanel, null);

        title = labelManager.getLabel(TXT_PLAYER_STATS);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        playerStatsPanel = new PlayerStatsPanel(labelManager, options, roster, matchRepo);
        tabbedPane.addTab(title, null, playerStatsPanel, null);

        title = labelManager.getLabel(TXT_JUNIOR_SCHOOL);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        schoolPanel = new JuniorSchoolPanel(labelManager, options, school);
        tabbedPane.addTab(title, null, schoolPanel, null);

        title = labelManager.getLabel(TXT_LEAGUETABLE);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        leaguePanel = new LeagueTablePanel(labelManager, options, leaguesData);
        tabbedPane.addTab(title, null, leaguePanel, null);

        title = labelManager.getLabel(TXT_TRAINING);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        trainingPanel = new TrainingPanel(labelManager, options, office, roster, matchRepo);
        tabbedPane.addTab(title, null, trainingPanel, null);

        title = labelManager.getLabel(TXT_LINEUP);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        lineupPanel = new LineupPanel(labelManager, options, team, roster, lineupManager, matchRepo);
        tabbedPane.addTab( labelManager.getLabel(TXT_LINEUP), null, lineupPanel, null );

        title = labelManager.getLabel(TXT_MATCHES);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        matchesPanel = new MatchesPanel(labelManager, options, team, matchRepo);
        tabbedPane.addTab(title, null, matchesPanel, null);

        //stadiumPanel = new StadiumPanel(labelManager, options, stadium);
        //tabbedPane.addTab( labelManager.getLabel(TXT_STADIUM), null, stadiumPanel, null );

        title = labelManager.getLabel(TXT_FLAGCOLLECTION);
        if (loadingFrame != null) loadingFrame.displayLoadingMessage(title);
        flagsPlugin = new FlagsPlugin(labelManager, options, team, roster, matchRepo);
        tabbedPane.addTab(title, null, flagsPlugin, null);

        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);
        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
        tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);
        tabbedPane.setMnemonicAt(7, KeyEvent.VK_8);
        add(tabbedPane);
        squadPanel.setPlayerStatsPanel(playerStatsPanel);
        if (team.getId()==22733) {
            tabbedPane.setEnabledAt(1, false);
            tabbedPane.setEnabledAt(3, false);
            tabbedPane.setEnabledAt(4, false);
            tabbedPane.setEnabledAt(5, false);
            tabbedPane.setEnabledAt(6, false);
            tabbedPane.setEnabledAt(7, false);
        }
    }

    /* **************************************************************** */
    private void loadData() {
        DataEncapsulator data = DataEncapsulator.load();
        team = data.getTeamDetails();
        roster = data.getPlayerRoster();
        school = data.getJuniorSchool();
        stadium = data.getStadium();
        office = data.getCoachOffice();
        lineupManager = data.getLineupManager();
        leaguesData = LeagueEncapsulator.load();
        matchRepo = MatchRepository.load();
    }
    /* **************************************************************************** */
    private void saveData() {
        DataEncapsulator data = new DataEncapsulator();
        data.setTeamDetails(team);
        data.setPlayerRoster(roster);
        data.setJuniorSchool(school);
        data.setStadium(stadium);
        data.setCoachOffice(office);
        data.setLineupManager(lineupManager);
        data.save();
        leaguesData.save();
        matchRepo.save();
    }
    private void saveAll() {
        squadPanel.storeColumnSettings();
        schoolPanel.storeColumnSettings();
        lineupPanel.storeColumnSettings();
        saveData();
        options.save();
        positionManager.save();
    }
    private void exitProgram() {
        options.setWindowSize( getSize() );
        options.setWindowState( getExtendedState() & JFrame.MAXIMIZED_BOTH );
        saveAll();
        SO = null;
        this.dispose();
        System.out.close();
        System.err.close();
    }
    /* **************************************************************************** */
    private void refreshPanels(int parsedElements) {
        if ((parsedElements & PARSE_TEAM) == PARSE_TEAM) {

        }
        if ((parsedElements & PARSE_PLAYERS) == PARSE_PLAYERS) {
            squadPanel.refreshData();
            playerStatsPanel.refreshData();
            flagsPlugin.refresh();
            lineupPanel.refreshData();
            trainingPanel.refreshData();
        }
        if ((parsedElements & PARSE_JUNIORS) == PARSE_JUNIORS) {
            schoolPanel.refreshData();
        }
        if ((parsedElements & PARSE_TRAINING) == PARSE_TRAINING) {
            trainingPanel.refreshPanels();
        }
        if ((parsedElements & PARSE_STADIUM) == PARSE_STADIUM) {
            //
        }
        if ((parsedElements & PARSE_ECONOMY) == PARSE_ECONOMY) {
            //refresh Economy panel
        }
        if ((parsedElements & PARSE_LEAGUE) == PARSE_LEAGUE) {
            //refresh League panel
            leaguePanel.refreshData();
        }
        if ((parsedElements & PARSE_MATCH) == PARSE_MATCH) {
            playerStatsPanel.refreshRatingsTable();
            matchesPanel.refreshData();
            lineupPanel.refreshData();
            //flagsPlugin. ??? ();
        }
    }

    private void enableDownload(boolean enable) {
        downloadInProcess = !enable;
        java.awt.Component c = menuBar.getComponent(0);
        if (c instanceof JMenu) ((JMenu)c).setEnabled(enable);
        c = menuBar.getComponent(1);
        if (c instanceof JMenu) ((JMenu)c).setEnabled(enable);
    }

    /* ################################## */
    public static ImageIcon getImageIcon(String imageName) {
        if (IMAGE_ICON_CACHE.containsKey(imageName)) return IMAGE_ICON_CACHE.get(imageName);
        URL url = getResourceURL( imageName );
        if (url == null) return new ImageIcon();
        ImageIcon ret = new ImageIcon(url);
        ret.setDescription(null);
        IMAGE_ICON_CACHE.put(imageName, ret);
        return ret;
    }
    public static ImageIcon getFlagIcon(int countryId) {
        String filename = DIRNAME_FLAGS + '/' + Integer.toString(countryId) + ".png";
        ImageIcon flagIcon = null;
        if (IMAGE_ICON_CACHE.containsKey(filename)) return IMAGE_ICON_CACHE.get(filename);
        URL url = getResourceURL( filename );
        if (url != null) flagIcon = new ImageIcon( url );
        else {
            File flagFile = new File(filename);
            if (flagFile.canRead()) return new ImageIcon(filename, SO.labelManager.getCountryName(countryId));
            else {
                url = getResourceURL( FILENAME_IMG_UNKNOWNFLAG );
                if (url != null) flagIcon = new ImageIcon(url);
                else flagIcon = new ImageIcon();
            }
        }
        if (countryId > 0) flagIcon.setDescription(SO.labelManager.getCountryName(countryId));
        IMAGE_ICON_CACHE.put(filename, flagIcon);
        return flagIcon;
    }
    public static String getLabel(String label) {
        return SO.labelManager.getLabel(label);
    }
    public static String getSkillLevelName(int skill) {
        return SO.labelManager.getSkillLevelName(skill);
    }
    public static double getPositionRating(int position, PlayerProfile player) {
        return SO.positionManager.getPositionRating(position, player);
    }
    public static String getPositionShortName(int position) {
        return SO.labelManager.getPositionShortName(position);
    }
    public static String getPositionLongName(int position) {
        return SO.labelManager.getPositionLongName(position);
    }
    public static URL getResourceURL(String resourceName) {
        URL url = MainFrame.class.getResource( '/' + resourceName );
        return url;
    }
    public static File getResourceAsFile(String resourceName) {
        URL url = MainFrame.class.getResource( '/' + resourceName );
        if (url==null) return null;
        try {
            return new File(url.toURI());
        } catch (java.net.URISyntaxException use) { return null; }
    }

    public static int getTeamId() {
        return SO.team.getId();
    }

    public static String getTeamName(int id) {
        if (id == SO.team.getId()) return SO.team.getName();
        return "";
    }

    /* called from TeamDetails */
    public static void setCountry(int id) {
        SO.options.setCountry(id);
    }

    public static void setSelectedTab(java.awt.Component c) {
        SO.tabbedPane.setSelectedComponent(c);
    }

    /* only called from this class */
    private static void setLookAndFeel(String lf) {
        if (lf==null || lf.equals("")) return;
        try {
            UIManager.setLookAndFeel( lf );
        } catch (Exception e) { }
    }

    public static JFrame getOwnerForDialog() { return SO; }

    static void refreshPlayerNotesIcon() {
        SO.playerStatsPanel.refreshNotesIcon();
    }

    /* **************************************************************************** */
    /* Interfaz WindowListener */
    public void windowClosing (java.awt.event.WindowEvent windowEvent) {
        exitProgram();
    }
    public void windowClosed (java.awt.event.WindowEvent windowEvent) { }
    public void windowActivated(java.awt.event.WindowEvent windowEvent) { }    
    public void windowDeactivated(java.awt.event.WindowEvent windowEvent) { }
    public void windowDeiconified(java.awt.event.WindowEvent windowEvent) { }
    public void windowIconified(java.awt.event.WindowEvent windowEvent) { }
    public void windowOpened(java.awt.event.WindowEvent windowEvent) { }

    /* Interfaz ActionListener */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        try {
            String command = actionEvent.getActionCommand();
            if (command.equals(CMD_EXIT)) exitProgram();
            else if (downloadInProcess) return;
            if (command.equals(CMD_DOWNLOADALL)) downloadXMLData(true, true);
            else if (command.equals(CMD_DOWNLOADBASIC)) downloadXMLData(true, false);
            else if (command.equals(CMD_DL_LEAGUE_AND_MATCHES)) downloadXMLData(false, true);
            else if (command.equals(CMD_DL_MATCHLIST)) downloadMatchList();
            else if (command.equals(CMD_IMPORTXML)) importXML();
            else if (command.equals(CMD_IMPORT_SOKKERMON)) importSokkermon();
            else if (command.equals(CMD_IMPORT_APOLLO)) importApollo(1);
            else if (command.equals(CMD_IMPORT_APOLLO2)) importApollo(2);
            else if (command.equals(CMD_IMPORT_SV)) importSokkerViewer();
            else if (command.equals(CMD_OPTIONS)) showOptions();
            else if (command.equals(CMD_FORMULAS)) editFormulas();
            else if (command.equals(CMD_TRAIN_REP)) ReportFrame.showTrainingReport(roster);
            else if (command.equals(CMD_JUNIOR_REP)) ReportFrame.showJuniorReport(school);
            else if (command.equals(CMD_SCHOOL_REP)) ReportFrame.showSchoolReport(school);
            else if (command.equals(CMD_FLAGS_REP)) ReportFrame.showFlagsReport(matchRepo);
            else if (command.equals(CMD_CREDITS)) showCredits();
            else if (command.equals(CMD_CURRENCY_CONVERT)) showCurrencyConverter();
            else if (command.equals(CMD_LOGOMAKER) && flagsPlugin!=null) flagsPlugin.showLogoMakerDialog();
            else if (command.equals(CMD_NOTES) && team!=null) team.showManagerNotesEditor();
            else if (command.equals(CMD_SEND_TO_NTDB)) sendDataToNTDB();
            else if (command.equals(CMD_CHECKVERSION)) checkSOVersion(false);
            else if (command.equals(CMD_TEST)) doTest();
        } catch (Exception debug) {
            new DebugFrame(debug);
            enableDownload(true);
        }
    }
    /* ******************************** Menu Actions ******************************** */
    private void downloadXMLData(final boolean basic, final boolean leagueAndMatches) {
        int ret = options.showLoginDialog(this, labelManager);
        if (ret==JOptionPane.CLOSED_OPTION || ret==JOptionPane.CANCEL_OPTION) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    enableDownload(false);
                    int result = xmlHandler.fetchXMLData(basic, leagueAndMatches);
                    if (result == PARSE_FAILED) return null;
                    refreshPanels(result);
                    return "OK";
                }
                public void finished() {
                    if (options.getAutoSendToNTDB()) sendDataToNTDB();
                    else {
                        setCursor(null);
                        enableDownload(true);
                    }
                }
            };
        worker.start();
    }

    private void downloadMatchList() {
        MatchIdParser mip = new MatchIdParser(labelManager);
        final List<Integer> mids = mip.getMatchesList();
        if (mids==null || mids.isEmpty()) return;

        int ret = options.showLoginDialog(this, labelManager);
        if (ret==JOptionPane.CLOSED_OPTION || ret==JOptionPane.CANCEL_OPTION) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    enableDownload(false);
                    int result = xmlHandler.fetchMatches(mids);
                    if (result == PARSE_FAILED) return null;
                    refreshPanels(result);
                    return "OK";
                }
                public void finished() {
                    if (options.getAutoSendToNTDB()) sendDataToNTDB();
                    else {
                        setCursor(null);
                        enableDownload(true);
                    }
                }
            };
        worker.start();
    }

    private void importXML() {
        //show file dialog and get filename
        //if (!XMLHandler.ensureAccesibleDir(so.So.getDataDirName() + File.separator + DIRNAME_XML)) return;
        JFileChooser chooser = new JFileChooser(so.So.getBaseDirName() + so.So.getDataDirName() + File.separator + DIRNAME_XML);
        ExampleFileFilter filter = new ExampleFileFilter("xml", "XML");
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            enableDownload(false);
            int parseResult = PARSE_FAILED;
            File [] xmlSelectedFiles = chooser.getSelectedFiles();
            for (File xmlFile : xmlSelectedFiles) {
                parseResult = xmlHandler.parseXmlFile(xmlFile) | parseResult;
            }
            if (parseResult != PARSE_FAILED) refreshPanels(parseResult);
            enableDownload(true);
        }
    }

    private void importSokkermon() {
        /* make sure the country is set in the Options */
        if (options.getCountry() == NO_COUNTRY_SET) {
            JOptionPane.showMessageDialog(this, labelManager.getLabel(TXT_ERROR_NO_COUNTRY_SET),
                                          labelManager.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
            return;
        }
        //show file dialog and get filename
        //if (!XMLHandler.ensureAccesibleDir(DIRNAME_HTML)) return;
        JFileChooser chooser = new JFileChooser();
        ExampleFileFilter filter = new ExampleFileFilter();
        filter.addExtension("sok");
        filter.addExtension("jun");
        filter.setDescription("Sokkermon 5");
        ExampleFileFilter sokFilter = new ExampleFileFilter("sok", "Sokkermon 5");
        ExampleFileFilter junFilter = new ExampleFileFilter("jun", "Sokkermon 5");
        //chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(sokFilter);
        chooser.addChoosableFileFilter(junFilter);
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            enableDownload(false);
            int importResult = PARSE_FAILED;
            File [] htmlSelectedFiles = chooser.getSelectedFiles();
            HTMLHandler.SokkermonImporter skmon = htmlHandler.getSokkermonImporter();
            for (File htmlFile : htmlSelectedFiles) {
                importResult = skmon.parseSokkermonHTMLFile(htmlFile) | importResult;
                /* don't refresh the panels right away, do it only once in the end */
            }
            if (importResult != PARSE_FAILED) refreshPanels(importResult);
            skmon.storeTable();
            enableDownload(true);
        }
    }

    private void importApollo(int version) {
        //show file dialog and get filename
        JFileChooser chooser = new JFileChooser();
        ApolloFileFilter filter = new ApolloFileFilter("xml", "Apollo XML", version);
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            enableDownload(false);
            int parseResult = PARSE_FAILED;
            File [] xmlSelectedFiles = chooser.getSelectedFiles();
            for (File xmlFile : xmlSelectedFiles) {
                parseResult = xmlHandler.parseApolloXmlFile(xmlFile, version) | parseResult;
            }
            if (parseResult != PARSE_FAILED) refreshPanels(parseResult);
            enableDownload(true);
        }
    }

    private void importSokkerViewer() {
        //show file dialog and get filename
        JFileChooser chooser = new JFileChooser();
        SokkerViewerFileFilter filter = new SokkerViewerFileFilter("xml", "SokkerViewer XML");
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            enableDownload(false);
            int parseResult = PARSE_FAILED;
            File [] xmlSelectedFiles = chooser.getSelectedFiles();
            for (File xmlFile : xmlSelectedFiles) {
                parseResult = xmlHandler.parseSokkerViewerXmlFile(xmlFile) | parseResult;
            }
            if (parseResult != PARSE_FAILED) refreshPanels(parseResult);
            enableDownload(true);
        }
    }

    private void showOptions() {
        int ret = options.showOptionsDialog(this, labelManager, false);
        if (ret==OPT_NO_CHANGE) return;
        enableDownload(false);
        if ((ret & OPT_LANGUAGE) == OPT_LANGUAGE) {
            squadPanel.storeColumnSettings();
            schoolPanel.storeColumnSettings();
            setVisible(false);
            getContentPane().removeAll();
            javax.swing.SwingUtilities.updateComponentTreeUI(this);
            createGUI();
            setVisible(true);
        }
        else if ((ret & OPT_PLAF) == OPT_PLAF) {
            setVisible(false);
            //getContentPane().removeAll();
            javax.swing.SwingUtilities.updateComponentTreeUI(this);
            //createGUI();
            setVisible(true);
            //setSize(options.getWindowSize());
        }
        else {
            if ((ret & OPT_CUR_CONV_RATE)     == OPT_CUR_CONV_RATE) squadPanel.repaint();
            else if ((ret & OPT_CUR_SYMBOL)   == OPT_CUR_SYMBOL) squadPanel.repaint();
            if ((ret & OPT_SEASON_REF)      == OPT_SEASON_REF) squadPanel.refreshDateChooserPanel();
            if ((ret & OPT_LOW_SKILL)       == OPT_LOW_SKILL) schoolPanel.repaint();
            else if ((ret & OPT_HIGH_SKILL) == OPT_HIGH_SKILL) schoolPanel.repaint();
        }
        enableDownload(true);
    }
    private void editFormulas() {
        int ret = positionManager.showFormulasEditDialog(this, labelManager, roster);
        if (ret == OPT_FORMULAS) {
            roster.updatePositionRatings();
            squadPanel.refreshTable();
        }
    }
    private void showCredits() {
        String msg = so.So.getName() + " v" + so.So.getVersion() + " " + so.So.getReleaseStatus() + "\n" +
            so.So.getHomePage() + "\n\n" +
            labelManager.getLabel(TXT_AUTHOR) + " :   " + "Daniel Gonz\u00e1lez Fisher (danny)\n" +
            labelManager.getLabel(TXT_TRANSLATOR) + " (" + options.getLanguage() + ") :   " +
            labelManager.getLabel(TXT_TRANSLATOR_NAME);
        java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection(so.So.getHomePage());
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
        JOptionPane.showMessageDialog(this, msg, labelManager.getLabel(TXT_CREDITS), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCurrencyConverter() {
        new CurrencyConversionFrame(labelManager, options, this).setVisible(true);
    }

    private void sendDataToNTDB() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    enableDownload(false);
                    NTDBHandler ntdb = new NTDBHandler(MainFrame.this, options, labelManager, team);
                    ntdb.sendPlayersToNTDB(roster.getPlayersList());
                    return "OK";
                }
                public void finished() {
                    setCursor(null);
                    enableDownload(true);
                }
            };
        worker.start();
    }

    private void checkSOVersion(final boolean silent) {
        final SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    enableDownload(false);
                    try {
                        boolean res = xmlHandler.checkSOVersion(silent);
                        if (res) JOptionPane.showMessageDialog(MainFrame.this, labelManager.getLabel(TXT_RESTART_MSG));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return "OK";
                }
                public void finished() {
                    enableDownload(true);
                }
            };
        worker.start();
    }

    private void doTest() {
        //if (options.getLogonUsername().equals("Danny")) getStats();

//         System.out.println("base dir: " + so.So.getBaseDirName());
//         System.out.println("data dir: " + so.So.getDataDirName());
//         System.out.println("update url: " + so.So.getUpdateBaseURLName());

//         java.util.Date date = new java.util.Date(1202234655057L);
//         System.out.println(date.toString());
        //System.out.println("Danny : " + Base64Coder.encode("Danny") );
        //System.out.println("danny : " + Base64Coder.encode("danny") );


//         for (java.awt.Font f : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
//             System.out.println(f.getName() + "  :  " + f.getFontName() + "  @  " + f.getFamily());
//         }
//         System.out.println("===========================================================================");
//         for (String ffn : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
//             System.out.println(ffn);
//         }

        //office.debug();

        //new NTDBHandler(this, options, labelManager, team.getUserId()).sendPlayersToNTDB(roster.getPlayersList());
        //sendDataToNTDB();

//         try {
//         java.util.jar.JarFile jf = new java.util.jar.JarFile("MORE/langs.jar");
//         java.util.Enumeration<java.util.jar.JarEntry> enu = jf.entries();
//         while (enu.hasMoreElements()) {
//             java.util.jar.JarEntry je = enu.nextElement();
//             System.out.println( je.getName() );
//             System.out.println( " ¬ " + new File(je.getName()).getAbsolutePath() );
//         }
//         } catch(Exception e1) { }

//         SokkerCalendar sc = new SokkerCalendar(2006, SokkerCalendar.AUGUST, 1);
//         for (int i=0; i<150; i++) {
//             sc.add(SokkerCalendar.DAY_OF_MONTH, 1);
//             System.out.println(" --: " + sc.getWeekOfSeason() + " @ " + sc.getTime().toString() );
//         }


//         MatchRepository.TeamProfile tp = matchRepo.getTeam(41329);
//         if (tp == null) {
//             System.out.println("NULL");
//             return;
//         }
//         System.out.println(tp.getId());
//         System.out.println(tp.getName());
//         System.out.println(tp.getRank());

        /* Debug League */
//         LeagueDetails l = leaguesData.getLatestLeague();

//         for (LeagueDetails l : leaguesData.getLeagues()) {
            //if (l.getSeason()==8 && l.getName().equals("3.Liga.16")) leaguesData.deleteLeague(l.getSeason(), l.getId());
//         l.debug();
//             for (int r=1; r<=14; r++) {
//                 for (int n=1; n<=4; n++) {
//                     l.getMatch(r, n).debug(l);
//                 }
//             }
//         }

//         System.out.println("@ ACTUALES");
//         for (JuniorProfile jp : school.getJuniorsList()) System.out.println(jp.getName());
//         System.out.println("@ antiguos");
//         for (JuniorProfile jp : school.getFormerJuniorsList()) System.out.println(jp.getName());

        //matchRepo.debug();
        //matchRepo.save();

//          java.util.List<JuniorProfile> juniors = school.getJuniorsList();
//          for (JuniorProfile j : juniors) {
//            j.debug();
//          }

//         System.out.println(java.util.TimeZone.getDefault().getID());
//         for (String tz : java.util.TimeZone.getAvailableIDs()) System.out.println(tz);

    }

}
