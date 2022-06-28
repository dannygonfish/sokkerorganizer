package so.data;

// Sokker Organizer Files:
//   yyyy-MM-dd_HH-mm_team_TID.xml
//   yyyy-MM-dd_HH-mm_players_TID.xml
//   yyyy-MM-dd_HH-mm_trainers.xml
//   yyyy-MM-dd_HH-mm_juniors.xml
// 
// Sokker Viewer:
//      team_TID_week?_1_millis.xml
//   juniors_TID_week?_1_millis.xml
//   players_TID_week?_1_millis.xml
//
// Apollo 2.0 xml
//       team-yyyy-MM-dd-HH-mm.xml
//    players-yyyy-MM-dd-HH-mm.xml
//    juniors-yyyy-MM-dd-HH-mm.xml
//   trainers-yyyy-MM-dd-HH-mm.xml


import static so.Constants.*;
import static so.Constants.Labels.*;
import so.config.Options;
import so.text.LabelManager;
import so.util.DebugFrame;
import so.util.CookieManager;
import so.util.Base64Coder;
import so.util.SokkerCalendar;
import so.util.Dialog;
import so.util.Utils;
import so.gui.ProgressDialog;
import java.awt.Frame;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import com.toedter.calendar.JDateChooser;

public class XMLHandler {
    private static final String CHARSET = "UTF-8";

    private Options options;
    private Frame frame;
    private LabelManager lm;
    private CookieManager cookieManager;
    private ProgressDialog pdialog;
    private static final SimpleDateFormat SODF = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    private static final SimpleDateFormat XDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat APOLLODF = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

    private TeamDetails teamDetails;
    private PlayerRoster playerRoster;
    private JuniorSchool juniorSchool;
    private Stadium stadium;
    private CoachOffice coachOffice;
    private LeagueEncapsulator leagueEncapsulator;
    private MatchRepository matchRepo;

    private Date parseDate;
    private static int xmlDownloadCounter;

    public XMLHandler(Options opt, Frame fr, LabelManager lman, TeamDetails td, PlayerRoster roster, JuniorSchool school,
                      Stadium stad, CoachOffice office, LeagueEncapsulator league, MatchRepository repo) {
        options = opt;
        frame = fr;
        lm = lman;
        cookieManager = new CookieManager();
        teamDetails = td;
        playerRoster = roster;
        juniorSchool = school;
        stadium = stad;
        coachOffice = office;
        leagueEncapsulator = league;
        matchRepo = repo;
        pdialog = new ProgressDialog(fr);
        parseDate = null;
        xmlDownloadCounter = 0;
    }

    public Date getParseDate() { return parseDate; }

    public int fetchXMLData(boolean basic, boolean leagueAndMatches) {
        /* check XML save dir */
        if (!ensureAccesibleDir(so.So.getBaseDirName() + so.So.getDataDirName() + File.separator + DIRNAME_XML)) return PARSE_FAILED;
        pdialog.startProgress( 3 + (basic?4:0) + (leagueAndMatches?4:0) );
        int res = 0;
        try {
            res = internalFetchXMLData(basic, leagueAndMatches, null);
        } catch (Exception e) { e.printStackTrace(); }
        pdialog.endProgress();
        return res;
    }

    public int fetchMatches(List<Integer> matchIdsList) {
        pdialog.startProgress(4 + matchIdsList.size());
        int res = 0;
        try {
            res = internalFetchXMLData(false, false, matchIdsList);
        } catch (Exception e) { e.printStackTrace(); }
        pdialog.endProgress();
        return res;
    }

    private int internalFetchXMLData(boolean basic, boolean leagueAndMatches, List<Integer> matchIdsList) {
        File file;
        Document doc;
        if (options.getUseProxy()) {
			System.getProperties().put("http.proxyHost", options.getProxyHostname());
			System.getProperties().put("http.proxyPort", options.getProxyPort());
        }
        else {
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
        }
        int result = 0;
        try {
            /* login and get session cookies from start.php?session=xml */
            int teamId = initSession();
            if (teamId<1) return PARSE_FAILED;
            /* block banned SO users */
            if (teamId==22733) {
                JOptionPane.showMessageDialog(frame, "Your team is banned from using SO. Contact Danny for details", lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
                return PARSE_FAILED;
            }

            Date now = new Date();
            /* set hour to HH:MM:00.000 */
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            now = cal.getTime();

            if (basic) {
                /* get team-xxx.xml */
                pdialog.setBarString(lm.getLabel(TXT_DL_TEAM));
                file = getXMLAsFile("team-"+teamId+".xml", now);
                doc = getDocFromFile(file);
                /* parse team Data */
                result = parseTeam(doc, now) | result;

                //registerSO(); // remove in 0.8 !!!

                /* get trainers.xml */
                pdialog.setBarString(lm.getLabel(TXT_DL_TRAINING));
                file = getXMLAsFile("trainers.xml", now);
                doc = getDocFromFile(file);
                /* parse Coaches */
                result = parseCoaches(doc, now) | result;

                /* parse Stadium */
                //result = parseStadium(doc, now) | result;

                /* get players-xxx.xml */
                pdialog.setBarString(lm.getLabel(TXT_DL_PLAYERS));
                file = getXMLAsFile("players-"+teamId+".xml", now);
                doc = getDocFromFile(file);
                /* parse Players */
                result = parsePlayers(doc, now) | result;

                /* get juniors.xml */
                pdialog.setBarString(lm.getLabel(TXT_DL_JUNIORS));
                file = getXMLAsFile("juniors.xml", now);
                doc = getDocFromFile(file);
                /* parse Juniors */
                result = parseJuniors(doc, now) | result;
            }
            if (leagueAndMatches) {
                /* parse League Table */
                pdialog.setBarString(lm.getLabel(TXT_DL_LEAGUE));
                doc = getDocFromUrl("leagues-team.xml");
                result = parseLeague(doc, now) | result;
                /* parse Matches */
                pdialog.setBarString(lm.getLabel(TXT_DL_MATCHES));
                doc = getDocFromUrl("matches-team-" + teamId + ".xml");
                result = parseMatches(doc, now) | result;
            }
            if (matchIdsList != null) {
                /* parse Match Ids list */
                pdialog.setBarString(lm.getLabel(TXT_DL_MATCHES));
                result = parseMatchesFromList(matchIdsList) | result;
            }
            parseDate = now;
        } catch (MalformedURLException mue) {
            showErrorMessage(mue);
            return PARSE_FAILED;
        } catch (IOException ioe) {
            showErrorMessage(ioe);
            return PARSE_FAILED;
        }
        return result;
    }

    /* ###################################################################### */

    /* returns the teamId if successful, else -1 */
    protected int initSession() throws IOException, MalformedURLException {
        URL url;
        URLConnection urlConn;
        DataOutputStream printout;
        pdialog.setBarString(lm.getLabel(TXT_CONNECTING));
        /* URL of CGI-Bin script. */
        url = new URL (URL_HOSTNAME + URL_XML_INIT);
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
            //% System.err.println("Unknown Host, try again");
            JOptionPane.showMessageDialog(frame, lm.getExtendedLabel(TXT_ERROR_UNKNOWN_HOST, url.getHost()),
                                          lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
            return -1;
        } catch (java.net.SocketException se) {
            showErrorMessage(se);
            return -1;
        }
        String content = "ilogin=" + URLEncoder.encode(options.getLogonUsername(), CHARSET) +
                         "&ipassword=" + URLEncoder.encode(options.getLogonPassword(), CHARSET);
        pdialog.setBarString(lm.getLabel(TXT_LOGIN));
        printout.writeBytes(content);
        printout.flush ();
        printout.close ();
        /* Get response data. */
        InputStream input = urlConn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(input, CHARSET));
        String line;
        StringBuilder sb = new StringBuilder(1024);
        while (null != (line = br.readLine())) {
            sb.append(line + "\n");
            //System.out.println(line); //%
        }
        br.close();
        /* get Team ID */
        int teamId = checkLoginSuccess(sb.toString());
        /* is login ok? */
        if ( teamId < 1 ) return -1;
        /* get Session cookies */
        pdialog.setBarString(lm.getLabel(TXT_BEGIN_SESSION));
        cookieManager.storeCookies(urlConn);
        return teamId;
    }
    /* returns the teamId if successful, else -1 */
    private int checkLoginSuccess(String loginResponse) {
        if (loginResponse.indexOf("OK")==-1) {
            JOptionPane.showMessageDialog(frame, lm.getLabel(TXT_ERROR_LOGIN_FAILED),
                                          lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        String ss = "teamID=";
        //pdialog.setBarString(loginResponse);
        return Utils.getIntFromString(loginResponse, loginResponse.indexOf(ss)+ss.length());
    }

    /* ######################################################### */
    public int parseXmlFile(File xmlFile) {
        /* Compute Date */
        Date date = getDateFromFileName(xmlFile, SODF);
        if (date==null) {
            JOptionPane.showMessageDialog(frame, lm.getLabel(TXT_ERROR_INVALID_DATE) + " : " + xmlFile.getName(), lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
            return PARSE_FAILED;
        }
        String xmlFilename = xmlFile.getName();
        /* Convert XML into Java Document */
        Document doc = getDocFromFile(xmlFile);
        if (doc == null) return PARSE_FAILED;
        /* parse */
        int idx = xmlFilename.indexOf('_');
        if (idx==-1) return PARSE_FAILED;
        idx = xmlFilename.indexOf('_', idx+1);
        if (idx != -1) {
            if (xmlFilename.startsWith("team", idx+1)) return parseTeam(doc, date);
            else if (xmlFilename.startsWith("players", idx+1)) return parsePlayers(doc, date);
            else if (xmlFilename.startsWith("juniors", idx+1)) return parseJuniors(doc, date);
            else if (xmlFilename.startsWith("trainers", idx+1)) return parseCoaches(doc, date);
        }
        else {
            /* parse OLD Xml File */
            /* if doc is empty (no team tag) then stop */
            Node team = doc.getElementsByTagName("team").item(0);
            if (team == null) {
                JOptionPane.showMessageDialog(frame, "XML file is invalid : " + xmlFilename, lm.getLabel(TXT_ERROR),
                                              JOptionPane.ERROR_MESSAGE);
                return PARSE_FAILED;
            }

            /* Parse different sections of data */
            int res = 0;
            res = OldXMLHandler.parseTeam(doc, date, teamDetails) | res;
            res = OldXMLHandler.parsePlayers(doc, date, playerRoster) | res;
            res = OldXMLHandler.parseJuniors(doc, date, juniorSchool) | res;
            //res = OldXMLHandler.parseStadium(doc, date) | res;
            res = OldXMLHandler.parseCoaches(doc, date, coachOffice) | res;
            return res;
        }
        return PARSE_FAILED;
    }

    /* team_{tid}_{week}_{day}_{time in millis}.xml */
    public int parseSokkerViewerXmlFile(File xmlFile) {
        String xmlFilename = xmlFile.getName();
        /* Compute Date */
        int idx = xmlFilename.lastIndexOf('_');
        if (idx==-1) return PARSE_FAILED;
        long auxl = Utils.getLongFromString(xmlFilename, idx+1);
        if (auxl<0) return PARSE_FAILED;
        Date date = new Date(auxl);
        /* Convert XML into Java Document */
        Document doc = getDocFromFile(xmlFile);
        if (doc == null) return PARSE_FAILED;
        /* parse */
        idx = xmlFilename.indexOf('_');
        if (idx==-1) return PARSE_FAILED;
        int tid = Utils.getIntFromString(xmlFilename, idx+1);
        if (tid<1) return PARSE_FAILED;
        if (teamDetails.getId()!=tid && teamDetails.getId()!=TEAMID_NO_TEAM) return PARSE_FAILED;
        idx = xmlFilename.indexOf('_', idx+1);
        /* week and day not parsed */
        if (idx != -1) {
            if (xmlFilename.startsWith("team_")) return parseTeam(doc, date);
            else if (xmlFilename.startsWith("players_")) return parsePlayers(doc, date);
            else if (xmlFilename.startsWith("juniors_")) return parseJuniors(doc, date);
            else if (xmlFilename.startsWith("trainers_")) return parseCoaches(doc, date);
        }
        return PARSE_FAILED;
    }

    public int parseApolloXmlFile(File xmlFile, int version) {
        /* Compute Date */
        Date date = null;
        if (version == 1) date = getDateFromFileName(xmlFile, APOLLODF);
        else {
            int _p = xmlFile.getName().indexOf('-'); // quitar 'team-' o 'players-' etc
            date = getDateFromFileName(xmlFile.getName().substring(_p + 1), APOLLODF);
        }
        if (date==null) {
            JOptionPane.showMessageDialog(frame, lm.getLabel(TXT_ERROR_INVALID_DATE) + " : " + xmlFile.getName(), lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
            return PARSE_FAILED;
        }
        String xmlFilename = xmlFile.getName();
        /* Convert XML into Java Document */
        StringBuilder sb = new StringBuilder(1024);
        String charset = java.nio.charset.Charset.defaultCharset().name();
        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(xmlFile));
            if (input==null) return PARSE_FAILED;
            BufferedReader br = new BufferedReader(new InputStreamReader(input, charset));
            int c;
            while (-1 != (c = br.read())) {
                if (c>9) sb.append((char)c);
            }
            br.close();
        } catch (IOException ioe) { return PARSE_FAILED; }
        /* replace corrupted stuff */
        String xmlString = sb.toString().replaceAll("- <", "  <").trim();
        xmlString = xmlString.replaceFirst("UTF-8", charset);
        StringReader inputReader = new StringReader(xmlString);

        DocumentBuilder db = getDocumentBuilder();
        if (db == null) return PARSE_FAILED;
        Document doc = null;
        try {
            InputSource is = new InputSource(inputReader);
            doc = db.parse( is );
            inputReader.close();
        } catch (org.xml.sax.SAXException saxe) {
            saxe.printStackTrace();
            inputReader.close();
            return PARSE_FAILED;
        } catch (IOException ioe) {
            showErrorMessage(ioe);
            inputReader.close();
            return PARSE_FAILED;
        } catch (Exception e2) {
            new DebugFrame(e2);
            return PARSE_FAILED;
        }
        if (doc == null) return PARSE_FAILED;

        /* parse */
        if (version != 1) {
            if (xmlFilename.startsWith("team-")) return parseTeam(doc, date);
            else if (xmlFilename.startsWith("players-")) return parsePlayers(doc, date);
            else if (xmlFilename.startsWith("juniors-")) return parseJuniors(doc, date);
            else if (xmlFilename.startsWith("trainers-")) return parseCoaches(doc, date);
            else return PARSE_FAILED;
        }
        else {
            /* parse Apollo (OLD) Xml File */
            /* if doc is empty (no team tag) then stop */
            Node team = doc.getElementsByTagName("team").item(0);
            if (team == null) {
                JOptionPane.showMessageDialog(frame, "XML file is invalid : " + xmlFilename, lm.getLabel(TXT_ERROR),
                                              JOptionPane.ERROR_MESSAGE);
                return PARSE_FAILED;
            }
            /* Parse different sections of data */
            int res = 0;
            res = OldXMLHandler.parseTeam(doc, date, teamDetails) | res;
            res = OldXMLHandler.parsePlayers(doc, date, playerRoster) | res;
            res = OldXMLHandler.parseJuniors(doc, date, juniorSchool) | res;
            //res = OldXMLHandler.parseStadium(doc, date) | res;
            res = OldXMLHandler.parseCoaches(doc, date, coachOffice) | res;
            return res;
        }
    }
    /* ######################################################### */
    private File getXMLAsFile(String xmlName, Date saveDate) throws IOException, MalformedURLException {
        BufferedInputStream input = new BufferedInputStream(getXMLInputStream(xmlName));
        if (input==null) return null;
        BufferedOutputStream bos = null;
        String fileName = so.So.getBaseDirName() + so.So.getDataDirName() + File.separator + DIRNAME_XML + File.separator + SODF.format(saveDate) + '_' + xmlName;
        try {
            bos = new BufferedOutputStream(new FileOutputStream( fileName ));
        } catch (Exception exc) {
            new DebugFrame(exc);
            pdialog.endProgress();
            input.close();
            return null;
        }
        int c;
        while (-1 != ((c = input.read() )) ) {
            bos.write(c);
        }
        bos.flush();
        bos.close();
        input.close();
        return new File(fileName);
    }

    private String getXMLAsString(String xmlName) throws IOException, MalformedURLException {
        BufferedInputStream input = new BufferedInputStream(getXMLInputStream(xmlName));
        if (input==null) return null;
        BufferedReader br = new BufferedReader(new InputStreamReader(input, CHARSET));
        int c;
        StringBuilder sb = new StringBuilder(1024);
        while (-1 != (c = br.read())) {
            if (c>9) sb.append((char)c);
        }
        br.close();
        return sb.toString();
    }

    private InputStream getXMLInputStream(String xmlName) throws IOException, MalformedURLException {
        return getXMLInputStream(xmlName, 0);
    }

    private InputStream getXMLInputStream(String xmlName, int retryCounter) throws IOException, MalformedURLException {
        boolean isSokkerXML = !xmlName.startsWith("http://");
        if (isSokkerXML) {
            if (xmlDownloadCounter==10) xmlDownloadCounter = 0;
            else pause(xmlDownloadCounter*53 + 72);
            xmlDownloadCounter++;
        }
        /* URL of CGI-Bin script. */
        URL url = new URL( (isSokkerXML ? URL_HOSTNAME+"xml/" : "") + xmlName);
        URLConnection urlConn = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection)urlConn;
        urlConn.setDoInput (true);
        urlConn.setUseCaches (false);
        /* set cookies */
        cookieManager.setCookies(urlConn);
        urlConn.connect();
        /* Get response data. */
        try {
            return urlConn.getInputStream();
        } catch (IOException ioe) {
            if (isSokkerXML && retryCounter<2) {
                switch (httpConn.getResponseCode()) {
                case HttpURLConnection.HTTP_FORBIDDEN:
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    if (so.So.DEBUG) System.err.println(httpConn.getResponseMessage() + ": " + xmlName);
                    sinkInputStream(httpConn.getErrorStream());
                    pause(2950 + 550*retryCounter);
                    return getXMLInputStream(xmlName, retryCounter+1);
                default:
                    sinkInputStream(httpConn.getErrorStream());
                    throw ioe;
                }
            }
            else throw ioe;
        }
    }

    /* returns null if an error ocurs */
    private Document getDocFromUrl(String xmlName) throws IOException, MalformedURLException {
        String xmlString = getXMLAsString(xmlName);
        StringReader input = new StringReader(xmlString);
        /* Convert XML into Java Document */
        DocumentBuilder db = getDocumentBuilder();
        if (db == null) return null;
        Document doc = null;
        try {
            InputSource is = new InputSource(input);
            doc = db.parse( is );
            input.close();
        } catch (org.xml.sax.SAXException saxe) {
            System.err.println("error en:"+xmlName);
            showErrorMessage(saxe);
            input.close();
            return null;
        } catch (IOException ioe) {
            showErrorMessage(ioe);
            input.close();
            return null;
        } catch (Exception e2) {
            new DebugFrame(e2);
            return null;
        }
        return doc;
    }

    /* returns null if an error ocurs */
    private Document getDocFromFile(File file) {
        if (file == null) return null;
        /* Convert XML into Java Document */
        DocumentBuilder db = getDocumentBuilder();
        if (db == null) return null;
        Document doc = null;
        try {
            doc = db.parse( file );
        } catch (org.xml.sax.SAXException saxe) {
            showErrorMessage(saxe);
            return null;
        } catch (IOException ioe) {
            showErrorMessage(ioe);
            return null;
        } catch (Exception e2) {
            new DebugFrame(e2);
            return null;
        }
        return doc;
    }


    /* ######################################################### */
    protected int parseTeam(Document doc, Date date) {
        if (doc == null) return PARSE_FAILED;
        int ret = PARSE_FAILED;
        Node team = doc.getElementsByTagName("team").item(0);
        if (team==null) return PARSE_FAILED;
        Element eteam = (Element)team;
        int id = Integer.parseInt( getTagValue(eteam, "teamID") );
        String name = getTagValue(eteam, "name");
        int countryId = Integer.parseInt( getTagValue(eteam, "countryID") );
        int regionId = Integer.parseInt( getTagValue(eteam, "regionID") );
        Date creatDate = null;
        try {
            creatDate = XDF.parse( getTagValue(eteam, "dateCreated") );
        } catch (java.text.ParseException pe) { return PARSE_FAILED; }
        long money = Long.parseLong( getTagValue(eteam, "money") );
        int fans = Integer.parseInt( getTagValue(eteam, "fanclubCount") );
        int fanClubMood = Integer.parseInt( getTagValue(eteam, "fanclubMood") );
        int k1s = Integer.parseInt( getTagValue(eteam, "colShirt") );
        int k1p = Integer.parseInt( getTagValue(eteam, "colTraus") );
        int k2s = Integer.parseInt( getTagValue(eteam, "colShirt2") );
        int k2p = Integer.parseInt( getTagValue(eteam, "colTraus2") );
        int kks = Integer.parseInt( getTagValue(eteam, "colShirtKeep") );
        int kkp = Integer.parseInt( getTagValue(eteam, "colTrausKeep") );
        //short ? = Short.parseShort( getTagValue(eteam, "juniorsMax") );
        //String ? = getTagValue(eteam, "arenaName");
        int trt = Integer.parseInt( getTagValue(eteam, "trainingType") );
        int trf = Integer.parseInt( getTagValue(eteam, "trainingFormation") );
        float rank = Float.parseFloat( getTagValue(eteam, "rank") );
        Node unode = doc.getElementsByTagName("user").item(0);
        if (unode==null) return PARSE_FAILED;
        int uid = Integer.parseInt( getTagValue((Element)unode, "userID") );
        if (teamDetails.updateTeamDetails(date, id, countryId, name, regionId,uid,creatDate, k1s,k1p, k2s,k2p, kks,kkp)) {
            teamDetails.addTeamData(date, money, fans, fanClubMood, rank);
            ret = PARSE_TEAM | ret;
        }
        if (coachOffice.updateTraining(trt, trf, date)) ret = PARSE_TRAINING | ret;
        return ret;
    }

//     protected int parseStadium(Document doc, Date date) {
//         Node arena = doc.getElementsByTagName("arena").item(0);
//         if (arena == null) return PARSE_FAILED;
//         if (arena.getNodeType() != Node.ELEMENT_NODE) return PARSE_FAILED;
//         Element e_arena = (Element)arena;
//         String name = getTagValue(e_arena, "name");
//         //parsedStadium = new Stadium(name);
//         Stadium.StadiumData stadiumData = new Stadium.StadiumData(date);
//         NodeList standNodes = e_arena.getElementsByTagName("stand");
//         for (int i=0, N=standNodes.getLength(); i<N; i++) {
//             Node snode = standNodes.item(i);
//             String location = snode.getAttributes().getNamedItem("location").getNodeValue();
//             if (snode.getNodeType() == Node.ELEMENT_NODE) {
//                 Element selement = (Element)snode;
//                 int capacity = Integer.parseInt( getTagValue(selement, "capacity") );
//                 short type = Short.parseShort( getTagValue(selement, "type") );
//                 short _sroof = Short.parseShort( getTagValue(selement, "roof") );
//                 boolean roof = (_sroof == 1);
//                 float days = Float.parseFloat( getTagValue(selement, "days") );
//                 stadiumData.addStand(location, capacity, type, days, roof);
//             }
//         }
//         //parsedStadium.addStadiumData(date, stadiumData);
//         return PARSE_STADIUM;
//     }
    protected int parsePlayers(Document doc, Date date) {
        if (doc == null) return PARSE_FAILED;
        //int tid = Integer.parseInt( doc.getElementsByTagName("players").item(0).getAttributes().getNamedItem("teamID").getNodeValue() );
        HashSet<PlayerProfile> parsedPlayers = new HashSet<PlayerProfile>();
        NodeList players = doc.getElementsByTagName("player");
        for (int i=0, N=players.getLength(); i<N; i++) {
            Node pnode = players.item(i);
            if (pnode.getNodeType() == Node.ELEMENT_NODE) {
                Element pelement = (Element)pnode;
                int id = Integer.parseInt( getTagValue(pelement, "ID") );
                String name    = getTagValue(pelement, "name");
                String surname = getTagValue(pelement, "surname");
                short country  = Short.parseShort( getTagValue(pelement, "countryID") );
                short ag = Short.parseShort( getTagValue(pelement, "age") );
                int ht   = getTagValueAsInt(pelement, "height", 0); // will return 0 if the tag is not found; tag implemented on 4-2-2010
                int tid  = Integer.parseInt( getTagValue(pelement, "teamID") );
                int jtid = Integer.parseInt( getTagValue(pelement, "youthTeamID") );
                int vl   = Integer.parseInt( getTagValue(pelement, "value") );
                int sy   = Integer.parseInt( getTagValue(pelement, "wage") );
                short cd = Short.parseShort( getTagValue(pelement, "cards") );
                short go = Short.parseShort( getTagValue(pelement, "goals") );
                short as = Short.parseShort( getTagValue(pelement, "assists") );
                short ms = Short.parseShort( getTagValue(pelement, "matches") );
                short ntCd = Short.parseShort( getTagValue(pelement, "ntCards") );
                short ntGo = Short.parseShort( getTagValue(pelement, "ntGoals") );
                short ntAs = Short.parseShort( getTagValue(pelement, "ntAssists") );
                short ntMs = Short.parseShort( getTagValue(pelement, "ntMatches") );
                short in = Short.parseShort ( getTagValue(pelement, "injuryDays") );
                boolean ntp = (Integer.parseInt( getTagValue(pelement, "national") ) == 1);
                boolean ntpu21 = (Integer.parseInt( getTagValue(pelement, "national") ) == 2);
                boolean tl = (Integer.parseInt( getTagValue(pelement, "transferList") ) == 1);
                short fm = Short.parseShort( getTagValue(pelement, "skillForm") );
                short exp = Short.parseShort( getTagValue(pelement, "skillExperience") );
                short tw  = Short.parseShort( getTagValue(pelement, "skillTeamwork") );
                short dis = Short.parseShort( getTagValue(pelement, "skillDiscipline") );
                short st = Short.parseShort( getTagValue(pelement, "skillStamina") );
                short pc = Short.parseShort( getTagValue(pelement, "skillPace") );
                short te = Short.parseShort( getTagValue(pelement, "skillTechnique") );
                short ps = Short.parseShort( getTagValue(pelement, "skillPassing") );
                short kp = Short.parseShort( getTagValue(pelement, "skillKeeper") );
                short df = Short.parseShort( getTagValue(pelement, "skillDefending") );
                short pm = Short.parseShort( getTagValue(pelement, "skillPlaymaking") );
                short sc = Short.parseShort( getTagValue(pelement, "skillScoring") );
                PlayerProfile player = new PlayerProfile(id, name, surname, country, tid, jtid);
                player.addPlayerData(date, vl, sy, ag, ht, ms, go, as, fm, st, pc, te, ps, kp, df, pm, sc, cd, in, dis, exp, tw, tl, ntp, ntpu21, ntMs, ntGo, ntAs, ntCd);
                parsedPlayers.add(player);
            }
        } //end for
        playerRoster.updatePlayers( parsedPlayers , date );
        return PARSE_PLAYERS;
    }

    protected int parseJuniors(Document doc, Date date) {
        if (doc == null) return PARSE_FAILED;
        HashSet<JuniorProfile> parsedJuniors = new HashSet<JuniorProfile>();
        NodeList juniors = doc.getElementsByTagName("junior");
        for (int i=0, N=juniors.getLength(); i<N; i++) {
            Node jnode = null;
            jnode = juniors.item(i);
            if (jnode.getNodeType() == Node.ELEMENT_NODE) {
                Element jelement = (Element)jnode;
                int id = getTagValueAsInt(jelement, "ID");
                if (id == -1) continue;
                String name    = getTagValue(jelement, "name");
                String surname = getTagValue(jelement, "surname");
                int formation = getTagValueAsInt(jelement, "formation", JR_NO_DATA);
                short weeks = Short.parseShort( getTagValue(jelement, "weeks") );
                short skill = Short.parseShort( getTagValue(jelement, "skill") );
                short age = (short)getTagValueAsInt(jelement, "age", JR_NO_DATA);
                JuniorProfile junior = new JuniorProfile(id, name, surname, formation);
                junior.addJuniorData(date, weeks, skill, age);
                parsedJuniors.add(junior);
            }
        } //end for
        juniorSchool.updateJuniors( parsedJuniors, date );
        return PARSE_JUNIORS;
    }

    protected int parseCoaches(Document doc, Date date) {
        if (doc == null) return PARSE_FAILED;
        HashSet<CoachOffice.CoachProfile> parsedCoaches = new HashSet<CoachOffice.CoachProfile>();
        NodeList coaches = doc.getElementsByTagName("trainer");
        for (int i=0, N=coaches.getLength(); i<N; i++) {
            Node cnode = coaches.item(i);
            if (cnode.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    Element celement = (Element)cnode;
                    int id = Integer.parseInt( getTagValue(celement, "ID") );
                    String name    = getTagValue(celement, "name");
                    String surname = getTagValue(celement, "surname");
                    int job = Integer.parseInt( getTagValue(celement, "job") );
                    short country  = Short.parseShort( getTagValue(celement, "countryID") );
                    short age = Short.parseShort( getTagValue(celement, "age") );
                    int salary = Integer.parseInt( getTagValue(celement, "wage") );
                    short generalskill = Short.parseShort( getTagValue(celement, "skillCoach") );
                    short stamina    = Short.parseShort( getTagValue(celement, "skillStamina") );
                    short pace       = Short.parseShort( getTagValue(celement, "skillPace") );
                    short technique  = Short.parseShort( getTagValue(celement, "skillTechnique") );
                    short passing    = Short.parseShort( getTagValue(celement, "skillPassing") );
                    short keepers    = Short.parseShort( getTagValue(celement, "skillKeeper") );
                    short defenders  = Short.parseShort( getTagValue(celement, "skillDefending") );
                    short playmakers = Short.parseShort( getTagValue(celement, "skillPlaymaking") );
                    short scorers    = Short.parseShort( getTagValue(celement, "skillScoring") );
                    parsedCoaches.add( new CoachOffice.CoachProfile(id, name, surname, job, country, age, salary, generalskill, stamina, pace, technique, passing, keepers, defenders, playmakers, scorers) );
                } catch (Exception e) {
                    if (so.So.DEBUG) e.printStackTrace();
                    continue;
                }
            }
        } //end for
        coachOffice.updateCoaches(parsedCoaches, date);
        return PARSE_TRAINING;
    }

    protected int parseLeague(Document doc, Date date) {
        if (doc == null) return PARSE_FAILED;
        ArrayList<Integer> foundLeagues = new ArrayList<Integer>();
        NodeList leagues = doc.getElementsByTagName("leagueID");
        for (int i=0, N=leagues.getLength(); i<N; i++) {
            //if (so.So.DEBUG) System.out.println("matchlist: check League # " + i); //!
            int lid = Integer.parseInt( leagues.item(i).getFirstChild().getNodeValue() );
            if (lid==3 || lid==200) continue;
            if (!foundLeagues.contains(lid)) foundLeagues.add(lid);
        }
        SokkerCalendar skcal = new SokkerCalendar(date);
        boolean todayIsEndOfWeek = (skcal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY || skcal.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY);
        int _week = skcal.getWeekOfSeason();
        int season = skcal.getSeason();
        int ret = 0;
        for (int leagueId : foundLeagues) {
            doc = null;
            Element info = null;
            MatchRepository.LeagueData _ld = matchRepo.getLeague(leagueId);
            if (_ld==null) {
                try {
                    if (so.So.DEBUG) System.out.println("download league id: " + leagueId);
                    doc = getDocFromUrl("league-" + leagueId + ".xml");
                } catch (IOException ioe) {
                    continue;
                }
                /* make sure it is a standard league */
                info = (Element)doc.getElementsByTagName("info").item(0);
                if (info==null) continue;
                int _type = getTagValueAsInt(info, "type");
                if (_type==-1) continue;
                int _off = Integer.parseInt( getTagValue(info, "isOfficial") );
                int _cup = Integer.parseInt( getTagValue(info, "isCup") );
                //int _uid = Integer.parseInt( getTagValue(info, "userID") );
                String _lname = getTagValue(info, "name");
                _ld = matchRepo.addLeague(leagueId, _lname, _type, _off, _cup);
            }
            if ( _ld.getType() != MATCH_LEAGUE ) continue;
            if (doc==null) {
                try {
                    //if (so.So.DEBUG) System.out.println("dl " + leagueId);
                    doc = getDocFromUrl("league-" + leagueId + ".xml");
                } catch (IOException ioe) {
                    continue;
                }
                info = (Element)doc.getElementsByTagName("info").item(0);
                if (info==null) continue;
            }
            int cid = Integer.parseInt( getTagValue(info, "countryID") );
            int round = Integer.parseInt( getTagValue(info, "round") );
            String lname = getTagValue(info, "name");
            /* get the stored league, or create it if there's none */
            LeagueDetails ld = leagueEncapsulator.getLeague(season, leagueId);
            boolean updateFullLeague = false;
            if (ld==null) {
                ld = new LeagueDetails(leagueId, cid, lname, season);
                updateFullLeague = true;
            }
            /* don't bother if we already have this data */
            //if (round<=ld.getRound()) continue;
            if (round<ld.getRound() || (round==ld.getRound() && !todayIsEndOfWeek && round>0) ||
                (round==0 && !updateFullLeague) || _week==16) continue;
            /* check the teams */
            NodeList _teams = doc.getElementsByTagName("team");
            if (_teams.getLength()!=8) continue;
            boolean isMyTeamPresent = false;
            NodeList _tids = doc.getElementsByTagName("teamID");
            for (int i=0, N=_teams.getLength(); i<N; i++) {
                int tid = Integer.parseInt( _tids.item(i).getFirstChild().getNodeValue() );
                if (tid==teamDetails.getId()) isMyTeamPresent = true;
            }
            /* failsafe for if the tables have been erased in SK server */
            int _r = getTagValueAsInt((Element)_teams.item(0), "round");
            if (_r != round) continue;
            /* skip if this is not my league */
            if (!isMyTeamPresent) continue;
            /* get the teams data */
            pdialog.setBarString(lm.getLabel(TXT_DL_LEAGUE) + " : " + lm.getLabel(TXT_DL_TEAM));
            //pdialog.setBarString(lname + " : " + lm.getLabel(TXT_DL_TEAM));
            for (int i=0, N=_teams.getLength(); i<N; i++) {
                Node tnode = _teams.item(i);
                if (tnode.getNodeType() == Node.ELEMENT_NODE) {
                    Element telement = (Element)tnode;
                    int tid = Integer.parseInt( getTagValue(telement, "teamID") );
                    String _rt = getTagValue(telement, "rankTotal");
                    int lastSeasonPos = 9 - Integer.parseInt( _rt.substring( (_rt.length()>3?_rt.length()-3:0) ) );
                    String tname = "";
                    if (tid==teamDetails.getId()) {
                        isMyTeamPresent = true;
                        tname = teamDetails.getName();
                    }
                    else {
                        MatchRepository.TeamProfile tp = parseTeamProfileFromXml(tid);
                        if (tp == null) {
                            isMyTeamPresent = false;
                            break;
                        }
                        tname = tp.getName();
                    }
                    ld.addTeam(tid, tname, lastSeasonPos);
                }
            }
            /* check if an error ocurred, reused variable isMyTeamPresent */
            if (!isMyTeamPresent) continue;
            /* parse the rounds */
            pdialog.setBarString(lname + " : " + lm.getLabel(TXT_DL_MATCHES));
            try {
                if (updateFullLeague) {
                    int _auxr = ld.getRound();
                    pdialog.extendMaximumBy(14 - _auxr);
                    for (int r=_auxr+1; r<=round; r++) parseRoundMatchesFromXML(r, ld);
                    if (_auxr==0) { /* get the unplayed rounds */
                        for (int r=round+1; r<=14; r++) parseRoundMatchesFromXML(r, ld);
                    }
                }
                else {
                    pdialog.extendMaximumBy(round - ld.getRound());
                    for (int r=ld.getRound()+1; r<=round; r++) parseRoundMatchesFromXML(r, ld);
                    if (todayIsEndOfWeek && round<14) {
                        pdialog.extendMaximumBy(1);
                        if (parseRoundMatchesFromXML(round+1, ld)) round++;
                    }
                }
            } catch (IOException ioe) {
                if (so.So.DEBUG) ioe.printStackTrace();
                continue;
            } // end TRY
            ld.setRound(round);
            leagueEncapsulator.updateLeagueDetails(ld);
            ret = PARSE_LEAGUE;
            break;
        } //end for LEAGUEID
        return ret;
    }
    private boolean parseRoundMatchesFromXML(int round, LeagueDetails ld) throws IOException {
        Document doc;
        boolean roundIsFinished = true;
        int mid, ltid, vtid, lgoals, vgoals, isFinished;
        pdialog.setBarString(ld.getName() + " : " + round);
        doc = getDocFromUrl("matches-league-" + ld.getId() + '-' + round + ".xml");
        NodeList matches = doc.getElementsByTagName("match");
        for (int i=0, N=matches.getLength(); i<N; i++) {
            Node mnode = matches.item(i);
            if (mnode.getNodeType() == Node.ELEMENT_NODE) {
                Element melement = (Element)mnode;
                mid = Integer.parseInt( getTagValue(melement, "matchID") );
                ltid = Integer.parseInt( getTagValue(melement, "homeTeamID") );
                vtid = Integer.parseInt( getTagValue(melement, "awayTeamID") );
                isFinished = Integer.parseInt( getTagValue(melement, "isFinished") );
                if (isFinished==1) {
                    lgoals = Integer.parseInt( getTagValue(melement, "homeTeamScore") );
                    vgoals = Integer.parseInt( getTagValue(melement, "awayTeamScore") );
                }
                else {
                    lgoals = 0;
                    vgoals = 0;
                    roundIsFinished = false;
                }
                ld.addMatch(round, mid, ltid, vtid, lgoals, vgoals, i+1, (isFinished==1));
            }
        }
        return roundIsFinished;
    }
    private MatchRepository.TeamProfile parseTeamProfileFromXml(int teamId) {
        Document doc;
        try {
            doc = getDocFromUrl("team-" + teamId + ".xml");
        } catch (IOException ioe) {
            return null;
        }
        //pdialog.advanceProgress();
        Element team = (Element)doc.getElementsByTagName("team").item(0);
        if (team==null) return null;
        int tid = Integer.parseInt( getTagValue(team, "teamID") );
        if (tid != teamId) return null;
        String name = getTagValue(team, "name");
        int cid = Integer.parseInt( getTagValue(team, "countryID") );
        float rank = Float.parseFloat( getTagValue(team, "rank") );
        return matchRepo.addTeam(tid, name, cid, rank);
    }

    protected int parseMatches(Document doc, Date date) {
        if (doc == null) return PARSE_FAILED;
        ArrayList<Integer> matchIds = new ArrayList<Integer>();
        NodeList matches = doc.getElementsByTagName("match");
        for (int i=0, N=matches.getLength(); i<N; i++) {
            Node mnode = matches.item(i);
            if (mnode.getNodeType() == Node.ELEMENT_NODE) {
                Element melement = (Element)mnode;
                int isFinished = Integer.parseInt( getTagValue(melement, "isFinished") );
                if (isFinished!=1) continue;
                int mid = Integer.parseInt( getTagValue(melement, "matchID") );
                matchIds.add(mid);
            }
        }
        if (matchIds.isEmpty()) return PARSE_FAILED;
        pdialog.extendMaximumBy(matchIds.size());
        return parseMatchesFromList(matchIds);
    }

    protected int parseMatchesFromList(List<Integer> matchIdsList) {
        if (matchIdsList.isEmpty()) return 0;
        Document doc;
        Date mdate = null;
        int ret = 0;
        int myTid = teamDetails.getId();
        boolean imLocal = false;
        boolean notMyMatch = false;
        MatchRepository.TeamProfile ltp, vtp;
        matchRepo.addTeam(myTid, teamDetails.getName(), teamDetails.getCountryId(), teamDetails.getRank());
        for (int mid : matchIdsList) {
            /* if it already in the repository (and COMPLETE), don't download */
            if (matchRepo.hasCompleteMatch(mid)) {
                pdialog.advanceProgress();
                continue;
            }
            try {
                doc = getDocFromUrl("match-" + mid + ".xml");
            } catch (IOException ioe) {
                if (so.So.DEBUG) ioe.printStackTrace();
                pdialog.advanceProgress();
                continue;
            }
            try {
                Element info = (Element)doc.getElementsByTagName("info").item(0);
                if (info==null) continue;
                int isFinished = Integer.parseInt( getTagValue(info, "isFinished") );
                if (isFinished!=1) continue;
                try {
                    mdate = XDF.parse( getTagValue(info, "dateStarted") );
                    if (teamDetails.getCreationDate()!=null &&
                        mdate.getTime()<teamDetails.getCreationDate().getTime()) continue;
                } catch (java.text.ParseException pe) { continue; }
                pdialog.setBarString(lm.getExtendedLabel(TXT_DL_MATCHID, Integer.toString(mid)));
                /* begin Match parsing */
                int ltid = Integer.parseInt( getTagValue(info, "homeTeamID") );
                int vtid = Integer.parseInt( getTagValue(info, "awayTeamID") );
                notMyMatch = false;
                if (myTid == ltid) imLocal = true;
                else if (myTid == vtid) imLocal = false;
                else notMyMatch = true;

                if (imLocal || notMyMatch) if (parseOtherTeamIntoRepo(vtid) == PARSE_FAILED) continue;
                if (!imLocal || notMyMatch) if (parseOtherTeamIntoRepo(ltid) == PARSE_FAILED) continue;

                int lid = Integer.parseInt( getTagValue(info, "leagueID") );
                int mtype = getMatchTypeFromLeagueId(lid);
                /* fix for NT friendlies (Towarzyski league) */
                if (mtype==MATCH_FRIENDLY_NORMAL && ltid<250 && vtid<250) {
                    mtype = MATCH_NT_FRIENDLY;
                    lid = 13;
                }
                if (mtype == PARSE_FAILED) continue; // error;
                if (notMyMatch && (mtype&MT_NT)==0) continue; // match is not mine nor a NT match
                int weather = Integer.parseInt( getTagValue(info, "weather") );
                int spect = Integer.parseInt( getTagValue(info, "supporters") );
                int lg = Integer.parseInt( getTagValue(info, "homeTeamScore") );
                int vg = Integer.parseInt( getTagValue(info, "awayTeamScore") );
                String lname = getTagValue(info, "homeTeamName");
                String vname = getTagValue(info, "awayTeamName");
                //String lname = matchRepo.getTeam(ltid).getName();
                //String vname = matchRepo.getTeam(vtid).getName();
                /* add parsed MatchData to repository */
                matchRepo.addMatch(mid, mdate, mtype, lid, weather, spect, ltid, vtid, lname, vname, new Score(lg, vg));
                /* parse TeamStats */
                NodeList teams = doc.getElementsByTagName("teamStats");
                for (int i=0, N=teams.getLength(); i<N; i++) {
                    try {
                        Element tselement = (Element)teams.item(i);
                        int _tid = Integer.parseInt( getTagValue(tselement, "teamID") );
                        String _tacn = getTagValue(tselement, "tacticName");
                        float _poss = Integer.parseInt( getTagValue(tselement, "timePossession") ) * 100 / 63396f;
                        float _pih = Integer.parseInt( getTagValue(tselement, "timeOnHalf") ) * 100 / 63396f;
                        int _shots = Integer.parseInt( getTagValue(tselement, "shoots") );
                        int _fouls = Integer.parseInt( getTagValue(tselement, "fouls") );
                        int _offs = Integer.parseInt( getTagValue(tselement, "offsides") );
                        int _yc = Integer.parseInt( getTagValue(tselement, "yellowCards") );
                        int _rc = Integer.parseInt( getTagValue(tselement, "redCards") );
                        int _rats = Integer.parseInt( getTagValue(tselement, "ratingScoring") );
                        int _ratp = Integer.parseInt( getTagValue(tselement, "ratingPassing") );
                        int _ratd = Integer.parseInt( getTagValue(tselement, "ratingDefending") );
                        matchRepo.addTeamStats(mid,_tid,_tacn,_poss,_pih,_shots,_fouls,_offs, _yc,_rc,_rats,_ratp,_ratd);
                    } catch (Exception e) {
                        if (so.So.DEBUG) e.printStackTrace();
                        continue;
                    }
                }
                /* parse PlayerStats */
                NodeList teamPlayersStats = doc.getElementsByTagName("playersStats");
                for (int i=0, N=teamPlayersStats.getLength(); i<N; i++) {
                    Node tpsnode = teamPlayersStats.item(i);
                    if (tpsnode.getNodeType() != Node.ELEMENT_NODE) continue;
                    int _tid = Integer.parseInt( tpsnode.getAttributes().getNamedItem("teamID").getNodeValue() );
                    if (_tid < 1) continue;
                    Element tpselement = (Element)tpsnode;
                    NodeList playerStats = tpselement.getElementsByTagName("playerStats");
                    for (int j=0, M=playerStats.getLength(); j<M; j++) {
                        Node psnode = playerStats.item(j);
                        if (psnode.getNodeType() != Node.ELEMENT_NODE) continue;
                        Element pselement = (Element)psnode;
                        try {
                            int pid = Integer.parseInt( getTagValue(pselement, "playerID") );
                            int pos = Integer.parseInt( getTagValue(pselement, "number") );
                            int order = Integer.parseInt( getTagValue(pselement, "formation") );
                            int in = Integer.parseInt( getTagValue(pselement, "timeIn") );
                            int out = Integer.parseInt( getTagValue(pselement, "timeOut") );
                            int yc = Integer.parseInt( getTagValue(pselement, "yellowCards") );
                            int rc = Integer.parseInt( getTagValue(pselement, "redCards") );
                            int inj = Integer.parseInt( getTagValue(pselement, "isInjured") );
                            int go = Integer.parseInt( getTagValue(pselement, "goals") );
                            int as = Integer.parseInt( getTagValue(pselement, "assists") );
                            int fo = Integer.parseInt( getTagValue(pselement, "fouls") );
                            int sh = Integer.parseInt( getTagValue(pselement, "shoots") );
                            int rat = Integer.parseInt( getTagValue(pselement, "rating") );
                            int tplay = Integer.parseInt( getTagValue(pselement, "timePlaying") );
                            int tdef = Integer.parseInt( getTagValue(pselement, "timeDefending") );
                            matchRepo.addPlayerStats(pid, mid, _tid, pos, order, rat, in, out, sh, fo, as, go,
                                                     yc, rc, tplay, tdef, (inj==1));
                        } catch (Exception e) {
                            if (so.So.DEBUG) e.printStackTrace();
                            continue;
                        }
                    }
                }
                ret = PARSE_MATCH;
            } catch (Exception e) {
                System.err.println("ERROR downloading MatchID=" + mid);
                e.printStackTrace();
                System.err.println("CONTINUING WITH NEXT MATCH");
            }
            matchRepo.markMatchAsComplete(mid);
        } // end for (int mid : matchIdsList)
        return ret;
    }

    private int parseOtherTeamIntoRepo(int teamId) {
        try {
            Document doc = getDocFromUrl("team-" + teamId + ".xml");
            if (doc == null) return PARSE_FAILED;
            Node team = doc.getElementsByTagName("team").item(0);
            Element eteam = (Element)team;
            String name = getTagValue(eteam, "name");
            int countryId = Integer.parseInt( getTagValue(eteam, "countryID") );
            float rank = Float.parseFloat( getTagValue(eteam, "rank") );
            matchRepo.addTeam(teamId, name, countryId, rank);
            return PARSE_TEAM;
        } catch (IOException ioe) {
        } catch (NumberFormatException nfe) { }
        return PARSE_FAILED;
    }
    private int getMatchTypeFromLeagueId(int lid) {
        if (lid == 3) return MATCH_FRIENDLY_NORMAL;
        if (lid == 13) return MATCH_NT_FRIENDLY;
        if (lid == 200) return MATCH_FRIENDLY_CUPRULES;
        MatchRepository.LeagueData ld = matchRepo.getLeague(lid);
        if (ld != null) return ld.getType();
        try {
            Document doc = getDocFromUrl("league-" + lid + ".xml");
            if (doc == null) return PARSE_FAILED;
            Element info = (Element)doc.getElementsByTagName("info").item(0);
            if (info == null) return MATCH_FRIENDLY_LEAGUE; // deleted friendly league
            String name = getTagValue(info, "name");
            int _type = Integer.parseInt( getTagValue(info, "type") );
            int _off = Integer.parseInt( getTagValue(info, "isOfficial") );
            int _cup = Integer.parseInt( getTagValue(info, "isCup") );
            ld = matchRepo.addLeague(lid, name, _type, _off, _cup);
            return ld.getType();
        } catch (IOException ioe) {
        } catch (NumberFormatException nfe) { }
        return PARSE_FAILED; //was MATCH_FRIENDLY_LEAGUE;
    }

    private String getTagValue(Element ele, String tag) {
        try {
            return ele.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }
    private int getTagValueAsInt(Element ele, String tag) {
        return getTagValueAsInt(ele, tag, -1);
    }
    private int getTagValueAsInt(Element ele, String tag, int defval) {
        try {
            return Integer.parseInt( getTagValue(ele, tag) );
        } catch (NullPointerException npe) {
        } catch (NumberFormatException nfe) { }
        return defval;
    }
    private String getAttributeValue(Element ele, String tag, String attr) {
        return ele.getElementsByTagName(tag).item(0).getAttributes().getNamedItem(attr).getNodeValue();
    }

    /* ######################################################### */
    private DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            new DebugFrame(pce);
            return null;
        }
    }

    private void showErrorMessage(Throwable t) {
        if (so.So.DEBUG) t.printStackTrace();
        JOptionPane.showMessageDialog(frame, t.toString(), lm.getLabel(TXT_ERROR), JOptionPane.ERROR_MESSAGE);
    }

    private Date getDateFromFileName(String dateText, java.text.DateFormat dateFormat) {
        Date d = null;
        try {
            d = dateFormat.parse( dateText );
        } catch (java.text.ParseException pe) {
            //System.err.println("ParseException " + fn + " : " + f.getName());
            JDateChooser dateChooser = new JDateChooser(null, null, null, null);
            int dlg = JOptionPane.showConfirmDialog(frame, dateChooser, lm.getLabel(TXT_INPUT_DATE),
                                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (dlg == JOptionPane.OK_OPTION) {
                d = dateChooser.getDate();
            }
        }
        return d;
    }

    private Date getDateFromFileName(File file, java.text.DateFormat dateFormat) {
        return getDateFromFileName(file.getName(), dateFormat);
    }
    /* ######################################################### */
    public static boolean ensureAccesibleDir(String dirname) {
        try {
            File dir = new File(dirname);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception exc) {
            new DebugFrame(exc);
            return false;
        }
        return true;
    }

    private boolean pause() {
        return pause(2000);
    }

    private boolean pause(long delay) {
        try{
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            return false;
        }
        return true;
    }


    /* ###################################################################### */
    /* ###################################################################### */
    /* ###################################################################### */

//     private static class RSortPair implements Comparable<RSortPair> {
//         private Object key;
//         private float value;
//         private int extra;

//         public RSortPair(Object k, float v, int e) {
//             key = k;
//             value = v;
//             extra = e;
//         }
//         public RSortPair(Object k, float v) {
//             this(k, v, 0);
//         }

//         public String getKeyAsString() { return (String)key; }
//         public float getValue() { return value; }
//         public int getExtraInt() { return extra; }
//         public MatchRepository.TeamProfile getKeyAsMRTP() { return (MatchRepository.TeamProfile)key; }

//         public int hashCode() {
//             return (int)(value*100000) + key.hashCode();
//         }
//         public boolean equals(Object obj) {
//             if (obj==null) return false;
//             if ( !(obj instanceof RSortPair) ) return false;
//             RSortPair rsp = (RSortPair)obj;
//             return (this.value==rsp.value) && (this.key.equals(rsp.key));
//         }
//         public int compareTo(RSortPair o) {
//             int c = -(new Float(this.value).compareTo(new Float(o.value)));
//             if (c==0) c = (new Double(this.key.hashCode()).compareTo(new Double(o.key.hashCode())));
//             return c;
//         }

//     }

    /* ###################################################################### */
//     public int fetchStatistics() {
//         StatisticParser sp = new StatisticParser();
//         pdialog.startProgress(3 + 85*9); // ligas * (liga + 8 equipos)
//         sp.internalFetchStatistics(true);
//         pdialog.endProgress();
//         sp.printRanking();
//         return 1;
//     }

//     private class StatisticParser {
//         HashMap<Integer,Float> oldRanks;
//         //TreeMap<Float,MatchRepository.TeamProfile> ranking;
//         TreeSet<RSortPair> ranking;
//         HashMap<Integer,String> teamLeagues; // tid, leagueName
//         TreeSet<RSortPair> leaguesRanking;

//         StatisticParser() {
//             oldRanks = new HashMap<Integer, Float>();   
//             //ranking = new TreeMap<Float,MatchRepository.TeamProfile>();
//             ranking = new TreeSet<RSortPair>();
//             teamLeagues = new HashMap<Integer,String>();
//             leaguesRanking = new TreeSet<RSortPair>();
//         }

//         public int internalFetchStatistics(boolean getRanking) {
//             Document doc;
//             if (options.getUseProxy()) {
//                 System.getProperties().put("http.proxyHost", options.getProxyHostname());
//                 System.getProperties().put("http.proxyPort", options.getProxyPort());
//             }
//             else {
//                 System.getProperties().remove("http.proxyHost");
//                 System.getProperties().remove("http.proxyPort");
//             }
//             int result = 0;
//             try {
//                 /* login and get session cookies from start.php?session=xml */
//                 int teamId = initSession();
//                 if (teamId<1) return PARSE_FAILED;
//                 Date now = new Date();
//                 if (getRanking) {
//                     //int [] ligas = { 3986, 3987, 3988, 3989, 3990 };
//                     //pdialog.setMaximum(85);
//                     for (int lid=3986; lid<=4006 ; lid++) {
//                         pdialog.setBarString(lm.getLabel(TXT_DL_LEAGUE) + " : " + lid);
//                         parseTeamsFromLeague(lid);
//                     }
//                     pause();
//                     for (int lid=5058; lid<=5089 ; lid++) {
//                         pdialog.setBarString(lm.getLabel(TXT_DL_LEAGUE) + " : " + lid);
//                         parseTeamsFromLeague(lid);
//                     }
//                     pause();
//                     for (int lid=7829; lid<=7860 ; lid++) {
//                         pdialog.setBarString(lm.getLabel(TXT_DL_LEAGUE) + " : " + lid);
//                         parseTeamsFromLeague(lid);
//                     }
//                 }
//                 parseDate = now;
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//                 showErrorMessage(ex);
//                 return PARSE_FAILED;
//             }
//             return result;
//         }

//         public void parseTeamsFromLeague(int leagueId) {
//             Document doc = null;
//             int tid = 0;
//             float leagueRank = 0f;
//             try {
//                 if (so.So.DEBUG) System.out.println("=== dl " + leagueId + " ===");
//                 doc = getDocFromUrl("league-" + leagueId + ".xml");
//             } catch (Exception e1) {
//                 System.err.println("leagueId: " + leagueId);
//                 e1.printStackTrace();
//                 return;
//             }
//             try{
//                 /* check the teams */
//                 Element info = (Element)doc.getElementsByTagName("info").item(0);
//                 String lname = getTagValue(info, "name");
//                 NodeList _tids = doc.getElementsByTagName("teamID");
//                 //System.out.println("# league " + leagueId + " : " + _tids.getLength());
//                 for (int i=0, N=_tids.getLength(); i<N; i++) {
//                     try {
//                         tid = Integer.parseInt( _tids.item(i).getFirstChild().getNodeValue() );
//                         System.out.print("  ¬ " + tid);
//                         /* get the teams data */
//                         MatchRepository.TeamProfile _tp = matchRepo.getTeam(tid);
//                         if (_tp==null) oldRanks.put(tid, 0f);
//                         else oldRanks.put(tid, _tp.getRank());
//                         pdialog.setBarString(lname + " : " + tid);
//                         MatchRepository.TeamProfile tp = parseTeamProfileFromXml(tid);
//                         if (tp==null) {
//                             System.err.println("tid: " + tid + " NULL");
//                             return;
//                         }
//                         float aux = 0f;
//                         //while (ranking.containsKey(tp.getRank()+aux)) aux += 0.0001f;
//                         //ranking.put(tp.getRank()+aux, tp);
//                         ranking.add(new RSortPair(tp, tp.getRank()));
//                         leagueRank += tp.getRank();
//                         teamLeagues.put(tid, lname);
//                     } catch (Exception ex) {
//                         System.err.println("inner ERROR tid: " + tid + "   lid="+leagueId);
//                         ex.printStackTrace();
//                     }
//                 }
//                 System.out.println(".");
//                 leaguesRanking.add(new RSortPair(lname, leagueRank/8, leagueId));
//             } catch (Exception ex) {
//                 System.err.println("tid: " + tid + "   lid="+leagueId);
//                 ex.printStackTrace();
//             }
//         }

//         public void printRanking() {
//           //ArrayList<MatchRepository.TeamProfile> teams = new ArrayList<MatchRepository.TeamProfile>(ranking.values());
//             //java.util.Collections.reverse(teams);
//             int pos = 1;
//             String lname;
//             float old, diff;
//             int tid;
//             System.out.println("--------------------------------------------------------------------");
//             //for (MatchRepository.TeamProfile tp : teams) {
//             MatchRepository.TeamProfile tp;
//             for (RSortPair rsp : ranking) {
//                 tp = rsp.getKeyAsMRTP();
//                 tid = tp.getId();
//                 lname = teamLeagues.get(tid);
//                 old = oldRanks.containsKey(tid) ? oldRanks.get(tid) : 0;
//                 diff = old>0 ? tp.getRank()-old : 0;
//                 System.out.print(pos + ".- [b][tid=" + tid + "]" + tp.getName() + "[/tid][/b] {[i]" + lname +
//                                  "[/i]} : [b]" + tp.getRank());
//                 if (old>0 && diff!=0) {
//                     System.out.println("[/b] (" + (diff>0?"[color=green]+":"[color=red]") + diff + "[/color])" );
//                 }
//                 else System.out.println("[/b]");
//                 pos++;
//             }
//             System.out.println("====================================================================");
//             pos = 1;
//             for (RSortPair rsp : leaguesRanking) {
//                 System.out.println(pos + ".- [b][url=league.php?leagueID=" + rsp.getExtraInt() + "]" +
//                                    rsp.getKeyAsString() + "[/url][/b] : [b]" + rsp.getValue() + "[/b]");
//                 pos++;
//             }
//         }

//     }

    /* ###################################################################### */
    /* ###################################################################### */

//     private void registerSO() {
//         teamDetails.addRegisterCount2();
//         if (teamDetails.getId()!=TEAMID_NO_TEAM && teamDetails.getCountryId()>0)
//             registerSO(teamDetails.getId(), teamDetails.getCountryId());
//     }

//     private void registerSO(int tid, int country) {
//         if (teamDetails.isRegistered2()) return;
//         String address;
//         boolean reg2 = false;
//         if (teamDetails.isRegistered()) {
//             if (teamDetails.getRegisterCount2()>=5) {
//                 address = "http://dannysokker.5gbfree.com/_so/so-key.php?tid=" + tid + "&country=" + country + "&regc=" + teamDetails.getRegisterCount2();
//                 reg2 = true;
//             }
//             else return;
//         }
//         else address = "http://alumnos.elo.utfsm.cl/~egonzalf/so-key.php?tid=" + tid + "&country=" + country;
//         try {
//             URL url = new URL(address);
//             URLConnection urlConn = url.openConnection();
//             urlConn.setDoInput (true);
//             urlConn.setUseCaches (false);
//             urlConn.connect();
//             /* Get response data. */
//             InputStream input = urlConn.getInputStream();
//             if (input==null) return;
//             BufferedReader br = new BufferedReader(new InputStreamReader(input, CHARSET));
//             String response = br.readLine();
//             while (null != br.readLine()) ;
//             br.close();
//             if (response!=null && response.startsWith("OK")) {
//                 if (reg2) teamDetails.setRegistered2(true);
//                 else teamDetails.setRegistered(true);
//             }
//             //if (so.So.DEBUG) System.out.println("Register RESPONSE: " + response);
//         } catch (Exception e) {
//             if (so.So.DEBUG) e.printStackTrace();
//         }
//     }

    public boolean checkSOVersion(boolean silent) {
        Document doc = null;
        try {
            //doc = getDocFromUrl("http://sokkerorganizer.googlepages.com/version.xml");
            doc = getDocFromUrl(so.So.getUpdateBaseURLName() + "version.xml");
        } catch (IOException ioe) {
            if (so.So.DEBUG) ioe.printStackTrace();
            return false;
        }
        if (doc == null) return false;
        Node soNode = doc.getElementsByTagName("so").item(0);
        if (soNode.getNodeType() != Node.ELEMENT_NODE) return false;
        Element element = (Element)soNode;
        String version = getTagValue(element, "version");
        double ver = Double.parseDouble(version);
        double langVer = 0.0;
        Node langNode = element.getElementsByTagName("languages").item(0);
        element = (Element)langNode;
        NodeList nl = element.getElementsByTagName(lm.getCurrentLanguageName());
        if (nl!=null && nl.getLength()>0 && lm.getCurrentLanguageVersion()>0.0) {
            try {
                langVer = Double.parseDouble(nl.item(0).getFirstChild().getNodeValue());
            } catch (NumberFormatException nfe) {
            } catch (NullPointerException npe) { }
        }

        boolean newVersion = ver>so.So.getVersion();
        boolean newLangUpdates = langVer>lm.getCurrentLanguageVersion();
        if (newVersion || newLangUpdates) {
            String zipurl = getTagValue((Element)soNode, "zipurl");
            JTextArea jta = new JTextArea("");
            NodeList notes = doc.getElementsByTagName("note");
            Node attr;
            String note;
            for (int i=0, N=notes.getLength(); i<N; i++) {
                attr = notes.item(i).getAttributes().getNamedItem("type");
                /* skip non type="lang" if lang update only */
                if (!newVersion && (attr==null || !attr.getNodeValue().equals("lang"))) continue;
                note = notes.item(i).getFirstChild().getNodeValue();
                jta.append(note + "\n");
            }
            jta.setEditable(false);
            String title;
            if (newVersion) title = lm.getExtendedLabel(TXT_NEW_VERSION, Double.toString(ver));
            else title = lm.getLabel(TXT_LANG_UPDATES);
            int dlg = JOptionPane.showOptionDialog(frame, jta, title, JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.PLAIN_MESSAGE, null,
                                                   new Object[]{ lm.getLabel(TXT_UPDATE) }, null);
            if (dlg == JOptionPane.OK_OPTION) return updateSO(doc, newVersion, newLangUpdates);
        }
        else if (!silent) {
            Dialog dlg = new Dialog("", false);
            dlg.getContentPane().add(new JLabel("No new updates found"), java.awt.BorderLayout.CENTER);
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setResizable(false);
            dlg.setVisible(true);
        }
        return false;
    }

    public boolean updateSO(Document doc, boolean newVersion, boolean newLangs) {
        int max = 1;
        NodeList nlRes   = doc.getElementsByTagName("file");
        NodeList nlUnjar = doc.getElementsByTagName("unjar");
        max += nlUnjar.getLength();
        if (newVersion) {
            max += nlRes.getLength();
        }
        pdialog.startProgress(max);
        File auxFile;
        String filename, savename;
        Node attr, attr2, attr3;
        if (newVersion) {
            /* new Resources */
            String targetLocation, fileType;
            for (int i=0; i<nlRes.getLength(); i++) {
                targetLocation = "resources";
                fileType = "jar";
                attr  = nlRes.item(i).getAttributes().getNamedItem("ver");
                attr2 = nlRes.item(i).getAttributes().getNamedItem("loc");
                attr3 = nlRes.item(i).getAttributes().getNamedItem("type");
                /* skip if version is current */
                if (attr!=null) {
                    try {
                        if ( so.So.getVersion() >= Double.parseDouble(attr.getNodeValue()) ) continue;
                    } catch (Exception e) { }
                }
                if (attr2!=null) {
                    try {
                        targetLocation = attr2.getNodeValue();
                    } catch (Exception e) { }
                }
                if (attr3!=null) {
                    try {
                        fileType = attr3.getNodeValue();
                    } catch (Exception e) { }
                }
                filename = nlRes.item(i).getFirstChild().getNodeValue();
                if (fileType.equals("jar")) savename = filename + ".new";
                else savename = filename;
                auxFile = new File(targetLocation, savename);
                if (auxFile.exists() && !auxFile.delete()) continue; // maybe return false?
                pdialog.setBarString(lm.getLabel(TXT_DL_UPDATING_SO) + " : " + filename);
                downloadFile(so.So.getUpdateBaseURLName() + filename, auxFile);
            }
            if (nlUnjar.getLength()>0) newLangs = true;
        }
        if (newLangs) {
            for (int i=0; i<nlUnjar.getLength(); i++) {
                filename = nlUnjar.item(i).getFirstChild().getNodeValue();
                auxFile = new File(filename);
                if (auxFile.exists() && !auxFile.delete()) continue;
                pdialog.setBarString(lm.getLabel(TXT_DL_UPDATING_SO) + " : " + filename);
                downloadFile(so.So.getUpdateBaseURLName() + filename, auxFile);
                //unzip
                if (auxFile.canRead()) unjarFile(auxFile);
                auxFile.delete();
            }
        }
        pdialog.endProgress();
        return true;
    }

    // ===========================================================================
    private static void sinkInputStream(InputStream is) {
        if (is == null) return;
        try {
            while (is.read() != -1) ;
            is.close();
        } catch (Exception ex) { }
    }

    private static void downloadFile(String address, File localFile) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream  in = null;
        try {
            URL url = new URL(address);
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            if (so.So.DEBUG) System.out.println(localFile.getName() + "\t" + numWritten);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioe) {
            }
        }
    }

    private static void unjarFile(File file) {
        InputStream  in = null;
        OutputStream out = null;
        try {
            JarFile jar = new JarFile(file);
            File f, parent;
            JarEntry je;
            for (java.util.Enumeration<JarEntry> enu = jar.entries(); enu.hasMoreElements(); ) {
                je = enu.nextElement();
                if (je.getName().startsWith("META-INF")) continue;
                f = new File(je.getName());
                parent = f.getParentFile();
                if (parent!=null && !parent.exists()) parent.mkdirs();
                if (f.exists() && !f.delete()) continue;
                try {
                    in = jar.getInputStream(je);
                    out = new BufferedOutputStream(new FileOutputStream(f));
                    byte[] buffer = new byte[1024];
                    int numRead;
                    while ((numRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, numRead);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                    } catch (IOException ioe) {
                    }
                }
            }
            jar.close();
        } catch (java.util.jar.JarException ze) { return;
        } catch (java.io.IOException ioe) { return; }
    }

}
