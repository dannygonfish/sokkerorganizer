package so.data;

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.config.Options;
import so.text.LabelManager;
import so.util.DebugFrame;
import so.util.Base64Coder;
import so.gui.ProgressDialog;
import java.io.*;
import java.net.*;
import java.awt.Frame;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.Properties;

public class NTDBHandler {
    private static final String CHARSET = "UTF-8";
    private static Properties NTDBURLs = null;

    private ProgressDialog pdialog;
    private Frame frame;
    private Options options;
    private LabelManager lm;
    private TeamDetails team;

    public NTDBHandler(java.awt.Frame fr, Options opt, LabelManager lman, TeamDetails td) {
        if (NTDBURLs==null) loadNTDBURLs();
        pdialog = new ProgressDialog(fr);
        options = opt;
        lm = lman;
        frame = fr;
        team = td;
    }


    private static void loadNTDBURLs() {
        NTDBURLs = new Properties();
        InputStream is = null;
        try {
            try{
                is = so.gui.MainFrame.getResourceURL(DIRNAME_CONFIGFILES + '/' + FILENAME_NTDB_URLS).openStream();
            } catch (NullPointerException npe) {
                is = so.gui.MainFrame.getResourceURL(DIRNAME_CONFIGFILES + '/' + FILENAME_EMBEDDED_NTDB).openStream();
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            NTDBURLs.load(bis);
            bis.close();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch(IllegalArgumentException iae) {
            new DebugFrame(iae);
            return;
        } catch(Exception e) {
            DebugFrame df = new DebugFrame(e);
            df.setText(DIRNAME_CONFIGFILES + '/' + FILENAME_NTDB_URLS);
            return;
        }
    }

    public void sendPlayersToNTDB(List<PlayerProfile> players) {
        boolean check = false;
        int totalProgress = 1;
        for (PlayerProfile pp : players) {
            if (pp.allowSendToNTDB()) {
                check = true;
                if (isNtdbUrlSet(pp.getCountryFrom())) totalProgress+=2;
            }
        }
        if (!check) return;

        pdialog.startProgress(totalProgress);
        if (options.getUseProxy()) {
			System.getProperties().put("http.proxyHost", options.getProxyHostname());
			System.getProperties().put("http.proxyPort", options.getProxyPort());
        }
        else {
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
        }
        pdialog.setBarString("NTDB");
        for (PlayerProfile pp : players) {
            try {
                if (pp.allowSendToNTDB() && isNtdbUrlSet(pp.getCountryFrom())) sendToNTDB(pp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pdialog.endProgress();
    }

    protected boolean sendToNTDB(PlayerProfile pp) throws IOException, MalformedURLException {
        URL url;
        URLConnection urlConn;
        DataOutputStream printout;
        pdialog.setBarString("NTDB: " + lm.getLabel(TXT_CONNECTING));
        /* URL of NTDB receiver script. */
        url = new URL( NTDBURLs.getProperty(Integer.toString(pp.getCountryFrom())) );
        /* URL connection channel. */
        urlConn = url.openConnection();
        /* Let the run-time system (RTS) know that we want input. */
        urlConn.setDoInput (true);
        /* Let the RTS know that we want to do output. */
        urlConn.setDoOutput (true);
        /* No caching, we want the real thing. */
        urlConn.setUseCaches (false);
        /* Specify the content type. */
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        /* use Proxy Authentication if enabled */
        if (options.getUseProxy() && options.getUseProxyAuth()) {
            String auth = options.getProxyUsername() + ':' + options.getProxyPassword();
            String encodedAuth = Base64Coder.encode( auth );
            urlConn.setRequestProperty( "Proxy-Authorization", "Basic " + encodedAuth );
        }
        /* Send POST output. */
        try {
            printout = new DataOutputStream (urlConn.getOutputStream());
        } catch (UnknownHostException uhe) {
            JOptionPane.showMessageDialog(frame, lm.getExtendedLabel(TXT_ERROR_UNKNOWN_HOST, url.getHost()),
                                          lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (java.net.SocketException se) {
            showErrorMessage(se);
            return false;
        }
        String content = "pid=" + URLEncoder.encode(Integer.toString(pp.getId()), CHARSET) +
                         "&playername=" + URLEncoder.encode(pp.getFullName(), CHARSET) +
                         "&name=" + URLEncoder.encode(pp.getName(), CHARSET) +
                         "&surname=" + URLEncoder.encode(pp.getSurname(), CHARSET) +
                         "&teamid=" + URLEncoder.encode(Integer.toString(pp.getOwnerTeamId()), CHARSET) +
                         "&teamname=" + URLEncoder.encode(team.getName(), CHARSET) +
                         "&userid=" + URLEncoder.encode(Integer.toString(team.getUserId()), CHARSET) +
                         "&username=" + URLEncoder.encode(options.getLogonUsername(), CHARSET) +
                         "&countryid=" + URLEncoder.encode(Integer.toString(pp.getCountryFrom()), CHARSET) +
                         "&value=" + URLEncoder.encode(Integer.toString(pp.getValue()), CHARSET) +
                         "&wage=" + URLEncoder.encode(Integer.toString(pp.getSalary()), CHARSET) +
                         "&age=" + URLEncoder.encode(Integer.toString(pp.getAge()), CHARSET) +
                         "&exp=" + URLEncoder.encode(Integer.toString(pp.getExperience()), CHARSET) +
                         "&teamwork=" + URLEncoder.encode(Integer.toString(pp.getTeamWork()), CHARSET) +
                         "&form=" + URLEncoder.encode(Integer.toString(pp.getForm()), CHARSET) +
                         "&sta=" + URLEncoder.encode(Integer.toString(pp.getStamina()), CHARSET) +
                         "&pac=" + URLEncoder.encode(Integer.toString(pp.getPace()), CHARSET) +
                         "&tec=" + URLEncoder.encode(Integer.toString(pp.getTechnique()), CHARSET) +
                         "&pas=" + URLEncoder.encode(Integer.toString(pp.getPassing()), CHARSET) +
                         "&kee=" + URLEncoder.encode(Integer.toString(pp.getKeeper()), CHARSET) +
                         "&def=" + URLEncoder.encode(Integer.toString(pp.getDefender()), CHARSET) +
                         "&pla=" + URLEncoder.encode(Integer.toString(pp.getPlaymaker()), CHARSET) +
                         "&str=" + URLEncoder.encode(Integer.toString(pp.getScorer()), CHARSET) +
                         "&train=" + URLEncoder.encode("", CHARSET);
        pdialog.setBarString("NTDB: " + pp.getFullName());
        if (so.So.DEBUG) System.out.println("¬ NTDB: " + pp.getFullName());
        printout.writeBytes(content);
        printout.flush ();
        printout.close ();
        /* Get response data. */
        InputStream input = urlConn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(input, CHARSET));
        String line;
        while (null != (line = br.readLine())) {
            if (so.So.DEBUG) System.out.println(line);
            //System.out.println(line);
        }
        br.close();
        return true;
    }

    public static boolean isNtdbUrlSet(int countryId) {
        if (NTDBURLs==null) loadNTDBURLs();
        String url = NTDBURLs.getProperty(Integer.toString(countryId));
        if (url==null) return false;
        else return true;
    }

    private void showErrorMessage(Throwable t) {
        if (so.So.DEBUG) t.printStackTrace();
        JOptionPane.showMessageDialog(frame, t.toString(), lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
    }

}
