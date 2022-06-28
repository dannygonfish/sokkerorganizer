package so.data;
/**
 * PlayerRoster.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class PlayerRoster implements java.io.Serializable {
    private static final long serialVersionUID = -665374645977901675L;

    private int teamid;
    private TreeMap<Integer, PlayerProfile> allPlayers;
    transient private ArrayList<PlayerProfile> activePlayers;
    transient private ArrayList<PlayerProfile> formerPlayers;
    transient private Date allPlayersListDate;
    transient private ArrayList<PlayerProfile> allPlayersOnDate;

    public PlayerRoster(int id) {
        teamid = id;
        allPlayers = new TreeMap<Integer, PlayerProfile>();
        activePlayers = new ArrayList<PlayerProfile>();
        formerPlayers = new ArrayList<PlayerProfile>();
    }
    public PlayerRoster() {
        this(TEAMID_NO_TEAM);
    }

    public int getActivePlayerCount() { return activePlayers.size(); }
    public List<PlayerProfile> getPlayersList() { return activePlayers; }
    public List<PlayerProfile> getFormerPlayersList() { return formerPlayers; }
    public void setTeamId(int id) { teamid = id; }
    public int getTeamId() { return teamid; }
    public PlayerProfile getPlayer(int pid) { return allPlayers.get(pid); }

    public boolean updatePlayers(Set<PlayerProfile> players, Date updatingDate) {
        if (players == null) return false;
        Date currentPlayersDate = null;
        if (activePlayers.size() > 0) currentPlayersDate = activePlayers.get(0).getCurrentDate();
        boolean isThisNewUpdate = currentPlayersDate==null || updatingDate.compareTo(currentPlayersDate)>=0;
        for (PlayerProfile p : players) {
            int id = p.getId();
            if (allPlayers.containsKey(id)) {
                PlayerProfile player = allPlayers.get(id);
                player.addPlayerData(p, isThisNewUpdate);
                if ( isThisNewUpdate && !player.isActive() ) {
                    player.setActive(true);
                    formerPlayers.remove(player);
                    activePlayers.add(player);
                }
            }
            else {
                allPlayers.put(id, p);
                if (isThisNewUpdate) {
                    p.setActive(true);
                    activePlayers.add(p);
                }
            }
        }
        // players not updated are not active anymore
        if (isThisNewUpdate) {
            PlayerProfile [] auxArray = new PlayerProfile[activePlayers.size()];
            auxArray = activePlayers.toArray(auxArray);
            for (int i=0; i<auxArray.length; i++) {
                if ( !players.contains(auxArray[i]) ) {  // equals
                    activePlayers.remove(auxArray[i]);
                    auxArray[i].setActive(false);
                    formerPlayers.add(auxArray[i]);
                }
            }
        }
        java.util.Collections.sort(activePlayers);
        return true;
    }

    private List<PlayerProfile> getAllPlayersOn(Date date) {
        if (allPlayersListDate==null || allPlayersOnDate==null || !allPlayersListDate.equals(date)) {
            if (allPlayersOnDate != null) allPlayersOnDate.clear();
            else allPlayersOnDate = new ArrayList<PlayerProfile>();
            for (PlayerProfile p : allPlayers.values()) {
                if (p.wasActiveOn(date)) allPlayersOnDate.add(p);
            }
        }
        return allPlayersOnDate;
    }

    public int getActivePlayerCountOn(Date date) {
        return getAllPlayersOn(date).size();
    }

    public int getPlayersByNationalityCount(int nationality) {
        int n = 0;
        for (PlayerProfile p : activePlayers) {
            if (p.getCountryFrom()==nationality) n++;
        }
        return n;
    }
    public int getPlayersByNationalityCount(int nationality, Date date) {
        int n = 0;
        for (PlayerProfile p : getAllPlayersOn(date)) {
            if (p.getCountryFrom()==nationality) n++;
        }
        return n;
    }

    public double getAverageAge() {
        int age = 0;
        for (PlayerProfile p : activePlayers) {
            age += p.getLatestData().getAge();
        }
        return (double)age / (double)activePlayers.size();
    }
    public double getAverageAge(Date date) {
        int age = 0;
        for (PlayerProfile p : getAllPlayersOn(date)) {
            age += p.getAge(date);
        }
        return (double)age / (double)allPlayersOnDate.size();
    }

    public int getTotalSalary() {
        int salary = 0;
        for (PlayerProfile p : activePlayers) {
            salary += p.getLatestData().getSalary();
        }
        return salary;
    }
    public int getTotalSalary(Date date) {
        int salary = 0;
        for (PlayerProfile p : getAllPlayersOn(date)) {
            salary += p.getSalary(date);
        }
        return salary;
    }

    public double getAverageSalary() {
        return (double)getTotalSalary() / (double)activePlayers.size();
    }
    public double getAverageSalary(Date date) {
        return (double)getTotalSalary(date) / (double)allPlayersOnDate.size();
    }

    public double getAverageForm() {
        int form = 0;
        for (PlayerProfile p : activePlayers) {
            form += p.getLatestData().getForm();
        }
        return (double)form / (double)activePlayers.size();
    }
    public double getAverageForm(Date date) {
        int form = 0;
        for (PlayerProfile p : getAllPlayersOn(date)) {
            form += p.getForm(date);
        }
        return (double)form / (double)allPlayersOnDate.size();
    }

    public int getTotalValue() {
        int value = 0;
        for (PlayerProfile p : activePlayers) {
            value += p.getLatestData().getValue();
        }
        return value;
    }
    public int getTotalValue(Date date) {
        int value = 0;
        for (PlayerProfile p : getAllPlayersOn(date)) {
            value += p.getValue(date);
        }
        return value;
    }

    public double getAverageValue() {
        return (double)getTotalValue() / (double)activePlayers.size();
    }
    public double getAverageValue(Date date) {
        return (double)getTotalValue(date) / (double)allPlayersOnDate.size();
    }

    public TreeSet<Date> getDates() {
        TreeSet<Date> dates = new TreeSet<Date>();
        for (PlayerProfile p : activePlayers) {
            dates.addAll( p.getDates() );
        }
        return dates;
    }
    public TreeSet<Date> getAllDates() {
        TreeSet<Date> dates = new TreeSet<Date>();
        for (PlayerProfile p : allPlayers.values()) {
            dates.addAll( p.getDates() );
        }
        return dates;
    }

    public void updatePositionRatings() {
        for (PlayerProfile p : activePlayers) p.updatePositionRatings();
    }

    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        activePlayers = new ArrayList<PlayerProfile>();
        formerPlayers = new ArrayList<PlayerProfile>();
        for (Map.Entry<Integer, PlayerProfile> player : allPlayers.entrySet()) {
            if (player.getValue().isActive()) activePlayers.add(player.getValue());
            else formerPlayers.add(player.getValue());
        }
        java.util.Collections.sort(activePlayers);
    }

}
