package so.data;

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.config.Options;
import so.text.LabelManager;
import so.util.CookieManager;
import so.util.Utils;
import java.awt.Frame;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.Box;
import java.util.Set;
//import java.util.List;
//import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.util.Properties;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.nio.charset.Charset;
//import java.nio.ByteBuffer;
//import java.nio.CharBuffer;

public class HTMLHandler {
    private static final String CHARSET = "UTF-8";
    private static final String NT_SEARCH_STRING        = "glowna.php?teamID=";
    private static final String TEAMID_SEARCH_STRING    = "glowna.php?teamID=";
    private static final String MATCHID_SEARCH_STRING   = "comment.php?matchID=";
    //private static final String LEAGUEID_SEARCH_STRING  = URL_LEAGUE_PAGE + "?leagueID=";
    private static final String COUNTRYID_SEARCH_STRING = "country.php?ID_country=";
    private static final String PLAYERID_SEARCH_STRING  = "player.php?PID=";
    private static final String JUNIORID_SEARCH_STRING  = "juniors.php?action=SendOff&juniorID=";
    private static final String IND_MARK_SEARCH_STRING  = "stats.php?matchID=";
    private static final String MATCH_PREFIX     = "match-";
    private static final String MATCH_TEAM_AFFIX = "_team-";
    private static final String YELLOWCARD_SEARCH_STRING = "zolta.gif";
    private static final String REDCARD_SEARCH_STRING    = "czerwona.gif";
    private static final String INJURY_SEARCH_STRING     = "kontuzja.gif";

    private static final String SKMON_EXT_PLAYERS      = ".sok";
    private static final String SKMON_EXT_JUNIORS      = ".jun";
    private static final String SKMON_EXT_TRAINING     = ".trn";
    //private static final String SKMON_EXT_MATCH_REPORT = ".som";
    //private static final String SKMON_EXT_MATCH_LINEUP = ".soa";

    private Options options;
    private Frame frame;
    private LabelManager lm;
    private CookieManager cookieManager;
    //private ProgressDialog pdialog;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    private static final SimpleDateFormat SKMDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat MRDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //private int parsedTrainingType;
    //private int parsedTrainingPos;
    //private LeagueDetails parsedLeagueDetails; UNUSED
    private Date parsedDataDate;
    private Set<PlayerProfile> parsedPlayers;
    private Set<JuniorProfile> parsedJuniors;
    //private boolean trainingParseSuccess;
    //private boolean leagueParseSuccess;
    private boolean playersParseSuccess;
    private boolean juniorsParseSuccess;

    private PlayerRoster playerRoster;
    private JuniorSchool juniorSchool;

    public HTMLHandler(Options opt, Frame fr, LabelManager lman, PlayerRoster roster, JuniorSchool school) {
        options = opt;
        frame = fr;
        lm = lman;
        //pdialog = new ProgressDialog(fr);
        playerRoster = roster;
        juniorSchool = school;
        cookieManager = new CookieManager();
        initParsing();
    }
    private void initParsing() {
        //trainingParseSuccess = false;
        //leagueParseSuccess = false;
        parsedDataDate = null;
        //parsedTrainingType = 0;
        //parsedTrainingPos = 0;
    }

//     public boolean fetchHTMLs(Date date) {
//         return fetchHTMLs(date, null);
//     }
//     public boolean fetchHTMLs(Date date, List<PlayerProfile> players) {
//         /* check HTML save dir */
//         if (!XMLHandler.ensureAccesibleDir(DIRNAME_HTML)) return false;

//         pdialog.startProgress();
//         boolean res = false;
//         try {
//             res = internalFetch(date, players);
//         } catch (Exception e) { e.printStackTrace(); }
//         pdialog.endProgress();
//         return res;
//     }

//     public List<Integer> getHistoricalMatchesList(int teamId) {
//         ArrayList<Integer> matchIds = new ArrayList<Integer>();
//         boolean success = false;
//         String html;
//         pdialog.startProgress();
//         try {
//             /* get session cookies from index.php */
//             success = initSession();
//             if (!success) return null;
//             /* send login info to start.php */
//             success = doLogin();
//             if (!success) return null;
//             /* check login success by downloading glowna.php */
//             String glowna = getHTMLAsString(URL_TEAM_PAGE);
//             if (!checkLoginSuccess(glowna)) {
//                 return null;
//             }
//             /* get Matches list - matches.php?teamID=xxx&page=-y */
//             pdialog.setBarString(lm.getLabel(TXT_DL_MATCHES));
//             for (int i=1; ; i++) {
//                 html = getHTMLAsString(URL_MATCHES_PAGE + "?teamID=" + teamId + "&page=-" + i);
//                 int idx = 0;
//                 ArrayList<Integer> _mids = new ArrayList<Integer>();
//                 while(idx!=-1) {
//                     idx = html.indexOf(MATCHID_SEARCH_STRING, idx);
//                     if (idx==-1) break;
//                     idx += MATCHID_SEARCH_STRING.length();
//                     int mid = getIntFromString(html, idx);
//                     if (!matchIds.contains(mid)) _mids.add(mid);
//                 }
//                 if (_mids.isEmpty()) break;
//                 Collections.reverse(_mids);
//                 matchIds.addAll( _mids );
//             }
//             /* disconnect - index.php?action=start */
//             pdialog.setBarString(lm.getLabel(TXT_LOGOFF));
//             success = getHTMLAsNothing(URL_LOGOFF);
//         } catch (MalformedURLException mue) {
//             pdialog.endProgress();
//             return null;
//         } catch (IOException ioe) {
//             pdialog.endProgress();
//             return null;
//         }
//         pdialog.endProgress();
//         if (!matchIds.isEmpty()) Collections.reverse(matchIds);
//         return matchIds;
//     }


//     private boolean internalFetch(Date now, List<PlayerProfile> players) {
//         boolean success = false;
//         String fileName = null;
//         try {
//             /* get session cookies from index.php */
//             success = initSession();
//             if (!success) return false;
//             /* send login info to start.php */
//             success = doLogin();
//             if (!success) return false;
//             /* check login success by downloading glowna.php */
//             String glowna = getHTMLAsString(URL_TEAM_PAGE);
//             if (!checkLoginSuccess(glowna)) {
//                 JOptionPane.showMessageDialog(frame, lm.getLabel(TXT_ERROR_LOGIN_FAILED),
//                                               lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
//                 return false;
//             }

//             initParsing();
//             if (now == null) now = new Date();
//             parsedDataDate = now;
//             /* get League Table - league.php?leagueID=xxx */
//             SokkerCalendar _skCal = new SokkerCalendar(now);
//             int _wos = _skCal.getWeekOfSeason();
//             if (_wos<16) {
//                 pdialog.setBarString(lm.getLabel(TXT_DL_LEAGUE));
//                 fileName = getLeagueTable(glowna, now);
//                 if (fileName == null) return false;
//                 leagueParseSuccess = parseLeague(fileName, now);
//             }
//             /* get Training - training.php */
//             pdialog.setBarString(lm.getLabel(TXT_DL_TRAINING));
//             fileName = getHTMLAsFile(URL_TRAINING_PAGE, now, "training");
//             if (fileName == null) return false;
//             trainingParseSuccess = parseTraining(fileName, now);

//             /* get Economy - economy.php */
//             pdialog.setBarString(lm.getLabel(TXT_DL_ECONOMY));
//             fileName = getHTMLAsFile(URL_ECONOMY_PAGE, now, "economy");
//             if (fileName == null) return false;
//             //% aqui parse economy


//             checkNTCoach();
//             /* disconnect - index.php?action=start */
//             pdialog.setBarString(lm.getLabel(TXT_LOGOFF));
//             success = getHTMLAsNothing(URL_LOGOFF);
//         } catch (MalformedURLException mue) {
//             showErrorMessage(mue);
//             return false;
//         } catch (IOException ioe) {
//             showErrorMessage(ioe);
//             return false;
//         }
//         return true;
//     }

//     protected boolean initSession() throws IOException, MalformedURLException {
//         URL url;
//         URLConnection urlConn;
//         pdialog.setBarString(lm.getLabel(TXT_CONNECTING));
//         url = new URL (URL_HOSTNAME + URL_INIT);
//         urlConn = url.openConnection();
//         /* get Session cookies */
//         HttpURLConnection httpc = (HttpURLConnection)urlConn;
//         // only interested in the length of the resource
//         httpc.setRequestMethod("HEAD");
//         urlConn.connect();
//         pdialog.setBarString(lm.getLabel(TXT_BEGIN_SESSION));
//         cookieManager.storeCookies(urlConn);
//         httpc.getContentLength(); // int len =
//         return true;
//     }

//     protected boolean doLogin() throws IOException, MalformedURLException {
//         String username = options.getLogonUsername();
//         String password = options.getLogonPassword();

//         URL url;
//         URLConnection urlConn;
//         DataOutputStream printout;
        
//         url = new URL (URL_HOSTNAME + URL_START_PHP);
//         urlConn = url.openConnection();
//         urlConn.setDoInput (true);
//         urlConn.setDoOutput (true);
//         urlConn.setUseCaches (false);
//         urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//         cookieManager.setCookies(urlConn);
//         urlConn.connect();
//         pdialog.setBarString(lm.getLabel(TXT_LOGIN));
//         /* Send POST output. */
//         try {
//             printout = new DataOutputStream (urlConn.getOutputStream ());
//         } catch (UnknownHostException uhe) {
//             // "Unknown Host, try again"
//             pdialog.endProgress();
//             JOptionPane.showMessageDialog(frame, lm.getExtendedLabel(TXT_ERROR_UNKNOWN_HOST, url.getHost()), lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
//             return false;
//         } catch (java.net.SocketException se) {
//             pdialog.endProgress();
//             showErrorMessage(se);
//             return false;
//         }
//         String content = "ilogin=" + URLEncoder.encode(username,CHARSET) +
//             "&ipassword=" + URLEncoder.encode(password,CHARSET);
//         printout.writeBytes(content);
//         printout.flush ();
//         printout.close ();
//         urlConn.getInputStream();
//         BufferedInputStream input = new BufferedInputStream(urlConn.getInputStream ());
//         int c;
//         while (-1 != ((c = input.read() )) ) {
//             ;
//         }
//         input.close();
//         return true;
//     }

//     protected boolean checkLoginSuccess(String html) {
//         int idx = html.indexOf(TEAMID_SEARCH_STRING);
//         // se puede double-check con MATCHID_SEARCH_STRING partir de idx
//         return (idx != -1);
//     }

//     protected String getLeagueTable(String html, Date date) throws IOException, MalformedURLException {
//         String ss = "<OBJECT";
//         int idx = html.indexOf(ss);
//         if (idx == -1) return null;
//         idx += ss.length();
//         idx = html.indexOf(LEAGUEID_SEARCH_STRING, idx);
//         if (idx == -1) return null;
//         int leagueID = getIntFromString(html, idx + LEAGUEID_SEARCH_STRING.length());
//         if (leagueID == -1) return null;
//         return getHTMLAsFile(LEAGUEID_SEARCH_STRING + leagueID + "&action=fix", date, "leaguefix_" + leagueID);
//     }

//     protected void checkNTCoach() throws IOException, MalformedURLException {
//         String leftFrameHtml = getHTMLAsString(URL_LEFT_FRAME);
//         if (leftFrameHtml.indexOf(URL_NT_PAGE) == -1) return;

//         String ntPage = getHTMLAsString(URL_NT_PAGE);
//         int idx = ntPage.indexOf(NT_SEARCH_STRING);
//         if (idx == -1) return;
//         // System.out.println( ntPage.substring(idx, idx+36) );
//         idx += NT_SEARCH_STRING.length();
//         int countryNT = getIntFromString(ntPage, idx);
//         System.out.println("You are NT coach of " + lm.getCountryName(countryNT)  + " [" + countryNT + "]. CONGRATULATIONS! :)");
//     }

    // ======================= GET HTMLs =========================
    /* returns full file name with path */
    /* UNUSED */
//     protected String getHTMLAsFile(String htmlName, Date saveDate, String fileName) throws IOException, MalformedURLException {
//         BufferedInputStream input = new BufferedInputStream(getHTMLInputStream(htmlName));
//         if (input==null) return null;

//         BufferedOutputStream bos = null;
//         if (saveDate != null) {
//             if (fileName == null) fileName = getNewHTMLFilename(htmlName, saveDate);
//             else fileName = getNewHTMLFilename(fileName, saveDate);
//         }
//         else fileName = fileName + ".html";
//         fileName = so.So.getBaseDirName() + DIRNAME_HTML + File.separator + fileName;
//         try {
//             bos = new BufferedOutputStream(new FileOutputStream( fileName ));
//         } catch (Exception exc) {
//             new DebugFrame(exc);
//             pdialog.endProgress();
//             input.close();
//             return null;
//         }
//         int c;
//         while (-1 != ((c = input.read() )) ) {
//             bos.write(c);
//         }
//         bos.flush();
//         bos.close();
//         input.close();

//         return fileName;
//     }

    /* UNUSED */
//     protected String getHTMLAsString(String htmlName) throws IOException, MalformedURLException {
//         InputStream input = getHTMLInputStream(htmlName);
//         if (input==null) return null;
//         BufferedReader br = new BufferedReader(new InputStreamReader(input, CHARSET));
//         String line;
//         StringBuilder sb = new StringBuilder(1024);
//         while (null != (line = br.readLine())) {
//             sb.append(line + "\n");
//         }
//         br.close();
//         return sb.toString();
//     }

    /* UNUSED */
//     protected boolean getHTMLAsNothing(String htmlName) throws IOException, MalformedURLException {
//         URL url;
//         URLConnection urlConn;
//         url = new URL (URL_HOSTNAME + htmlName);
//         urlConn = url.openConnection();
//         urlConn.setUseCaches (false);
//         HttpURLConnection httpc = (HttpURLConnection)urlConn;
//         // only interested in the length of the resource
//         httpc.setRequestMethod("HEAD");
//         /* set cookies */
//         cookieManager.setCookies(urlConn);
//         urlConn.connect();
//         httpc.getContentLength(); // int len =
//         return true;
//     }

    // ==========================================================================
    /* UNUSED */
//     private InputStream getHTMLInputStream(String htmlName) throws IOException, MalformedURLException {
//         URL url;
//         URLConnection urlConn;
//         /* URL of CGI-Bin script. */
//         url = new URL (URL_HOSTNAME + htmlName);
//         urlConn = url.openConnection();
//         urlConn.setDoInput (true);
//         urlConn.setUseCaches (false);
//         /* set cookies */
//         cookieManager.setCookies(urlConn);
//         urlConn.connect();
//         /* Get response data. */
//         return urlConn.getInputStream();
//     }

//     private String getNewHTMLFilename(String urlfile, Date date) {
//         return SDF.format(date) + '_' + urlfile + ".html";
//     }

    private String readHTML(String fileName) {
        return readHTML(fileName, CHARSET);
    }
    private String readHTML(String fileName, String charSet) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            if (fis==null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, charSet));
            String line;
            StringBuilder sb = new StringBuilder(1024);
            while (null != (line = br.readLine())) {
                sb.append(line + "\n");
            }
            br.close();
            fis.close();
            return sb.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//     private void showErrorMessage(Throwable t) {
//         JOptionPane.showMessageDialog(frame, t.getLocalizedMessage(), lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
//     }

    // ==========================================================================
    // ==========================================================================
    /* Parse data */
//     public Date getParsedDataDate() { return parsedDataDate; }
//     public int getParsedTrainingType() { return parsedTrainingType; }
//     public int getParsedTrainingPos() { return parsedTrainingPos; }
//     public LeagueDetails getParsedLeagueDetails() { return parsedLeagueDetails; }
//     public Set<PlayerProfile> getParsedPlayers() { return parsedPlayers; }

//     public boolean trainingParsingSucceeded() { return trainingParseSuccess; }
//     public boolean leagueParsingSucceeded() { return leagueParseSuccess; }
//     public boolean playersParsingSucceeded() { return playersParseSuccess; }
    // ==========================================================================
//     public int parseSOHTMLFile(File file) {
//         initParsing();
//         String name = file.getName();
//         /* is a match? html */
//         if ( name.startsWith(MATCH_PREFIX) ) {
//             //matchParseSuccess = parseMatchFile( file );
//             return PARSE_FAILED;
//         }
//         /* has a date? */
//         int idx = name.indexOf("_");
//         if (idx==-1) return PARSE_FAILED;
//         idx = name.indexOf("_", idx+1);
//         if (idx==-1) return PARSE_FAILED;
//         idx++;
//         parsedDataDate = null;
//         try {
//             parsedDataDate = SDF.parse( name );
//         } catch (java.text.ParseException pe) { }
//         if (parsedDataDate==null) return PARSE_FAILED;
//         String aux = name.substring(idx);
//         if (aux.startsWith("economy")) {

//             return PARSE_ECONOMY;
//         }
//         else if (aux.startsWith("training")) {
//             trainingParseSuccess = parseTraining(file.getPath(), parsedDataDate);
//             return PARSE_TRAINING;
//         }
//         else if (aux.startsWith("leaguefix")) {
//             leagueParseSuccess = parseLeague(file.getPath(), parsedDataDate);
//             return PARSE_LEAGUE;
//         }
//         else return PARSE_FAILED;
//     }
    // ==========================================================================
//     private boolean parseTraining(String fileName, Date date) {
//         String html = readHTML(fileName);
//         String ss = "<form action=\"training.php\"";
//         int idx = html.indexOf(ss);
//         if (idx==-1) return false;
//         idx += ss.length();
//         ss = "<option selected value=\"";
//         idx = html.indexOf(ss, idx);
//         if (idx==-1) return false;
//         idx += ss.length();
//         int type = getIntFromString(html, idx);
//         if (type<1 || type>8) return false;
//         idx = html.indexOf(ss, idx);
//         if (idx==-1) return false;
//         idx += ss.length();
//         int pos = getIntFromString(html, idx);
//         if (pos<0 || pos>3) return false;
//         parsedTrainingType = type;
//         parsedTrainingPos = pos;
//         trainingParseSuccess = true;
//         parsedDataDate = date;
//         return true;
//     }

    // ==========================================================================
//     private boolean parseLeague(String fileName, Date date) {
//         SokkerCalendar _skCal = new SokkerCalendar(date);
//         int _wos = _skCal.getWeekOfSeason();
//         if (_wos==16) return false;
//         String html = readHTML(fileName);
//         String aux, ss;
//         int idx, idx2;
//         LeagueDetails league = new LeagueDetails();
//         /* get league name */
//         ss = "<H1 class=\"barHeader\">";
//         idx  = html.indexOf(ss);
//         if (idx == -1) return false;
//         idx2 = html.indexOf("</H1>");
//         aux = html.substring(idx+ss.length(), idx2);
//         league.setName(aux);
//         /* get league id */
//         idx = html.indexOf(LEAGUEID_SEARCH_STRING, idx2);
//         if (idx == -1) return false;
//         idx += LEAGUEID_SEARCH_STRING.length();
//         league.setID( getIntFromString(html, idx) );
//         /* get country */
//         idx = html.indexOf(COUNTRYID_SEARCH_STRING, idx);
//         if (idx == -1) return false;
//         idx += COUNTRYID_SEARCH_STRING.length();
//         league.setCountry( getIntFromString(html, idx) );
//         /* get season */
//         ss  = "<TD";
//         idx = html.indexOf(ss, idx) + ss.length();
//         idx = html.indexOf(ss, idx) + ss.length();
//         idx = html.indexOf(">", idx) + 1;
//         so.util.SokkerCalendar sokCal = new so.util.SokkerCalendar();
//         sokCal.setTime( date );
//         league.setSeason( sokCal.getSeason() );
//         /* get round */
//         idx = html.indexOf(ss, idx) + ss.length();
//         idx = html.indexOf(ss, idx) + ss.length();
//         idx = html.indexOf(">", idx) + 1;
//         int round = getIntFromString(html, idx);
//         //league.setRound( round );
//         int _id;
//         String _aux, _aux2;
//         /* get team names and ids */
//         for (int i=0; i<8; i++) {
//             idx = html.indexOf(TEAMID_SEARCH_STRING, idx);
//             if (idx == -1) return false;
//             idx += TEAMID_SEARCH_STRING.length();
//             _id = getIntFromString(html, idx);
//             idx  = html.indexOf(">", idx) + 1;
//             idx2 = html.indexOf("<", idx);
//             _aux = html.substring(idx, idx2);
//             league.addTeam(_id, _aux);
//         }
//         /* get match rounds */
//         int r;
//         for (r=1; r<=round+1; r++) {
//             int gl, gv;
//             boolean stop = false;
//             for (int i=0; i<4; i++) {
//                 idx = html.indexOf(MATCHID_SEARCH_STRING, idx);
//                 if (idx == -1) {
//                     stop = true;
//                     break;
//                 }
//                 idx += MATCHID_SEARCH_STRING.length();
//                 _id = getIntFromString(html, idx);
//                 idx  = html.indexOf("> ", idx) + 2;
//                 idx2 = html.indexOf(" - ", idx);
//                 _aux = html.substring(idx, idx2);
//                 idx = idx2 + 3;
//                 idx2 = html.indexOf(" <", idx);
//                 _aux2 = html.substring(idx, idx2);
//                 ss = "<td>";
//                 idx  = html.indexOf(ss, idx2+2) + ss.length();
//                 gl = getIntFromString(html, idx);
//                 idx = html.indexOf(":", idx) + 1;
//                 gv = getIntFromString(html, idx);
//                 league.addMatch(r, _id, _aux, _aux2, gl, gv, i+1);
//                 idx2 = idx;
//             }
//             if (stop) break;
//         }
//         round = r - 1;
//         league.setRound( round );
//         if (r > 14) league.rebuildTeamTable();
//         for ( ; r<=14; r++) {
//             ss = "<table";
//             idx = html.indexOf(ss, idx2);
//             if (idx == -1) return false;
//             idx += ss.length();
//             ss = "<td";
//             idx = html.indexOf(ss, idx);
//             if (idx == -1) return false;
//             idx += ss.length();
//             for (int i=0; i<4; i++) {
//                 ss = "<td> ";
//                 idx = html.indexOf(ss, idx) + ss.length();
//                 ss = " - ";
//                 idx2 = html.indexOf(ss, idx);
//                 _aux = html.substring(idx, idx2);
//                 idx = idx2 + ss.length();
//                 ss = " </td>";
//                 idx2 = html.indexOf(ss, idx);
//                 _aux2 = html.substring(idx, idx2);
//                 ss = "<td>";
//                 idx = html.indexOf(ss, idx2+5) + ss.length();
//                 league.addMatch(r, _aux, _aux2, i+1);
//             }
//             if (r == round+1) league.rebuildTeamTable();
//         }
//         parsedLeagueDetails = league;
//         parsedDataDate = date;
//         return true;
//     }

    // ==========================================================================
    private short _getFieldAsShort(String text, int offset) {
        int idx = text.indexOf('>', offset) + 1;
        int idx2 = text.indexOf('<', idx);
        return (short)Utils.getIntFromString( text.substring(idx, idx2).replaceAll("&nbsp;", "") , 0);
    }


    /* ================================================================================ */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* ================================================================================ */
    public class SokkermonImporter {
        private Properties skillsTable;

        private SokkermonImporter() {
            skillsTable = new Properties();
            String tableFile = so.So.getBaseDirName() + so.So.getDataDirName() + File.separator + FILENAME_HTML_PARSE_TABLE;
            try {
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(tableFile));
                skillsTable.load(is);
                is.close();
            } catch(Exception e) { }
        }
        
        public int parseSokkermonHTMLFile(File file) {
            //initParsing();
            String name = file.getName();
            /* is a match report html (.som) NO */
            /* is a match lineup html (.soa) NO */
            /* get Date from filename */
            parsedDataDate = null;
            try {
                parsedDataDate = SKMDF.parse( name );
            } catch (java.text.ParseException pe) {
                if (so.So.DEBUG) pe.printStackTrace();
            }
            if (parsedDataDate==null) return PARSE_FAILED;

            /* set hour to 10:00:00.000 */
            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedDataDate);
            cal.set(Calendar.HOUR, 12);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            parsedDataDate = cal.getTime();

            /* is a players html (.sok) */
            if ( name.endsWith(SKMON_EXT_PLAYERS) ) {
                playersParseSuccess = parsePlayers(file.getAbsolutePath(), parsedDataDate);
                return PARSE_PLAYERS;
            }
            /* is a juniors html (.jun) */
            else if ( name.endsWith(SKMON_EXT_JUNIORS) ) {
                juniorsParseSuccess = parseJuniors(file.getAbsolutePath(), parsedDataDate);
                return PARSE_JUNIORS;
            }
            /* is a training html (.trn) */
            else if ( name.endsWith(SKMON_EXT_TRAINING) ) {
                //% FALTA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                return PARSE_TRAINING;
            }
            else return PARSE_FAILED;
        }

        private boolean parsePlayers(String fileName, Date date) {
            playersParseSuccess = false;
            parsedPlayers = new HashSet<PlayerProfile>();
            String html = readHTML(fileName);
            String charSet = getCharset(html);
            if (!CHARSET.equals(charSet)) html = readHTML(fileName, charSet);

            int idx = html.indexOf(TEAMID_SEARCH_STRING);
            if (idx == -1) return false;
            idx += TEAMID_SEARCH_STRING.length();
            int ownerTeamId = Utils.getIntFromString(html, idx);
            int idx2, idx3;
            String ss, ss2;
            while ( (idx=html.indexOf(PLAYERID_SEARCH_STRING, idx))!=-1 ) {
                idx += PLAYERID_SEARCH_STRING.length();
                int id = Utils.getIntFromString(html, idx);
                idx = html.indexOf('>', idx) + 1;
                idx2 = html.indexOf('<', idx);
                String fullName = html.substring(idx, idx2);
                if (so.So.DEBUG) System.out.println(fullName);
                idx = html.indexOf(", ", idx2+1);
                ss = "flags/";
                idx2 = html.indexOf(ss, idx2+1);
                /* country */
                short country = (short)options.getCountry();
                if (idx2>0 && idx2<idx) { //has a flag, i.e. he is foreign player
                    country = (short)Utils.getIntFromString(html, idx2+ss.length());
                }
                if (country < 0) {
                    //ask user
                }
                /* age */
                idx = html.indexOf(' ', idx+2) + 1;
                short ag = (short)Utils.getIntFromString(html, idx);
                idx = html.indexOf(": ", idx) + 2;
                /* value can also have enlosing FONT tags for PLUS users */
                if (html.charAt(idx)=='<') idx = html.indexOf('>', idx) + 1;
                int vl = (int)(Utils.getIntFromString(html, idx) / options.getCurrencyConversionRate());
                idx = html.indexOf(": ", idx) + 2;
                /* (just in case salary also has FONT tags) */
                if (html.charAt(idx)=='<') idx = html.indexOf('>', idx) + 1;
                int sy = (int)(Utils.getIntFromString(html, idx) / options.getCurrencyConversionRate());
                /* form */
                ss = "<br>";
                idx = html.indexOf(ss, idx) + ss.length();
                idx3 = html.indexOf(ss, idx);
                idx2 = html.lastIndexOf(' ', idx3-1);
                short fm = getSkillLvlFromName( html.substring(idx, idx2).trim() );

                idx = idx3 + ss.length();
                ss = "</td>";
                idx3 = html.indexOf(ss, idx2+1);
                /* bookings */
                short cd = 0;
                idx2 = html.indexOf(REDCARD_SEARCH_STRING, idx);
                if (idx<idx2 && idx2<idx3) cd = 3;
                else {
                    idx2 = html.indexOf(YELLOWCARD_SEARCH_STRING, idx);
                    if (idx<idx2 && idx2<idx3) {
                        idx2 = html.indexOf(YELLOWCARD_SEARCH_STRING, idx2+YELLOWCARD_SEARCH_STRING.length());
                        if (idx<idx2 && idx2<idx3) cd = 2;
                        else cd = 1;
                    }
                }
                /* injuries */
                float in = 0f;
                idx2 = html.indexOf(INJURY_SEARCH_STRING, idx);
                if (idx<idx2 && idx2<idx3) {
                    idx2 += INJURY_SEARCH_STRING.length();
                    idx = html.indexOf('(', idx2) + 1;
                    in = Utils.getIntFromString(html, idx);
                    if (in == -1) in = 3.5f;
                }
                /* skills */
                ss = "<td>";
                idx = html.indexOf(ss, idx3);
                if (idx==-1) continue;
                idx += ss.length();
                ss  = "<b>";
                ss2 = "</b>";
                idx = html.indexOf(ss, idx) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short st = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short pc = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short te = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short ps = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short kp = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short df = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short pm = getSkillLvlFromName( html.substring(idx, idx2) );
                idx = html.indexOf(ss, idx2 + ss2.length()) + ss.length();
                idx2 = html.indexOf(ss2, idx);
                short sc = getSkillLvlFromName( html.substring(idx, idx2) );

                String [] _na = fullName.split("\\s+", 2);
                String name = "", surname = "";
                switch (_na.length) {
                case 1:
                    surname = fullName;
                    break;
                case 2:
                    name = _na[0];
                    surname = _na[1];
                    break;
                default:
                    surname = fullName;
                    // ask user
                }
                //int tid = so.gui.MainFrame.getTeamId();
                PlayerProfile player = new PlayerProfile(id, name, surname, country, ownerTeamId, ownerTeamId);
                short _z = 0;
                player.addPlayerData(date, vl, sy, ag, 0, _z, _z, _z, fm, st, pc, te, ps, kp, df, pm, sc, cd, (int)in, 0,0,0, false, false, false, 0,0,0,0);
                parsedPlayers.add(player);
                //% System.out.println("----------------");
                //% player.debug();
                idx = idx2 + ss2.length();
            }

            playersParseSuccess = true;
            parsedDataDate = date;
            playerRoster.updatePlayers( parsedPlayers , date );
            return true;
        }

        private boolean parseJuniors(String fileName, Date date) {
            juniorsParseSuccess = false;
            parsedJuniors = new HashSet<JuniorProfile>();
            String html = readHTML(fileName);
            String charSet = getCharset(html);
            if (!CHARSET.equals(charSet)) html = readHTML(fileName, charSet);

            int idx = html.indexOf("juniors.php");
            if (idx == -1) return false;
            idx += "juniors.php".length();
            //int ownerTeamId = Utils.getIntFromString(html, idx);
            int idx2, idx3;
            String ss, ss2;
            while ( (idx=html.indexOf(JUNIORID_SEARCH_STRING, idx))!=-1 ) {
                idx3 = idx + JUNIORID_SEARCH_STRING.length();
                int id = Utils.getIntFromString(html, idx3);
                idx3 = html.indexOf('>', idx3) + 1;
                ss = "<tr>";
                idx = html.lastIndexOf(ss, idx) + ss.length();
                /* name */
                ss = "<b>";
                idx = html.indexOf(ss, idx) + ss.length();
                ss = "</b>";
                idx2 = html.indexOf(ss, idx);

                String fullName = html.substring(idx, idx2);
                if (so.So.DEBUG) System.out.println(fullName);
                /* skill level */
                ss = "<td";
                idx = html.indexOf(ss, idx2) + ss.length();
                idx = html.indexOf('>', idx) + 1;
                ss = "</td>";
                idx2 = html.indexOf(ss, idx);
                short skill = getSkillLvlFromName( html.substring(idx, idx2).trim() );
                /* weeks */
                ss = "<td";
                idx = html.indexOf(ss, idx2) + ss.length();
                idx = html.indexOf('>', idx) + 1;
                short weeks =(short) Utils.getIntFromString(html, idx);
                if (weeks<0) weeks = 0;
                String [] _na = fullName.split("\\s+", 2);
                String name = "", surname = "";
                switch (_na.length) {
                case 1:
                    surname = fullName;
                    break;
                case 2:
                    name = _na[0];
                    surname = _na[1];
                    break;
                default:
                    surname = fullName;
                    // ask user
                }

                JuniorProfile junior = new JuniorProfile(id, name, surname);
                junior.addJuniorData(date, weeks, skill, (short)0);
                parsedJuniors.add(junior);
                System.out.println("----------------");
                junior.debug();
                idx = idx3;
            }

            juniorsParseSuccess = true;
            parsedDataDate = date;
            juniorSchool.updateJuniors( parsedJuniors, date );
            return true;
        }

        protected short getSkillLvlFromName(String skillName) {
            if (skillName == null) return 0;
            skillName = skillName.replaceAll("\\<[^\\>]+\\>", "");
            String lvl = skillsTable.getProperty(skillName);
            if (lvl != null) {
                try {
                    return Short.parseShort(lvl);
                } catch (NumberFormatException nfe) { }
            }
            Vector<String> skills = lm.getSkillLevelNames();
            for (int i=0; i<skills.size(); i++) {
                if ( skillName.equals(skills.get(i)) ) {
                    skillsTable.setProperty(skillName, Integer.toString(i));
                    return (short)i;
                }
            }
            for (int i=0; i<skills.size(); i++) {
                if ( skillName.startsWith(skills.get(i)) ) {
                    skillsTable.setProperty(skillName, Integer.toString(i));
                    return (short)i;
                }
            }
            skills = lm.getDefaultSkillLevelNames();
            for (int i=0; i<skills.size(); i++) {
                if ( skillName.equals(skills.get(i)) ) {
                    skillsTable.setProperty(skillName, Integer.toString(i));
                    return (short)i;
                }
            }
            for (int i=0; i<skills.size(); i++) {
                if ( skillName.startsWith(skills.get(i)) ) {
                    skillsTable.setProperty(skillName, Integer.toString(i));
                    return (short)i;
                }
            }
            JComboBox comboSkill = new JComboBox(skills);
            Box box = new Box(javax.swing.BoxLayout.Y_AXIS);
            box.add(new javax.swing.JLabel(lm.getExtendedLabel(TXT_UNRECOGNIZED_SKILL, skillName)));
            box.add(comboSkill);
            // ask for skill
            JOptionPane.showMessageDialog(frame, box, lm.getLabel(TXT_CH_SKILL), JOptionPane.QUESTION_MESSAGE);
            int skl = comboSkill.getSelectedIndex();
            // put it in table
            skillsTable.setProperty(skillName, Integer.toString(skl));
            return (short)skl;
        }

        public void storeTable() {
            String tableFile = so.So.getBaseDirName() + so.So.getDataDirName() +File.separator+ FILENAME_HTML_PARSE_TABLE;
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tableFile));
                skillsTable.store(bos, " Cached entries for HTML parsing");
                bos.close();
            } catch (Exception e) { }
        }
    } // END class SokkermonImporter

    public SokkermonImporter getSokkermonImporter() {
        return new SokkermonImporter();
    }

    public static String getCharset(String content) {
        String ss = "charset=";
        int idx = content.indexOf( ss );
        if (idx == -1) return "UTF-8";
        idx += ss.length();
        int idx2 = content.indexOf('"', idx);
        return content.substring(idx, idx2);
    }

//     public static int getIntFromString(String str, int offset) {
//         if (offset<0) return -1;
//         try {
//             char c;
//             StringBuilder _sub = new StringBuilder();
//             boolean firstChar = true;
//             for (int i = offset; i<str.length(); i++) {
//                 c = str.charAt(i);
//                 if (firstChar && c=='-') _sub.append(c);
//                 if (Character.isDigit(c)) {
//                     _sub.append(c);
//                     firstChar = false;
//                 }
//                 else if (Character.isSpaceChar(c)) continue;
//                 else break;
//             }
//             if (_sub.length() == 0) return -1;
//             return Integer.parseInt( _sub.toString() );
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return -1;
//     }

}
