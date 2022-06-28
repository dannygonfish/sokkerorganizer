package so;

import static so.Constants.*;
import so.gui.MainFrame;
import so.gui.LoadingFrame;
import so.util.DebugFrame;
import java.net.URL;
import java.io.File;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Calendar;
import javax.swing.ImageIcon;
import java.awt.Image;
import javax.swing.UIManager;

public final class So {
    private static final String SO_NAME = "Sokker Organizer";
    private static final double SO_VERSION = 0.95;
    private static final String SO_RELEASE_STATUS = "BETA";
    private static final String SO_SPECIAL_MSG = "";
    private static final String DANNY_BDAY_MSG = "Happy Birthday to Danny :)";
    private static final String SO_HOMEPAGE = "http://sk.danny.cl/so/";

    public static boolean DEBUG = false;
    public static boolean CONSOLE = false;
    public static boolean JAVAWS = false;

    private static File HOMEDIR = null;
    private static String DIRNAME_BASEDIR = "";
    private static String UPDATE_BASE_URL = "http://sk.danny.cl/so/";
    private static String DIRNAME_DATA = Constants.DIRNAME_DATA;

    private static Image SO_IMAGE = null;
    private static boolean usingOverrides = false;

    public static void main(String [] args) {
        if (args.length>0) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-DEBUG")) DEBUG = true;
                else if (arg.equalsIgnoreCase("-CONSOLE")) CONSOLE = true;
                else if (arg.equalsIgnoreCase("-JAVAWS")) JAVAWS = true;
            }
        }
        if (!CONSOLE) {
            System.setOut(new PrintStream(new so.util.DebugOutputStream("System.out")));
            System.setErr(new PrintStream(new so.util.DebugOutputStream("System.err " + SO_VERSION)));
        }

        System.setProperty("http.agent", "SO/" + SO_VERSION);

        if (JAVAWS) {
            HOMEDIR = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory();
        }
        if (HOMEDIR!=null) {
            DIRNAME_BASEDIR = HOMEDIR.getAbsolutePath() + File.separator + SO_NAME + File.separator;
            if (!so.data.XMLHandler.ensureAccesibleDir(DIRNAME_BASEDIR)) DIRNAME_BASEDIR = "";;
        }

        final LoadingFrame loadingFrame = new LoadingFrame(SO_NAME);
        checkOverrides();
        removeExcessFiles(); //% REMOVE in 1.0!!!
        initPlafs();
        //javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame main = new MainFrame(SO_NAME + " v" + SO_VERSION + ' ' + SO_RELEASE_STATUS
                                               + (usingOverrides?" (!) ":' ') + SO_SPECIAL_MSG, loadingFrame);
                Calendar cal = Calendar.getInstance();
                if (cal.get(Calendar.MONTH)==Calendar.OCTOBER && cal.get(Calendar.DATE)==22)
                    main.setTitle(SO_NAME + " v" + SO_VERSION + ' ' + SO_RELEASE_STATUS + ' ' + DANNY_BDAY_MSG);
                main.setVisible(true);
                loadingFrame.dispose();
            }
        });

    }

    public static Image loadSoImage() {
        if (SO_IMAGE == null) {
            try {
                URL url = So.class.getResource( "/" + FILENAME_IMG_SOICON );
                if (url == null) return null;
                SO_IMAGE = (new ImageIcon(url)).getImage();
            } catch (Exception e) { }
        }
        return SO_IMAGE;
    }

    public static String getName() { return SO_NAME; }
    public static double getVersion() { return SO_VERSION; }
    public static String getReleaseStatus() { return SO_RELEASE_STATUS; }
    public static String getHomePage() { return SO_HOMEPAGE; }

    public static String getBaseDirName() { return DIRNAME_BASEDIR; }
    public static String getDataDirName() { return DIRNAME_DATA; }
    public static String getUpdateBaseURLName() { return UPDATE_BASE_URL; }

    /* ###################################################### */
    private static void initPlafs() {
        if (JAVAWS) {
            UIManager.installLookAndFeel("NimROD", "com.nilo.plaf.nimrod.NimRODLookAndFeel");
            return;
        }
        Properties props = new Properties();
        if (!so.data.XMLHandler.ensureAccesibleDir(DIRNAME_PLAFJARS)) return;
        try {
            String filename = DIRNAME_PLAFJARS + File.separator + FILENAME_PLAFS_DATA;
            java.io.BufferedInputStream is = new java.io.BufferedInputStream(new java.io.FileInputStream(filename));
            props.load(is);
            is.close();
            java.util.Set keys = props.keySet();
            for (Object k : keys) {
                String value, name, className;
                int idx;
                if ( new File(DIRNAME_PLAFJARS + "/" +k.toString()).exists() ) {
                    value = (String)props.get(k);
                    idx = value.indexOf(",");
                    if (idx == -1) continue;
                    name = value.substring(0, idx);
                    className = value.substring(idx+1);
                    UIManager.installLookAndFeel(name, className);
                }
            }
        } catch(java.io.IOException ioe) {
        } catch(IllegalArgumentException iae) { iae.printStackTrace();
        } catch(Exception e) { new DebugFrame(e);
        }
    }
    /* ###################################################### */
    private static void checkOverrides() {
        Properties overrides = new Properties();
        try {
            //String filename = FILENAME_OVERRIDES;
            File oFile = new File(FILENAME_OVERRIDES);
            if (!oFile.exists()) return;
            java.io.BufferedInputStream is = new java.io.BufferedInputStream(new java.io.FileInputStream(oFile));
            overrides.load(is);
            is.close();

            String prop = overrides.getProperty("UPDATE_BASE_URL");
            if (prop!=null) {
                UPDATE_BASE_URL = prop;
                usingOverrides = true;
            }

            prop = overrides.getProperty("DIRNAME_DATA");
            if (prop != null) {
                DIRNAME_BASEDIR = "";
                DIRNAME_DATA = prop;
                usingOverrides = true;
            }
        } catch(Exception e) { /*new DebugFrame(e);*/
        }
    }

    /* ###################################################### */
    //% REMOVE IN 1.0!!!
    private static void removeExcessFiles() {
        File delFile;
        try {
            delFile = new File(DIRNAME_LANG + File.separator + "Catal\u00e0.txt");
            if (delFile.exists()) delFile.delete();
            delFile = new File(DIRNAME_LANG + File.separator + "Espa\u00f1ol.txt");
            if (delFile.exists()) delFile.delete();
            delFile = new File(DIRNAME_LANG + File.separator + "Portugu\u00eas (BR).txt");
            if (delFile.exists()) delFile.delete();
            delFile = new File(DIRNAME_LANG + File.separator + "Portugu\u00eas (PT).txt");
            if (delFile.exists()) delFile.delete();
            delFile = new File(DIRNAME_LANG + File.separator + "Fran\u00e7ais.txt");
            if (delFile.exists()) delFile.delete();
            delFile = new File(DIRNAME_LANG + File.separator + "T\u00fcrk\u00e7e.txt");
            if (delFile.exists()) delFile.delete();
        } catch(Exception e) { }

    }
}
