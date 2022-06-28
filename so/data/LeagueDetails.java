package so.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LeagueDetails implements java.io.Serializable, Comparable<LeagueDetails> {
    private static final long serialVersionUID = 3752216159832394105L;

    private int id;
    private short country;
    private short season;
    private short round;
    private String name;

    private ArrayList<MatchData> matches;
    private HashMap<String, TeamProfile> teams;
    transient private RoundData roundData;
    transient private int currentRound;
    //& transient private int comparableRound;
    transient private ArrayList<TeamProfile> currentStandings;

    public LeagueDetails(int i, int cid, String n, int s) {
        teams = new HashMap<String, TeamProfile>(9, 0.95f);
        matches = new ArrayList<MatchData>();
        id = i;
        country = (short)cid;
        name = n;
        season = (short)s;
        round = 0;
        roundData = null;
        currentRound = -1;
        //& comparableRound = 0;
        currentStandings = null;
    }

    public String toString() {
        return Integer.toString(season) + " - " + name;
    }
    //% DEBUG !!!!!!!
//     public String debugString() {
//         return name + ", season " + season + ", round "+ round +", country=" + country + ", id=" + id;
//     }
//     public void debug() {
//         System.out.println( debugString() );
//         for (TeamProfile tp : teams.values()) {
//             System.out.println( tp.debugString() );
//         }
//     }
    //% DEBUG !!!!!!!

    public void setName(String n) { name = n; }
    public void setID(int i) { id = i; }
    public void setCountry(int c) { country = (short)c; }
    public void setSeason(int s) { season = (short)s; }
    public void setRound(int r) { round = (short)r; }

    public int getId() { return id; }
    public int getCountry() { return country; }
    public int getSeason() { return season; }
    public int getRound() { return round; }
    public String getName() { return name; }

    public void addTeam(int tid, String tname, int lastSeasonPos) {
        TeamProfile team;
        String stid = Integer.toString(tid);
        if (teams.containsKey(stid)) {
            team = teams.get(stid);
            team.name = tname;
        }
        else {
            team = new TeamProfile(tid, tname, lastSeasonPos);
            teams.put(stid, team);
        }
    }

    public void addMatch(int round, int matchid, int tid1, int tid2, int goals1, int goals2, int mir, boolean finished) {
        TeamProfile tp1 = teams.get( Integer.toString(tid1) );
        TeamProfile tp2 = teams.get( Integer.toString(tid2) );
        //% System.out.println ("· "+round+"/" + mir +": " + team1 +" - "+team2+ "  ["+ goals1 +","+goals2+"]");
        MatchData match = new MatchData(matchid, round, tp1.getId(), tp2.getId(), goals1, goals2, finished, mir);
        int auxIdx = (round-1)*4 + mir-1;
        if (auxIdx >= matches.size()) matches.add(match);
        else matches.set(auxIdx, match);
        if (finished) {
            tp1.addMatchData(match);
            tp2.addMatchData(match);
        }
    }

    private String simplifyName(String n) {
        return n.replaceAll("\\W", "").toLowerCase();
    }

    public List<TeamProfile> getTeamsList(int round) {
        if (round<0 || round>this.round) return null;
        if (currentRound!=round || currentStandings==null) {
            currentStandings = new ArrayList<TeamProfile>( teams.values() );
            TeamProfile.comparableRound = round; //&
            java.util.Collections.sort(currentStandings);
            currentRound = round;
        }
        return currentStandings;
    }

    public MatchData getMatch(int round, int matchInRound) {
        return matches.get( (round-1)*4 + matchInRound-1 );
    }

    public TeamProfile getTeam(int tid) {
        return teams.get( Integer.toString(tid) );
    }

    /* debug */
//     public boolean deleteTeam(int tid) {
//         return (teams.remove(Integer.toString(tid)) != null);
//     }

    public String getTeamName(int tid) {
        TeamProfile tp = getTeam(tid);
        if (tp==null) return "";
        return tp.getName();
    }

    public boolean isMyLeague() {
        int myTid = so.gui.MainFrame.getTeamId();
        if (myTid<1) return false;
        if (getTeam(myTid)==null) return false;
        return true;
    }

    /* Interface Comparable */
    public int compareTo(LeagueDetails ld) {
        if (season!=ld.season) return (season>ld.season)?1:-1;
        if (round!=ld.round) return (round>ld.round)?1:-1;
        if (id!=ld.id) return (id<ld.id)?1:-1;
        return 0;
    }
    public boolean equals(Object obj) {
        if (obj==null || !(obj instanceof LeagueDetails)) return false;
        LeagueDetails ld = (LeagueDetails)obj;
        return this.id==ld.id && this.season==ld.season && this.round==ld.round;
    }

    /* FIX for sokker.org BUG */
    //& MAKE NESTED CLASSES to INNER CLASSES (remove static)
    private void fixLastSeasonPositions() {
        int [] _pos = new int[9];
        java.util.HashSet<TeamProfile> bugTeams = new java.util.HashSet<TeamProfile>();
        for (TeamProfile tp : teams.values()) {
            int lsp = tp.lastSeasonPos;
            if (lsp>0 && lsp<9) _pos[lsp] = tp.id;
            else bugTeams.add(tp);
        }
        for (TeamProfile tp : bugTeams) {
            for (int i=1; i<=8; i++) {
                if (_pos[i]<1) {
                    _pos[i] = tp.id;
                    tp.lastSeasonPos = (short)i;
                    break;
                }
            }
        }
        resetGraphicableData();
    }
    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        for (TeamProfile tp : teams.values()) {
            if (tp.lastSeasonPos<1 || tp.lastSeasonPos>8) {
                fixLastSeasonPositions();
                break;
            }
        }
    }

    // =================================================================================
    public static class TeamProfile implements java.io.Serializable, Comparable<TeamProfile> {
        private static final long serialVersionUID = -8526058669124527997L;

        static int comparableRound = 0; //&

        private int id;
        private String name;
        private short dataCount;
        private TeamData [] data;
        private short lastSeasonPos;

        public TeamProfile(int i, String n, int lsp) {
            id = i;
            name = n;
            dataCount = 0;
            data = new TeamData[14];
            lastSeasonPos = (short)lsp;
        }

        /* old HTML parsing */
//         public TeamProfile(int i, String n) {
//             this(i, n, 0);
//         }

        public String getName() { return name; }
        public int getLastSeasonPos() { 
            return (lastSeasonPos>0)?((lastSeasonPos<9)?lastSeasonPos:1):8;
            //& if (lastSeasonPos<1 || lastSeasonPos>8) fixLastSeasonPositions();
            //& return lastSeasonPos;
        }

        public void addMatchData(MatchData md) {
            if (dataCount>=data.length) return; // porsiaca
            if (dataCount>=md.getRound()) return;
            short gf, ga;
            boolean local = false;
            if (id == md.tidL) { //local
                gf = md.goalsL;
                ga = md.goalsV;
                local = true;
            }
            else { //visitor
                gf = md.goalsV;
                ga = md.goalsL;
            }
            if (dataCount==0) {
                data[dataCount] = new TeamData();
                if (lastSeasonPos==0) lastSeasonPos = (short)((5 - md.numberInRound) * 2 - (local?0:1));
            }
            else data[dataCount] = new TeamData(data[dataCount-1]);
            data[dataCount].addMatch(gf, ga);
            dataCount++;
        }

        public Object getData(int round, int idx) {
            if (round > dataCount) return "";
            if (round == 0) {
                if (idx==0) return Integer.valueOf(lastSeasonPos);
                else if (idx==1) return name;
                else return Integer.valueOf(0);
            }
            TeamData td = data[round-1];
            switch (idx) {
            case 0: return Integer.valueOf(lastSeasonPos);
            case 1: return name;
            case 2: return Integer.valueOf(td.matches);
            case 3: return Integer.valueOf(td.wins);
            case 4: return Integer.valueOf(td.draws);
            case 5: return Integer.valueOf(td.losses);
            case 6: return Integer.valueOf(td.goalsFavour);
            case 7: return Integer.valueOf(td.goalsAgainst);
            case 8: return Integer.valueOf(td.getGoalDiff());
            case 9: return Integer.valueOf(td.getPoints());
            default:
                return "";
            }
        }

        public int getId() { return id; }

        //% DEBUG
//         private String debugString() {
//             return name + " [id=" + Integer.toString(id) + "] " + dataCount + "; lsp=" + lastSeasonPos ;
//         }
        //% DEBUG

        /* Interface Comparable */
        public int compareTo(TeamProfile tp) {
            if (comparableRound > dataCount) return 0;
            if (tp==null) return 0;
            try {
                if (comparableRound > 0) { /* ordenar de mayor a menor */
                    if      (this.data[comparableRound-1].getPoints() > tp.data[comparableRound-1].getPoints()) return -1;
                    else if (this.data[comparableRound-1].getPoints() < tp.data[comparableRound-1].getPoints()) return 1;
                    if      (this.data[comparableRound-1].getGoalDiff() > tp.data[comparableRound-1].getGoalDiff()) return -1;
                    else if (this.data[comparableRound-1].getGoalDiff() < tp.data[comparableRound-1].getGoalDiff()) return 1;
                    if      (this.data[comparableRound-1].goalsFavour > tp.data[comparableRound-1].goalsFavour) return -1;
                    else if (this.data[comparableRound-1].goalsFavour < tp.data[comparableRound-1].goalsFavour) return 1;
                }
                if      (this.lastSeasonPos < tp.lastSeasonPos) return -1;
                else if (this.lastSeasonPos > tp.lastSeasonPos) return 1;
            } catch (Exception e) {
                System.err.println("cr:" + comparableRound + "  dC:"+dataCount);
                e.printStackTrace();
            }
            return 0;
        }
        public boolean equals(Object obj) {
            if (obj==null || !(obj instanceof TeamProfile)) return false;
            TeamProfile tp = (TeamProfile)obj;
            return this.id==tp.id;
        }
        public int hashCode() { return id; }

        protected static class TeamData implements java.io.Serializable {
            private static final long serialVersionUID = 1979982215390665442L;

            private short matches;
            private short wins;
            private short draws;
            private short losses;
            private short goalsFavour;
            private short goalsAgainst;

            public TeamData() {
                matches = 0;
                wins = 0;
                draws = 0;
                losses = 0;
                goalsFavour = 0;
                goalsAgainst = 0;
            }
            public TeamData(TeamData td) {
                matches = td.matches;
                wins = td.wins;
                draws = td.draws;
                losses = td.losses;
                goalsFavour = td.goalsFavour;
                goalsAgainst = td.goalsAgainst;
            }

            public void addMatch(short goalsScored, short goalsReceived) {
                matches++;
                if (goalsScored > goalsReceived) wins++;
                else if (goalsScored < goalsReceived) losses++;
                else draws++;
                goalsFavour += goalsScored;
                goalsAgainst += goalsReceived;
            }
            public int getPoints() { return wins*3 + draws; }
            public int getGoalDiff() { return goalsFavour - goalsAgainst; }
        }

    }

    // =================================================================================
    public static class MatchData implements java.io.Serializable {
        private static final long serialVersionUID = 3547871976964244932L;

        private short round; // 1 to 14
        private int id;
        private int tidL;
        private int tidV;
        private short goalsL;
        private short goalsV;
        private boolean played;
        private short numberInRound; // 1 to 4

        public MatchData(int i, int r, int tL, int tV, int gL, int gV, boolean p, int nir) {
            id = i;
            round = (short)r;
            tidL = tL;
            tidV = tV;
            goalsL = (short)gL;
            goalsV = (short)gV;
            played = p;
            numberInRound = (short)nir;
        }

        public boolean hasBeenPlayed() { return played; }
        public int getId() { return id; }
        public int getRound() { return round; }
        public int getNumberInRound() { return numberInRound; }
        public int getLocalTID() { return tidL; }
        public int getVisitorTID() { return tidV; }
        public int getLocalGoals() { return goalsL; }
        public int getVisitorGoals() { return goalsV; }

        //% DEBUG
//         public void debug(LeagueDetails ld) {
//             System.out.print("["+round+","+numberInRound+"] id="+id);
//             System.out.print("  L:"+ld.getTeamName(tidL)+"("+tidL+")");
//             System.out.print("  V:"+ld.getTeamName(tidV)+"("+tidV+")");
//             System.out.println("   {"+goalsL+","+goalsV+"}  "+played);
//         }
        //% DEBUG
    }
    // =================================================================================
    public void resetGraphicableData() { roundData = null; }
    public GraphicableData getGraphicableData() {
        if (roundData == null) roundData = new RoundData();
        return roundData;
    }

    public class RoundData implements GraphicableData {
        private int [][] data;

        public RoundData() {
            data = new int[15][8];
            for (int r=0; r<=round; r++) {
                List<TeamProfile> lista = getTeamsList(r);
                for (int i=0; i<8; i++) data[r][lista.get(i).getLastSeasonPos()-1] = i+1;
            }
            for (int r=round+1; r<=14; r++) {
                for (int i=0; i<8; i++) data[r][i] = so.Constants.NO_DATA;
            }
        }

        public int size() { return 15; }
        public int getFieldsCount() { return 8; }
        public int getFieldScale(int idx) { return 8; }

        public java.util.Date getDate(int week) {
            return null;
        }

        public int getData(int week, int team) {
            return data[week][team];
        }

        public String getTitle() { return name; }

    }

}
