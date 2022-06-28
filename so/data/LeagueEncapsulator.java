package so.data;
/**
 * LeagueEncapsulator.java
 *
 * @author Daniel González Fisher
 */

import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

public class LeagueEncapsulator extends AbstractData implements java.io.Serializable {
    private static final long serialVersionUID = -2963970099185250242L;

    private TreeMap<SeasonIdPair, LeagueDetails> leagues;
    private SeasonIdPair myCurrentLeague;

    public LeagueEncapsulator() {
        this(so.Constants.FILENAME_LEAGUEDATA);
    }
    public LeagueEncapsulator(String filename) {
        super(filename);
        leagues = new TreeMap<SeasonIdPair, LeagueDetails>();
        myCurrentLeague = null;
    }

    public void updateLeagueDetails(LeagueDetails ld) {
        SeasonIdPair sip = new SeasonIdPair(ld);
        if ( leagues.containsKey(sip) ) {
            if (leagues.get(sip).getRound() > ld.getRound()) return;
        }
        leagues.put(sip, ld);
        if (ld.isMyLeague()) {
            if (myCurrentLeague==null || myCurrentLeague.season<sip.season) myCurrentLeague = sip;
        }
        ld.resetGraphicableData();
    }

    public LeagueDetails getLatestLeague() {
        if (leagues==null || leagues.isEmpty()) return null;
        return leagues.get( leagues.firstKey() );
    }
    public LeagueDetails getMyLatestLeague() {
        if (leagues==null || leagues.isEmpty()) return null;
        if (myCurrentLeague == null) return getLatestLeague();
        return leagues.get( myCurrentLeague );
    }
    public LeagueDetails getLeague(int season, int leagueId) {
        if (leagues==null || leagues.isEmpty()) return null;
        SeasonIdPair sip = new SeasonIdPair(season, leagueId);
        return leagues.get(sip);
    }

    public boolean deleteLeague(int season, int leagueId) {
        if (leagues==null || leagues.isEmpty()) return false;
        SeasonIdPair sip = new SeasonIdPair(season, leagueId);
        return (leagues.remove(sip) != null);
    }

    public List<LeagueDetails> getLeagues() {
        if (leagues.isEmpty()) return null; // new ArrayList<LeagueDetails>();
        List<LeagueDetails> lista = new ArrayList<LeagueDetails>( leagues.values() );
        java.util.Collections.sort(lista);
        return lista;
    }

    public static LeagueEncapsulator load() {
        return loadObject( new LeagueEncapsulator() );
    }

    /* ************************************************************ */
    public static class SeasonIdPair implements java.io.Serializable, Comparable<SeasonIdPair> {
        static final long serialVersionUID = 8216651988919697541L;
        int season;
        int leagueId;

        public SeasonIdPair(int s, int lid) {
            season = s;
            leagueId = lid;
        }
        public SeasonIdPair(LeagueDetails ld) {
            season = ld.getSeason();
            leagueId = ld.getId();
        }

        /* Interface Comparable */
        public int compareTo(SeasonIdPair sip) {
            if (season!=sip.season) return (season>sip.season)?-1:1;
            if (leagueId!=sip.leagueId) return (leagueId>sip.leagueId)?-1:1;
            return 0;
        }
        public boolean equals(Object obj) {
            if (obj==null || !(obj instanceof SeasonIdPair)) return false;
            return this.hashCode()==obj.hashCode();
        }
        public int hashCode() {
            return season + leagueId*1000;
        }
    }
}
