package so.text;

/**
 * LabelManager.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import static so.Constants.Positions.*;
import so.util.*;
import so.data.CountryDataUnit;
import java.util.Properties;
import java.util.Vector;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.Collator;

public class LabelManager {
    private static final String LN_CATALA       = "Catal\u00e0";
    private static final String LN_CESTINA      = "\u010ce\u0161tina";
    private static final String LN_ROMANA       = "Rom\u00e2n\u00e3";
    private static final String LN_ELLENIKA     = "\u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac";
    private static final String LN_BULGARIAN    = "\u0411\u044a\u043b\u0433\u0430\u0440\u0441\u043a\u0438";
    private static final String LN_SLOVENCINA   = "Sloven\u010dina";
    private static final String LN_RUSSKII      = "\u0420\u0443\u0441\u0441\u043a\u0438\u0439";
    private static final String LN_SIMP_CHINESE = "\u7b80\u4f53\u4e2d\u6587";
    private static final String LN_ESPANOL      = "Espa\u00f1ol";
    private static final String LN_TURKCE       = "T\u00fcrk\u00e7e";
    private static final String LN_FRANCAIS     = "Fran\u00e7ais";
    private static final String LN_PORTUGUES_BR = "Portugu\u00eas (BR)";
    private static final String LN_PORTUGUES_PT = "Portugu\u00eas (PT)";
    private static final String LN_AZERI        = "Az\u0259ri";

    private static final String FN_CATALA       = "Catala";
    private static final String FN_CESTINA      = "Cestina";
    private static final String FN_ROMANA       = "Romana";
    private static final String FN_ELLENIKA     = "Ellenika";
    private static final String FN_BULGARIAN    = "Bulgarian";
    private static final String FN_SLOVENCINA   = "Slovencina";
    private static final String FN_RUSSKII      = "Russkii";
    private static final String FN_SIMP_CHINESE = "SChinese";
    private static final String FN_ESPANOL      = "Espanol";
    private static final String FN_TURKCE       = "Turkce";
    private static final String FN_FRANCAIS     = "Francais";
    private static final String FN_PORTUGUES_BR = "Portugues_BR";
    private static final String FN_PORTUGUES_PT = "Portugues_PT";
    private static final String FN_AZERI        = "Azeri";

    private static final String [] LANGNAMES = { LN_CATALA, LN_CESTINA, LN_ROMANA, LN_ELLENIKA, LN_BULGARIAN,
                                                 LN_SLOVENCINA, LN_RUSSKII, LN_SIMP_CHINESE, LN_ESPANOL, LN_TURKCE,
                                                 LN_FRANCAIS, LN_PORTUGUES_BR, LN_PORTUGUES_PT, LN_AZERI };
    private static final String [] FILENAMES = { FN_CATALA, FN_CESTINA, FN_ROMANA, FN_ELLENIKA, FN_BULGARIAN,
                                                 FN_SLOVENCINA, FN_RUSSKII, FN_SIMP_CHINESE, FN_ESPANOL, FN_TURKCE,
                                                 FN_FRANCAIS, FN_PORTUGUES_BR, FN_PORTUGUES_PT, FN_AZERI };

    private static Vector<CountryDataUnit> countries;

    private String language;
    private Properties defProps;
    private Properties props;

    public LabelManager() {
        this(DEFAULT_LANG);
    }
    public LabelManager(String language) {
        this.language = language;

        defProps = new Properties();
        if (!loadDefaultPropertiesFromJar()) {
            if (!loadDefaultPropertiesFromFile()) {
                System.err.println("SEVERE ERROR! Missing default language file: " + FILENAME_DEFAULT_LANG);
            }
        }

        if (!loadLanguage(language)) loadLanguage(DEFAULT_LANG);
    }
    private boolean loadDefaultPropertiesFromJar() {
        String resName = '/' + DIRNAME_CONFIGFILES + '/' + FILENAME_EMBEDDED_LANG;
        InputStream is = null;
        try {
            is = so.So.class.getResourceAsStream(resName);
            if (is == null) {
                System.err.println("Could not get Default Language file stream from JAR resource " + resName);
                return false;
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            defProps.load(bis);
            bis.close();
        } catch(IOException e) {
            DebugFrame df = new DebugFrame(e);
            df.setText(resName);
            return false;
        } catch(IllegalArgumentException iae) {
            new DebugFrame(iae);
            return false;
        }
        return true;
    }

    private boolean loadDefaultPropertiesFromFile() {
        InputStream is = null;
        try {
            is = new FileInputStream(DIRNAME_LANG + File.separator + FILENAME_DEFAULT_LANG);
            BufferedInputStream bis = new BufferedInputStream(is);
            defProps.load(bis);
            bis.close();
            System.err.println("Loading Default Language file from " + DIRNAME_LANG+File.separator+FILENAME_DEFAULT_LANG);
        } catch(IOException ioe) {
            DebugFrame df = new DebugFrame(ioe);
            df.setText(DIRNAME_LANG + "/" + FILENAME_DEFAULT_LANG);
            return false;
        } catch(IllegalArgumentException iae) {
            new DebugFrame(iae);
            return false;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean refreshLanguage() {
        return loadLanguage(language);
    }

    public boolean changeLanguage(String lang) {
        if (lang == null) return false;
        if (!loadLanguage(lang)) return false;
        language = lang;
        return true;
    }

    protected boolean loadLanguage(String lang) {
        if (lang == null) return false;
        if (lang.equals(DEFAULT_LANG)) {
            props = defProps;
            return true;
        }
        String archName = DIRNAME_LANG + '/' + langNameToFile(lang) + ".txt";
        //File arch = new File(archName);
        //if (!arch.exists()) return false;
        Properties _props = new Properties(defProps);
        try {
            //BufferedInputStream is = new BufferedInputStream(new FileInputStream(arch));
            java.net.URL langUrl = so.gui.MainFrame.getResourceURL(archName);
            if (langUrl == null) return false;
            BufferedInputStream is = new BufferedInputStream(langUrl.openStream());
            _props.load(is);
            is.close();
        } catch(IOException e) {
            new DebugFrame(e);
            return false;
        } catch(IllegalArgumentException iae) {
            new DebugFrame(iae);
            return false;
        }
        props = _props;
        return true;
    }

    public Vector<String> getLanguages() {
        Vector<String> lista = new Vector<String>();

        if (so.So.JAVAWS) {
            try {
                BufferedReader buf = new BufferedReader( new InputStreamReader(so.gui.MainFrame.getResourceURL(DIRNAME_CONFIGFILES + '/' + FILENAME_LANGUAGE_LIST).openStream()) );
                String entry = null;
                while ((entry=buf.readLine()) != null) {
                    String langFileName = new File(entry).getName();
                    if (langFileName.equals(FILENAME_DEFAULT_LANG)) continue;
                    lista.add( langFileToName( getNameWithoutExtension(langFileName) ) );
                }
                buf.close();
            } catch(Exception e1) {
                e1.printStackTrace();
                return lista;
            }
        }
        else {
            ExampleFileFilter filter = new ExampleFileFilter("txt");
            File folder = new File( DIRNAME_LANG );
            String [] langfilenames = folder.list( filter );
            if (langfilenames==null) return lista; // in case there are no language files, not even default
            lista.ensureCapacity(langfilenames.length + 1);
            for (String filename : langfilenames) {
                if (filename.equals(FILENAME_DEFAULT_LANG)) continue;
                lista.add( langFileToName(getNameWithoutExtension(filename)) );
            }
        }

        Collator coll = Collator.getInstance();
        coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        coll.setStrength(Collator.SECONDARY);
        java.util.Collections.sort(lista, coll);
        lista.add(0, DEFAULT_LANG);
        return lista;
    }

    //========================================================================

    public String getLabel(String label) {
        if (props == null) return "###";
        return props.getProperty(label, "<?" + label + "?>");
    }
    public String getExtendedLabel(String label, String arg) {
        if (props == null) return "###";
        String ret = props.getProperty(label, "<?" + label + "?>");
        ret = ret.replace( "%" , arg );
        return ret;
    }
    public String getPluralityLabel(String label, boolean plural) {
        if (props == null) return "###";
        String ret = props.getProperty(label, "<?" + label + "?>");
        int idx = ret.indexOf('$');
        if (idx == -1) return ret;
        if (plural) return ret.substring(idx+1);
        else return ret.substring(0, idx);
    }

    public String getCountryName(int id) {
        return getLabel("c_" + Integer.toString(id));
    }
    public String getSkillLevelName(int skill) {
        return getLabel( "skill_" + Integer.toString(skill) );
    }
    public String getDefaultSkillLevelName(int skill) {
        return defProps.getProperty("skill_" + Integer.toString(skill), "<?skill_" + Integer.toString(skill) + "?>");
    }
    public String getMoodLevelName(int mood) {
        return getLabel( "mood_" + Integer.toString(mood+1) );
    }

    public String getCurrentLanguageName() {
        return getLabel("englishname");
    }
    public double getCurrentLanguageVersion() {
        try {
            return Double.parseDouble(getLabel("langver"));
        } catch (NumberFormatException nfe) {
            return 0.0;
        }
    }

    public Vector<CountryDataUnit> getCountriesVector() {
        if (countries == null) {
            countries = new Vector<CountryDataUnit>();
            for (int i=1; i<135; i++) {
                if (defProps.containsKey( "c_" + Integer.toString(i)) ) countries.add(new CountryDataUnit(i));
            }
            java.util.Collections.sort(countries);
        }
        return countries;
    }

    public Vector<String> getSkillLevelNames() {
        Vector<String> lista = new Vector<String>();
        for (int i=0; i<=17; i++) lista.add( getSkillLevelName(i) );
        return lista;
    }
    public Vector<String> getDefaultSkillLevelNames() {
        Vector<String> lista = new Vector<String>();
        for (int i=0; i<=17; i++) lista.add( getDefaultSkillLevelName(i) );
        return lista;
    }

    private String getNameWithoutExtension(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        if(i>0 && i<filename.length()-1) return filename.substring(0,i);
        return filename;
    }

    public String getPositionShortName(int pos) {
        if (pos == NO_POSITION) return "";
        int pure_pos = pos & ~(P_LEFT | P_RIGHT | RESERVE);
        if ((pos & P_LEFT)==P_LEFT) {
            return getExtendedLabel( "pos_left" , getLabel("pos_" + Integer.toString(pure_pos)) );
        }
        else if ((pos & P_RIGHT)==P_RIGHT) {
            return getExtendedLabel( "pos_right" , getLabel("pos_" + Integer.toString(pure_pos)) );
        }
        else return getLabel( "pos_" + Integer.toString(pos) );
    }
    public String getPositionLongName(int pos) {
        if (pos == NO_POSITION) return "";
        int pure_pos = pos & ~(P_LEFT | P_RIGHT | RESERVE);
        if ((pos & P_LEFT)==P_LEFT) {
            return getExtendedLabel( "posext_left" , getLabel("posext_" + Integer.toString(pure_pos)) );
        }
        else if ((pos & P_RIGHT)==P_RIGHT) {
            return getExtendedLabel( "posext_right" , getLabel("posext_" + Integer.toString(pure_pos)) );
        }
        return getLabel( "posext_" + Integer.toString(pos) );
    }

    public static String langNameToFile(String name) {
        String filename = name;
        for (int i=0; i<LANGNAMES.length; i++) {
            if (name.equals(LANGNAMES[i])) {
                filename = FILENAMES[i];
                break;
            }
        }
        return filename;
    }

    public static String langFileToName(String filename) {
        String name = filename;
        for (int i=0; i<FILENAMES.length; i++) {
            if (filename.equals(FILENAMES[i])) {
                name = LANGNAMES[i];
                break;
            }
        }
        return name;
    }

}
