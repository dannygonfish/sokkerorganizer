package so.config;

import static so.Constants.*;
import so.util.DebugFrame;
import so.gui.MainFrame;
import java.util.Properties;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class CountryUtils {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    private static Properties countryData;

    private static void loadCountryData() {
        countryData = new Properties();
        InputStream is = null;
        try {
            try {
                is = MainFrame.getResourceURL(DIRNAME_CONFIGFILES + '/' + FILENAME_COUNTRY_DATA).openStream();
            } catch (NullPointerException npe) {
                is = MainFrame.getResourceURL(DIRNAME_CONFIGFILES + '/' + FILENAME_EMBEDDED_COUNTRY_DATA).openStream();
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            countryData.load(bis);
            bis.close();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch(IllegalArgumentException iae) {
            new DebugFrame(iae);
            return;
        } catch(Exception e) {
            DebugFrame df = new DebugFrame(e);
            df.setText(DIRNAME_CONFIGFILES + '/' + FILENAME_COUNTRY_DATA);
            return;
        }
    }

    public static String getCurrencySymbolForCountry(int id) {
        if (countryData == null) loadCountryData();
        String data = countryData.getProperty(Integer.toString(id));
        if (data==null) return "$";
        String [] fields = data.split(";",-3);
        if (fields.length==0 || fields[0].equals("")) return "$";
        return fields[0];
    }

    public static double getCurrencyConversionRateForCountry(int id) {
        if (countryData == null) loadCountryData();
        double ret = 0.0;
        String data = countryData.getProperty(Integer.toString(id));
        if (data==null) return ret;
        String [] fields = data.split(";",-3);
        if (fields.length<=1 || fields[1].equals("")) return ret;
        try {
            ret = Double.parseDouble(fields[1]);
        } catch (NumberFormatException nfe) { }
        return ret;
    }

    public static int getInitialReferenceSeasonForCountry(int id) {
        if (countryData == null) loadCountryData();
        int ret = 1;
        String data = countryData.getProperty(Integer.toString(id));
        if (data==null) return ret;
        String [] fields = data.split(";",-3);
        if (fields.length<=2 || fields[2].equals("")) return ret;
        try {
            ret = Integer.parseInt(fields[2]);
        } catch (NumberFormatException nfe) { }
        return ret;
    }

    public static Calendar getReferenceSeasonStartForCountry(int id) {
        if (countryData == null) loadCountryData();
        GregorianCalendar greg = new GregorianCalendar(2006, Calendar.AUGUST, 6);
        String data = countryData.getProperty(Integer.toString(id));
        if (data==null) return greg;
        String [] fields = data.split(";",-3);
        java.util.Date _date = null;
        if (fields.length<=3 || fields[3].equals("")) return greg;
        try {
            _date = SDF.parse(fields[3]);
        } catch (java.text.ParseException nfe) {
            return greg;
        }
        greg.setTime(_date);
        return greg;
    }

}
