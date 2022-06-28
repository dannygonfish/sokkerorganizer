package so.data;

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.gui.MainFrame;
import so.util.SokkerCalendar;
import so.util.SokkerWeek;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;

public class MatchRepository extends AbstractData {
    private static final long serialVersionUID = 2574582514620882937L;

    private HashMap<Integer,MatchData> matches;
    private HashMap<Integer,PlayerProfile> players;
    private HashMap<Integer,TeamProfile> teams;
    private HashMap<Integer,LeagueData> leagues;
    private TreeSet<CountryDataUnit> visitedFlags;
    private TreeSet<CountryDataUnit> hostedFlags;

    public MatchRepository() {
        super(FILENAME_MATCHDATA);
        matches = new HashMap<Integer,MatchData>();
        players = new HashMap<Integer,PlayerProfile>();
        teams = new HashMap<Integer,TeamProfile>();
        leagues = new HashMap<Integer,LeagueData>();
        visitedFlags = new TreeSet<CountryDataUnit>();
        hostedFlags = new TreeSet<CountryDataUnit>();
    }

    public TreeSet<CountryDataUnit> getVisitedFlags() { return visitedFlags; }
    public TreeSet<CountryDataUnit> getHostedFlags() { return hostedFlags; }

    /* if team is present, update its name. Otherwise, create it */
    public TeamProfile addTeam(int tid, String tname, int cid, float rank) {
        TeamProfile tp = null;
        if (teams.containsKey(tid)) {
            tp = teams.get(tid);
            tp.teamName = tname;
            tp.rank = rank;
        }
        else {
            tp = new TeamProfile(tid, tname, cid, rank);
            teams.put(tid, tp);
        }
        return tp;
    }

    public TeamProfile getTeam(int tid) {
        if (!teams.containsKey(tid)) return null;
        return teams.get(tid);
    }

    public MatchData getMatch(int mid) {
        if (!matches.containsKey(mid)) return null;
        return matches.get(mid);
    }

    public boolean hasCompleteMatch(int matchId) {
        if (!matches.containsKey(matchId)) return false;
        else return matches.get(matchId).isDataComplete();
    }

    public void markMatchAsComplete(int matchId) {
        MatchData md = getMatch(matchId);
        if (md != null) md.complete = true;
    }

    public int getLatestMatchId(int tid) {
        if (!teams.containsKey(tid)) return 0;
        TeamProfile tp = teams.get(tid);
        int mid = tp.latestMatchId;
        if (mid==0) {
            Date dmax = null, d = null;
            for (TeamProfile.TeamStats ts : tp.stats.values()) {
                d = ts.getMatchDate();
                if (d==null) continue;
                if (dmax==null || dmax.compareTo(d)<0) {
                    dmax = d;
                    mid = ts.matchId;
                }
            }
            tp.latestMatchId = mid;
        }
        return mid;
    }

    public PlayerProfile getPlayer(int pid) {
        if (!players.containsKey(pid)) return null;
        return players.get(pid);
    }

    public List<PlayerProfile.PlayerStats> getPlayerStats(int pid) {
        if (!players.containsKey(pid)) return null;
        return players.get(pid).getPlayerStats();
    }

    public List<MatchData> getMatchesForTeam(int tid) {
        return getMatchesForTeam(tid, -1);
    }

    /* Matches of precisely type TYPE */
    public List<MatchData> getMatchesForTeam(int tid, int type) {
        ArrayList<MatchData> list = new ArrayList<MatchData>();
        for (MatchData md : matches.values()) {
            if (md.isMatchForTeam(tid)) {
                if (type==-1 || (md.getType() & type)==type) list.add(md);
            }
        }
        return list;
    }

    /* Matches of any type in TYPE */
    public List<MatchData> getMatchesForTeam(int tid, int type, int officialStatus, int localisation, int result, int nationalStatus) {
        return getMatchesForTeam(tid, type, officialStatus, localisation, result, nationalStatus, -1, -1, null, null);
    }

    /* Matches of any type in TYPE, filtered by oppoId and leagueId */
    public List<MatchData> getMatchesForTeam(int tid, int type, int officialStatus, int localisation, int result, int nationalStatus, int leagueId, int oppoId) {
        return getMatchesForTeam(tid, type, officialStatus, localisation, result, nationalStatus, leagueId, oppoId, null, null);
    }

    /* Matches of any type in TYPE, filtered by oppoId and leagueId, and between startDate and endDate */
    public List<MatchData> getMatchesForTeam(int tid, int type, int officialStatus, int localisation, int result, int nationalStatus, int leagueId, int oppoId, Date startDate, Date endDate) {
        ArrayList<MatchData> list = new ArrayList<MatchData>();
        int _mt, aux;
        for (MatchData md : matches.values()) {
            if (tid>0 && !md.isMatchForTeam(tid)) continue;
            /* full parameter criteria OR normal friendlies (special status) */
            if (startDate!=null && md.getDate().before(startDate)) continue;
            if (endDate!=null   && md.getDate().after(endDate)) continue;
            if ( (((md.getType() & type)>0) && ((md.getType() & officialStatus)>0)) ||
                 (md.getType()==MATCH_FRIENDLY_NORMAL && (officialStatus & MT_FRIENDLY)>0) ) {
                if (md.isLocalTeam(tid)) _mt = MT_LOCAL;
                else _mt = MT_VISITOR;
                if ((localisation & _mt) == 0) continue;

                if ( (oppoId>0) && (oppoId != md.getOpponentId(tid)) ) continue;
                if ( (leagueId>0) && (leagueId != md.leagueId) ) continue;

                aux = md.getScore().getDifference() * (md.isLocalTeam(tid)?1:-1);
                if (aux > 0) _mt = MT_VICTORY;
                else if (aux == 0) _mt = MT_DRAW;
                else _mt = MT_DEFEAT;
                if ((result & _mt) == 0) continue;

                try {
                    if (getTeam(md.getLocalTid()).getCountryId() == getTeam(md.getVisitorTid()).getCountryId())
                        _mt = MT_NATIONAL;
                    else _mt = MT_INTERNATIONAL;
                    if ((nationalStatus & _mt) == 0) continue;
                } catch (NullPointerException npe) { continue; }
                list.add(md);
            }
        }
        java.util.Collections.sort(list, new MatchDateComparator(true));
        return list;
    }

    public List<MatchData> getMatchesVisitingCountry(int cid) {
        ArrayList<MatchData> list = new ArrayList<MatchData>();
        for (MatchData md : matches.values()) {
            if (md.visitorTid == MainFrame.getTeamId()) {
                TeamProfile tp = teams.get(md.localTid);
                if (tp==null) continue;
                if (tp.getCountryId() == cid) list.add(md);
            }
        }
        java.util.Collections.sort(list, new MatchDateComparator());
        return list;
    }

    public List<MatchData> getMatchesHostingCountry(int cid) {
        ArrayList<MatchData> list = new ArrayList<MatchData>();
        for (MatchData md : matches.values()) {
            if (md.localTid == MainFrame.getTeamId()) {
                TeamProfile tp = teams.get(md.visitorTid);
                if (tp==null) continue;
                if (tp.getCountryId() == cid) list.add(md);
            }
        }
        java.util.Collections.sort(list, new MatchDateComparator());
        return list;
    }

    public boolean addMatch(int mid, Date d, int t, int lid, int w, int sp, int ltid, int vtid, String ln, String vn, Score sc) {
        //!!TAKEN OUT IN CASE IT IS NOT FINISHED TO DOWNLOAD
        //!!if (matches.containsKey(mid)) return false;
        MatchData md = new MatchData(mid, d, t, lid, w, sp, ltid, vtid, ln, vn, sc);
        matches.put(mid, md);
        return true;
    }

    /* Match mid must already exist */
    public boolean addTeamStats(int mid, int tid, String tacn, float pos, float pih,
                                 int sh, int fo, int ofs, int yc, int rc, int rats, int ratp, int ratd) {
        if (!matches.containsKey(mid)) return false;
        MatchData md = matches.get(mid);
        return md.addTeamStats(tid, tacn, pos, pih, sh, fo, ofs, yc, rc, rats, ratp, ratd);
    }

    public boolean addPlayerStats(int pid, int mid, int tid, int po, int or, int rat, int tin, int tout, int sh,
                                  int fo, int as, int go, int yc, int rc, int tplay, int tdef, boolean inj) {
        MatchData md = matches.get(mid);
        return md.addPlayerStats(pid, tid, po, or, rat, tin, tout, sh, fo, as, go, yc, rc, tplay, tdef, inj);
    }


    public LeagueData addLeague(int lid, String name, int skType, int isOfficial, int isCup) {
        LeagueData ld = leagues.get(lid);
        if (ld == null) ld = new LeagueData(lid, name, skType, isOfficial, isCup);
        else ld.lname = name;
        leagues.put(lid, ld);
        return ld;
    }
    public LeagueData getLeague(int lid) {
        return leagues.get(lid);
    }

    public static MatchRepository load() {
        return loadObject( new MatchRepository() );
    }

    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (matches==null) matches = new HashMap<Integer,MatchData>();
        if (players==null) players = new HashMap<Integer,PlayerProfile>();
        if (teams==null) teams = new HashMap<Integer,TeamProfile>();
        if (leagues==null) leagues = new HashMap<Integer,LeagueData>();
        if (visitedFlags==null) visitedFlags = new TreeSet<CountryDataUnit>();
        if (hostedFlags==null) hostedFlags = new TreeSet<CountryDataUnit>();

        // FIX FOR MISSING timePlaying, timeDefending !!!
        //% REMOVE IN VERSION 0.8!!!
//         for (TeamProfile tp : teams.values()) {
//             for (TeamProfile.TeamStats ts : tp.stats.values()) {
//                 if (ts.timePlaying==0 && ts.timeDefending==0) {
//                     int timeDef = 0;
//                     int timePlay = 0;
//                     for (int pid : ts.posIds.values()) {
//                         PlayerProfile.PlayerStats ps = players.get(pid).stats.get(ts.matchId);
//                         timePlay += ps.timePlaying;
//                         timeDef += ps.timeDefending;
//                     }
//                     for (int pid : ts.subsIds.values()) {
//                         PlayerProfile.PlayerStats ps = players.get(pid).stats.get(ts.matchId);
//                         timePlay += ps.timePlaying;
//                         timeDef += ps.timeDefending;
//                     }
//                     ts.timePlaying = (timePlay==0 ? -1 : timePlay);
//                     ts.timeDefending = (timeDef==0 ? -1 : timeDef);
//                     if (so.So.DEBUG) System.out.println(" % " + timePlay + " : " + timeDef);
//                 }
//             }
//         }
    }

    // ############################################################
    // ############################################################
    public class MatchData implements Serializable {
        private static final long serialVersionUID = 4736521420560308215L;

        private int matchId;
        private Date date;
        private int leagueId;
        private int localTid;
        private int visitorTid;
        private String localName;
        private String visitorName;
        private Score score;
        private int spectators;
        private byte type;
        private byte weather;
        private boolean played;
        //private short week; // FALTA!!! (o sobra?)
        //private byte day;   // FALTA!!!
        private boolean complete;
        transient private TeamProfile.TeamStats localStats;
        transient private TeamProfile.TeamStats visitorStats;

        MatchData(int mid, Date d, int t, int lid, int w, int sp, int ltid, int vtid, String ln, String vn, Score sc) {
            matchId = mid;
            date = d;
            leagueId = lid;
            localTid = ltid;
            visitorTid = vtid;
            localName = ln;
            visitorName = vn;
            score = sc;
            spectators = sp;
            type = (byte)t;
            weather = (byte)w;
            played = true;
            complete = false;
            localStats = null;
            visitorStats = null;
       }

        public int getId() { return matchId; }
        public boolean isLocalTeam(int teamId) { return (teamId == localTid); }
        public Score getScore() { return score; }
        public Date getDate() { return date; }
        public int getLeagueId() { return leagueId; }
        public int getLocalTid() { return localTid; }
        public int getVisitorTid() { return visitorTid; }
        public String getLocalName() { return localName; }
        public String getVisitorName() { return visitorName; }
        public int getSpectators() { return spectators; }
        public int getType() { return type; }
        public boolean isFriendly() { return (type & MT_FRIENDLY) == MT_FRIENDLY; }
        public boolean isDataComplete() { return complete; }
        public boolean isMatchForTeam(int tid) { return tid==localTid || tid==visitorTid; }
        public int getOpponentId(int teamId) {
            if (teamId == localTid) return visitorTid;
            else if (teamId == visitorTid) return localTid;
            else return -1;
        }

        private boolean addTeamStats(int tid, String tacn, float pos, float pih, int sh, int fo, int ofs,
                                     int yc, int rc, int rats, int ratp, int ratd) {
            if (tid!=localTid && tid!=visitorTid) return false;
            TeamProfile tp = teams.get(tid);
            if (tp==null) return false;
            boolean local = isLocalTeam(tid);
            TeamProfile.TeamStats teamStats = tp.addTeamStats(matchId, local, tacn, pos, pih, sh, fo, ofs, yc, rc, rats, ratp, ratd);
            if (local) localStats = teamStats;
            else visitorStats = teamStats;
            return true;
        }

        private boolean addPlayerStats(int pid, int tid, int po, int or, int rat, int tin, int tout, int sh,
                                  int fo, int as, int go, int yc, int rc, int tplay, int tdef, boolean inj) {
            TeamProfile.TeamStats ts = null;
            if (tid==localTid) ts = getTeamStats(true);
            else if (tid==visitorTid) ts = getTeamStats(false);
            else return false;
            ts.addPlayerStats(pid, po, or, rat, tin, tout, sh, fo, as, go, yc, rc, tplay, tdef, inj);
            return true;
        }

        public TeamProfile.TeamStats getTeamStats(boolean local) {
            if (local) {
                if (localStats==null) {
                    TeamProfile tp = teams.get(localTid);
                    if (tp == null) return null; // error, this should never happen.
                    localStats = tp.stats.get(matchId);
                }
                return localStats;
            }
            else {
                if (visitorStats==null) {
                    TeamProfile tp = teams.get(visitorTid);
                    if (tp == null) return null; // error, this should never happen.
                    visitorStats = tp.stats.get(matchId);
                }
                return visitorStats;
            }
        }
        public TeamProfile.TeamStats getTeamStats(int tid) {
            if (localTid == tid) {
                if (localStats==null) {
                    TeamProfile tp = teams.get(localTid);
                    if (tp == null) return null; // error, this should never happen.
                    localStats = tp.stats.get(matchId);
                }
                return localStats;
            }
            else if (visitorTid == tid) {
                if (visitorStats==null) {
                    TeamProfile tp = teams.get(visitorTid);
                    if (tp == null) return null; // error, this should never happen.
                    visitorStats = tp.stats.get(matchId);
                }
                return visitorStats;
            }
            else return null;
        }

        public Object getData(int idx, int tid) {
            switch (idx) {
            case 0: return SokkerCalendar.normalizeFromPolishTimeZone(date);
            case 1:
                switch (leagueId) {
                case   3: return MainFrame.getLabel(TXT_FRIENDLY);
                case  13: return MainFrame.getLabel(TXT_FRIENDLY) + "(NT)";
                case 200: return MainFrame.getLabel(TXT_FRIENDLY) + " (" + MainFrame.getLabel(TXT_CUP) + ')';
                }
                LeagueData ld = getLeague(leagueId);
                return (ld == null)? MainFrame.getLabel(TXT_LEAGUE) + ' ' + Integer.toString(leagueId) : ld.getName();
            case 2: return localName;
            case 3: return score;
            case 4: return visitorName;
            case 5: return Long.valueOf(spectators);
            case 6: return Byte.valueOf(weather);
            case 7: return Float.valueOf(isLocalTeam(tid) ? visitorTid : localTid);
            case 8: return Integer.valueOf(matchId);
            default:
                return "";
            }
        }

        // FIX FOR CONSTANTS CHANGE, REMOVE IN 1.0!!!!!
        /* ************ Interface Serializable ************ */
        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
            if (type==1) type = MATCH_LEAGUE;
            if (type==2) type = MATCH_CUP;
            if (type==3) type = MATCH_QUALIFICATION;
            if (type==5) type = MATCH_FRIENDLY_CUPRULES;
            if (type==6) type = MATCH_FRIENDLY_LEAGUE;
            if (type==10) type = MATCH_NT_QUALIFICATION;
            if (type==11) type = MATCH_NT_WORLDCUP;
        }

//         public void debug() {
//             System.out.println("mid:"+ matchId + "  " +date+ "   lid:"+leagueId);
//             System.out.println("   * " + localTid + '=' + localName + " - " + visitorTid +'='+visitorName + "  " + score.toString());
//             System.out.println("   @+ " + spectators + ", " +  weather);
//         }
    }

    // ############################################################
    // ############################################################
    public class PlayerProfile implements Serializable {
        private static final long serialVersionUID = -4610364489734239357L;

        private int playerId;
        private String pname;
        private HashMap<Integer,PlayerStats> stats; // mid, stats
        transient private List<PlayerStats> statsList;
        transient private WeeklyStats [] weeklyStats;

        public PlayerProfile(int pid, String n) {
            playerId = pid;
            stats = new HashMap<Integer,PlayerStats>();
            statsList = null;
            weeklyStats = null;
        }
        private PlayerStats addPlayerStats(int mid, int tid, int pos, int or, int rat, int tin, int tout, int sh,
                                       int fo, int as, int go, int yc, int rc, int tplay, int tdef, boolean inj) {
            PlayerStats ps = new PlayerStats(mid, tid, pos, or, rat, tin, tout, sh, fo, as, go, yc, rc, tplay, tdef, inj);
            stats.put(mid, ps);
            statsList = null;
            weeklyStats = null;
            return ps;
        }

        /* list of Player Stats, reverse-ordered by date (most recent first) */
        public List<PlayerStats> getPlayerStats() {
            if (statsList == null) {
                statsList = new ArrayList<PlayerStats>( stats.size() );
                for (PlayerStats ps : stats.values()) if (ps.rating>0) statsList.add(ps);
                java.util.Collections.sort(statsList, new java.util.Comparator<PlayerStats>() {
                    public int compare(PlayerStats ps1, PlayerStats ps2) {
                        MatchData md1 = getMatch(ps1.matchId);
                        MatchData md2 = getMatch(ps2.matchId);
                        if (md1==null || md2==null) return 0;
                        return md2.date.compareTo(md1.date); // reverse Sort
                    }
                } );
            }
            return statsList;
        }

        public boolean hasPlayedInMatch(int mid) {
            if (!stats.containsKey(mid)) return false;
            PlayerStats ps = stats.get(mid);
            if (ps.rating>0 && ps.pos<12 && ((ps.timeOut>0?ps.timeOut:90) - ps.timeIn)>=30) return true;
            else return false;
        }
        public int getOrderInMatch(int mid) {
            if (!stats.containsKey(mid)) return -1;
            return stats.get(mid).order;
        }

        public WeeklyStats getWeeklyStats(int weeksAgo) {
            if (weeklyStats == null) {
                ArrayList<WeeklyStats> _ws = new ArrayList<WeeklyStats>();
                SokkerCalendar skCal = SokkerCalendar.getSharedInstance();
                Date startOfTrainingWeekDate = skCal.setDateToFirstTrainingDay( new Date() );
                SokkerWeek week = new SokkerWeek(startOfTrainingWeekDate);
                WeeklyStats weeklyStat = new WeeklyStats(week);
                _ws.add(weeklyStat);
                for (PlayerStats ps : getPlayerStats()) {
                    while (!week.contains(ps.getDate())) {
                        skCal.add(java.util.Calendar.WEEK_OF_YEAR, -1);
                        startOfTrainingWeekDate = skCal.getTime();
                        week = new SokkerWeek(startOfTrainingWeekDate);
                        weeklyStat = new WeeklyStats(week);
                        _ws.add(weeklyStat);
                    }
                    weeklyStat.put(ps);
                }
                weeklyStats = new WeeklyStats[_ws.size()];
                _ws.toArray(weeklyStats);
            }
            if (weeksAgo>=weeklyStats.length || weeksAgo<0) {
                return new WeeklyStats(new SokkerWeek( SokkerCalendar.getDateOfWeeksAgo(weeksAgo) ));
            }
            return weeklyStats[weeksAgo];
        }

//         public int getMatchesInWeek(int weeksAgo) {
//             if (matchesInWeek==null) {
//                 matchesInWeek = new int[MAX_WEEKS_IN_GRAPH];
//                 java.util.Arrays.fill(matchesInWeek, -1);
//             }
//             if (matchesInWeek[weeksAgo] == -1) {
//                 Date date = SokkerCalendar.getDateOfWeeksAgo(weeksAgo);
//                 matchesInWeek[weeksAgo] = getMatchesInWeek(date);
//                 matchesInWeek[weeksAgo] = getMatchesInWeek(date);
//             }
//             return matchesInWeek[weeksAgo];
//         }
//         /* TRAINING days are shifted to previous day */
//         public int getMatchesInWeek(Date date) {
//             if (date==null) return 0;
//             if (matchDates == null) {
//                 //matchDates = new TreeSet<Date>();
//                 matchDates = new TreeMap<Date, PlayerStats>();
//                 for (PlayerStats ps : getPlayerStats()) {
//                     matchDates.put(ps.getDate(), ps);
//                 }
//             }
//             SokkerWeek week = new SokkerWeek(date);
//             matchesInWeek[weekAgo] = matchDates.subSet(week.getBeginDate(), week.getEndDate()).size();
//         }

        /* ************ Interface Serializable ************ */
        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
            statsList = null;
        }

        // ==================================================
        public class PlayerStats implements Serializable {
            private static final long serialVersionUID = 9009728067373860820L;

            private int matchId;
            private int teamId;
            private byte pos;   // 1-16
            private byte order; // TRAINING_GK, TRAINING_DEF, TRAINING_MID, TRAINING_ATT
            private byte rating;
            private byte timeIn;
            private byte timeOut;
            private byte shots;
            private byte fouls;
            private byte assists;
            private byte goals;
            private byte yellowCards;
            private byte redCards;
            private short timePlaying;
            private short timeDefending;
            private boolean injured;

            public PlayerStats(int mid, int tid, int po, int or, int rat, int tin, int tout, int sh, int fo, int as,
                               int go, int yc, int rc, int tplay, int tdef, boolean inj) {
                matchId = mid;
                teamId = tid;
                pos = (byte)po;
                order = (byte)or;
                rating = (byte)rat;
                timeIn = (byte)tin;
                timeOut = (byte)tout;
                shots = (byte)sh;
                fouls = (byte)fo;
                assists = (byte)as;
                goals = (byte)go;
                yellowCards = (byte)yc;
                redCards = (byte)rc;
                timePlaying = (short)tplay;
                timeDefending = (short)tdef;
                injured = inj;
            }

            public Date getDate() {
                MatchData md = getMatch(matchId);
                if (md == null) return new Date(0);
                return SokkerCalendar.normalizeFromPolishTimeZone(md.getDate());
            }
            public int getMatchId() { return matchId; }
            public int getOrderPlayed() { return order; }
            public int getTimePlayed() {
                int t = 0;
                if (pos<12) t = (timeOut>0?timeOut:90) - timeIn;
                return t;
            }

            /* used by ratings panel in PlayerStatsPanel */
            public Object getData(int idx) {
                int aux;
                switch(idx) {
                case 0: return this.getDate();
                case 1: return Integer.valueOf(rating);
                case 2: return Integer.valueOf(pos);
                case 3:
                    switch(order) {
                    case 0: return "GK";
                    case 1: return "DEF";
                    case 2: return "MID";
                    case 3: return "ATT";
                    default: return "?";
                    }
                case 4: return getTimePlayed();
                case 5: /* off % */
                    aux = getTeam(teamId).stats.get(matchId).timePlaying;
                    return Float.valueOf(aux<=0 ? 0f : (timePlaying*100/(float)aux));
                case 6: /* def % */
                    aux = getTeam(teamId).stats.get(matchId).timeDefending;
                    return Float.valueOf(aux<=0 ? 0f : (timeDefending*100/(float)aux));
                case 7: return Integer.valueOf(goals);
                case 8: return Integer.valueOf(assists);
                case 9: return Integer.valueOf(shots);
                case 10: return Integer.valueOf(fouls);
                default:
                    return "";
                }
            }
        }
        // ==================================================
        public class WeeklyStats {
            private SokkerWeek week;
            //private PlayerStats psNT, psSunday, psWednesday;
            private ArrayList<PlayerStats> statsList; // orderer from most recent to older

            private WeeklyStats(SokkerWeek skWeek) {
                week = skWeek;
                statsList = new ArrayList<PlayerStats>(4);
            }

            private void put(PlayerStats ps) {
                statsList.add(ps);
            }

            public int getMatchesCount() {
                return statsList.size();
            }

            public int getMinutesPlayed() {
                int order = NO_DATA;
                int time = 0;
                for (PlayerStats ps : statsList) {
                    if (ps.getTimePlayed() > 0) {
                        if (order==NO_DATA) order = ps.getOrderPlayed();
                        if (order == ps.getOrderPlayed()) time += ps.getTimePlayed();
                    }
                }
                return time;
            }

            public int getOrderTrained() {
                for (PlayerStats ps : statsList) {
                    if (ps.getTimePlayed() > 0) return ps.getOrderPlayed();
                }
                return NO_DATA;
            }


        }

    }

    // ############################################################
    // ############################################################
    public class TeamProfile implements Serializable {
        private static final long serialVersionUID = -5673440449053499591L;

        private int teamId;
        private String teamName;
        private int countryId;
        private float rank;
        private HashMap<Integer,TeamStats> stats; //<mid, stats>
        private transient int latestMatchId;

        private TeamProfile(int tid, String tname, int cid, float r) {
            teamId = tid;
            teamName = tname;
            countryId = cid;
            rank = r;
            stats = new HashMap<Integer,TeamStats>();
            latestMatchId = 0;
        }

        public int getId() { return teamId; }
        public String getName() { return teamName; }
        public int getCountryId() { return countryId; }
        public float getRank() { return rank; }

        private TeamStats addTeamStats(int mid, boolean local, String tacn, float pos, float pih,
                                       int sh, int fo, int ofs, int yc, int rc, int rats, int ratp, int ratd) {
            TeamStats ts = new TeamStats(mid, local, tacn, pos, pih, sh, fo, ofs, yc, rc, rats, ratp, ratd);
            stats.put(mid, ts);
            latestMatchId = 0;
            return ts;
        }

        // ==================================================
        public class TeamStats implements Serializable {
            private static final long serialVersionUID = -5287219960701620444L;

            private int matchId;
            private boolean localInMatch;
            private String tacticName;
            private float possession;
            private float playInHalf;
            private int timePlaying;
            private int timeDefending;
            private byte shots;
            private byte fouls;
            private byte offsides;
            private byte yellowCards;
            private byte redCards;
            private byte ratingScoring;  // skl lvl
            private byte ratingPassing;   // skl lvl
            private byte ratingDefending; // skl lvl

            private HashMap<Integer,Integer> posIds;
            private TreeMap<Integer,Integer> subsIds;
            //transient private PlayerProfile.PlayerStats [] pStats;

            private TeamStats(int mid, boolean local, String tacn, float pos, float pih,
                              int sh, int fo, int ofs, int yc, int rc, int rats, int ratp, int ratd) {
                matchId = mid;
                localInMatch = local;
                tacticName = tacn;
                possession = pos;
                timePlaying = -1;
                timeDefending = -1;
                playInHalf = pih;
                shots = (byte)sh;
                fouls = (byte)fo;
                offsides = (byte)ofs;
                yellowCards = (byte)yc;
                redCards = (byte)rc;
                ratingScoring = (byte)rats;
                ratingPassing = (byte)ratp;
                ratingDefending = (byte)ratd;
                posIds = new HashMap<Integer,Integer>(17, 0.95f);
                subsIds = new TreeMap<Integer,Integer>();
                //pStats = null;
            }

            private boolean addPlayerStats(int pid, int pos, int or, int rat, int tin, int tout, int sh,
                                           int fo, int as, int go, int yc, int rc, int tplay, int tdef, boolean inj) {
                PlayerProfile pp = players.get(pid);
                if (pp == null) {
                    pp = new PlayerProfile(pid, "");
                    players.put(pid, pp);
                }
                PlayerProfile.PlayerStats ps = pp.addPlayerStats(matchId, teamId, pos, or, rat, tin, tout, sh, fo, as, go, yc, rc, tplay, tdef, inj);
                if (timePlaying<=0) timePlaying = (tplay>0 ? tplay : -1);
                else timePlaying += tplay;
                if (timeDefending<=0) timeDefending = (tdef>0 ? tdef : -1);
                else timeDefending += tdef;
                if (ps == null) return false;
                if (tin>0) addSubstitution(pos, pid);
                else posIds.put(pos, pid);
                // here cache if necessary
                return true;
            }
            private void addSubstitution(int pos, int pid) {
                if (subsIds.containsKey(pos)) addSubstitution(pos+100, pid);
                else subsIds.put(pos, pid);
            }

            public Date getMatchDate() {
                return getMatch(matchId).getDate();
            }
            public int getShots() { return shots; }
            public int getFouls() { return fouls; }
            public int getYellowCards() { return yellowCards; }
            public int getRedCards() { return redCards; }
            public float getPossession() { return possession/100f; }
            public float getPlayInHalf() { return playInHalf/100f; }
            public int getRatingScoring() { return ratingScoring; }
            public int getRatingPassing() { return ratingPassing; }
            public int getRatingDefending() { return ratingDefending; }

//             public void debug() {
//                 System.out.println("       ¬ " + (localInMatch?"L":"V")+ "  " + matchId + ", "+tacticName+", p" + possession+"%, h" + playInHalf + "%, sh"+shots+ ", fouls"+fouls+", yc"+yellowCards+", rc"+redCards+", SR=" + ratingScoring+ ", PR=" +ratingPassing+ ", DR" +ratingDefending );

//             }
        }

//         public void debug() {
//             System.out.println( teamName + " {" + teamId +'}');
//             System.out.println("   stats # " + stats.size() );
//             for (TeamStats ts : stats.values()) ts.debug();
//         }
    }

    // ############################################################
    // ############################################################
    public class LeagueData implements Serializable {
        private static final long serialVersionUID = 93761683890022L;

        private int leagueId;
        private byte ltype;
        private String lname;

        private LeagueData(int lid, String name, int skType, int isOfficial, int isCup) {
            leagueId = lid;
            lname = name;
            switch (skType) {
            case 3:
                if (isCup==1) ltype = (byte)MATCH_FRIENDLY_CUPRULES;
                else ltype = (byte)MATCH_FRIENDLY_NORMAL;
                break;
            case 0:
                if (isOfficial==1) ltype = (byte)MATCH_LEAGUE;
                else ltype = (byte)MATCH_FRIENDLY_LEAGUE;
                break;
            case 1:
                ltype = (byte)MATCH_CUP;
                break;
            case 2:
                ltype = (byte)MATCH_QUALIFICATION;
                break;
            case 4: /* WC Qualifiers */
                ltype = (byte)MATCH_NT_QUALIFICATION;
                break;
            case 5: /* WC Group phase */
            case 6: /* WC final rounds */
                ltype = (byte)MATCH_NT_WORLDCUP;
                break;
            case 7: /* junior league */
            default:
                ltype = 0;
            }
        }

        public int getId() { return leagueId; }
        public int getType() { return ltype; }
        public String getName() { return lname; }

        public String toString() { return lname + "[" + leagueId + "] t:" + ltype; }

        // FIX FOR CONSTANTS CHANGE, REMOVE IN 1.0!!!!!
        /* ************ Interface Serializable ************ */
        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
            if (ltype==1) ltype = MATCH_LEAGUE;
            if (ltype==2) ltype = MATCH_CUP;
            if (ltype==3) ltype = MATCH_QUALIFICATION;
            if (ltype==5) ltype = MATCH_FRIENDLY_CUPRULES;
            if (ltype==6) ltype = MATCH_FRIENDLY_LEAGUE;
            if (ltype==10) ltype = MATCH_NT_QUALIFICATION;
            if (ltype==11) ltype = MATCH_NT_WORLDCUP;
        }
    }

    // ############################################################
    // ############################################################
    public static class MatchDateComparator implements java.util.Comparator<MatchData>, Serializable {
        private static final long serialVersionUID = 37018L;

        private boolean reverse;

        public MatchDateComparator() {
            reverse = false;
        }
        public MatchDateComparator(boolean reverseOrder) {
            reverse = reverseOrder;
        }

        public int compare(MatchData md1, MatchData md2) {
            if (reverse) return md2.date.compareTo(md1.date);
            else return md1.date.compareTo(md2.date);
        }
    }

    // ############################################################
    // ############################################################

//     public void debug() {
//         System.out.println("MATCHES: " + matches.values().size());
//         for (MatchData md : matches.values()) md.debug();
//         System.out.println("");
//         System.out.println("TEAMS: " + teams.values().size());
//         for (TeamProfile tp : teams.values()) tp.debug();
//         System.out.println("");
//         System.out.println("PLAYERS: " + players.values().size());

//         System.out.println("");
//     }


}
