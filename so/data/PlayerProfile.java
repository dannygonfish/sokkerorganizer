package so.data;
/**
 * PlayerProfile.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import static so.Constants.Colors.*;
import static so.Constants.Positions.*;
import so.util.SokkerCalendar;
import so.gui.NotesEditorDialog;
import java.util.Date;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.Set;
import java.util.ArrayList;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Calendar; //! REMOVE with FIX 0.9


public class PlayerProfile implements java.io.Serializable, Comparable<PlayerProfile>, Noteable {
    static final long serialVersionUID = 7766143279522125767L;

    static private SokkerCalendar skCal = new SokkerCalendar();

    private int id;
    private String name;
    private String surname;
    private short countryFrom;
    private boolean active;
    private TreeMap<Date,PlayerData> data;
    private int ownerTeamId;
    private int juniorTeamId;
    private int preferredPosition;
    private char squadAssignment;
    private boolean sendToNTDB;
    private String managerNotes;

    transient private PlayerData latestData;
    transient private PlayerData previousToLatestData;
    transient private PlayerData comparisonData;

    transient private HashMap<Integer, Double> positionRatings;
    transient private int bestPosition;

    private static Date dateForComparison = null;
    transient private Date myDateForComparison;
    transient private WeeklyData weeklyData;

    transient private NotesEditorDialog notesEditor;

    public PlayerProfile(int i, String n, String sn, short cf, int tid, int jtid) {
        id = i;
        name = n;
        surname = sn;
        countryFrom = cf;
        data = new TreeMap<Date,PlayerData>();
        latestData = null;
        previousToLatestData = null;
        comparisonData = null;
        myDateForComparison = null;
        ownerTeamId = tid;
        juniorTeamId = jtid;
        positionRatings = new HashMap<Integer, Double>();
        preferredPosition = NO_POSITION;
        bestPosition = NO_POSITION;
        weeklyData = null;
        active = false;
        sendToNTDB = false;
        squadAssignment = ' ';
        managerNotes = "";
        notesEditor = null;
    }

    public static PlayerProfile getRandomPlayer() {
        PlayerProfile randPlayer = new PlayerProfile(getRandomShort(32000), "Random", "Player " + getRandomShort(100),
                                                     (short)1, getRandomShort(12000), getRandomShort(12000));
        randPlayer.addPlayerData(new Date(), getRandomShort(32000), getRandomShort(16000), getRandomShort(50),
                                 22, getRandomShort(100), getRandomShort(100), getRandomShort(100),
                                 getRandomShort(17), getRandomShort(11), getRandomShort(17), getRandomShort(17),
                                 getRandomShort(17), getRandomShort(17), getRandomShort(17), getRandomShort(17),
                                 getRandomShort(17), (short)0, 0, getRandomShort(17), getRandomShort(17),
                                 getRandomShort(17), false, false, false, 0, 0, 0, 0);
        return randPlayer;
    }
    private static short getRandomShort(int max) {
        return (short)Math.round( Math.random()*max  );
    }

    public boolean addPlayerData(Date dt, int vl, int sy, int ag, int ht, int ms, int go, int as, int fm, int st, int pc,
                                 int te, int ps, int kp, int df, int pm, int sc, int cd, int in, int td, int xp,
                                 int tw, boolean tl, boolean ntp, boolean ntpu21, int ntM, int ntG, int ntA, int ntC) {
        if (data.containsKey(dt)) return false;
        data.put(dt, new PlayerData(dt, vl, sy,  ag, ht, ms, go, as, fm, st, pc, te, ps, kp, df, pm, sc, cd, in, td, xp, tw, tl, ntp, ntpu21, ntM, ntG, ntA, ntC));
        updateLatestData();
        updatePositionRatings();
        return true;
    }
    // unused, reserved for national team plugin
    public boolean addPlayerData(PlayerData pd) {
        if (pd == null) return false;
        if (data.containsKey(pd.date)) return false;
        data.put(pd.date, pd);
        updateLatestData();
        return true;
    }
    public boolean addPlayerData(PlayerProfile pp) {
        return addPlayerData(pp, false);
    }
    public boolean addPlayerData(PlayerProfile pp, boolean newerUpdate) {
        if (pp == null) return false;
        if (pp.data == null) return false;
        if (pp.data.size() == 0) return false;
        if (data.containsKey( pp.data.lastKey() )) return false;
        data.put(pp.data.lastKey(), pp.data.get( pp.data.lastKey() ) );
        if (newerUpdate) {
            this.ownerTeamId = pp.ownerTeamId;
            if (pp.juniorTeamId>0) this.juniorTeamId = pp.juniorTeamId;
            name = pp.name;
            surname = pp.surname;
            if (countryFrom == NO_COUNTRY_SET) countryFrom = pp.countryFrom;
        }
        updateLatestData();
        updatePositionRatings();
        return true;
    }

    public void setOwnerTeam(int id, String name) {
        ownerTeamId = id;
    }

    protected PlayerData getLatestData() {
        if (data.size()==0) return null;
        if (latestData == null) updateLatestData();
        return latestData;
    }
    protected PlayerData getPreviousToLatestData() {
        //if (data.size()<=1) return ;
        if (previousToLatestData == null) updateLatestData();
        return previousToLatestData;
    }
    protected PlayerData getComparisonData() {
        if (comparisonData == null) updateComparisonData();
        else if (dateForComparison == null) comparisonData = getPreviousToLatestData();
        else if (!dateForComparison.equals(myDateForComparison)) updateComparisonData();
        return comparisonData;
    }
    private void updateLatestData() {
        if (data.size()==0) latestData = null;
        else latestData = data.get(data.lastKey());
        if (data.size()<=1) previousToLatestData = latestData;
        else previousToLatestData = data.get( data.headMap( data.lastKey() ).lastKey() );
        weeklyData = null;
    }
    private void updateComparisonData() {
        if (data.size()<=1) comparisonData = latestData;
        else if (dateForComparison == null) comparisonData = getPreviousToLatestData();
        else if (data.containsKey( dateForComparison )) comparisonData = data.get(dateForComparison);
        else {
            SortedMap<Date, PlayerData> auxData = data.headMap( dateForComparison );
            if (auxData.isEmpty()) comparisonData = data.get( data.firstKey() );
            else comparisonData = auxData.get( auxData.lastKey() );
        }
        myDateForComparison = dateForComparison;
    }

    public Date getDataDatePreviousTo(Date date) {
        if (data.isEmpty()) return null;
        if (date == null) return data.get( data.firstKey() ).getDate();
        else if (data.containsKey( date )) return data.get(date).getDate();
        else {
            SortedMap<Date, PlayerData> auxData = data.headMap( date );
            if (auxData.isEmpty()) return data.get( data.firstKey() ).getDate();
            else return auxData.get( auxData.lastKey() ).getDate();
        }
    }

    public Set<Date> getDates() { return data.keySet(); }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getFullName() { return name + ' ' + surname; }
    public short getCountryFrom() { return countryFrom; }
    public int getOwnerTeamId() { return ownerTeamId; }
    public int getSourceTeamId() { return juniorTeamId; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { active = a; }
    public int getPreferredPosition() { return preferredPosition; }
    public void setPreferredPosition(int pos) { preferredPosition = pos; }
    public char getSquadAssignment() { return squadAssignment; }
    public void setSquadAssignment(char c) { squadAssignment = c; }
    public Date getCurrentDate() { return getLatestData().getDate(); }
    public String toString() { return getFullName(); }
    public void setSendToNTDB(boolean send) { sendToNTDB = send; }
    public boolean allowSendToNTDB() { return sendToNTDB; }
    public boolean isNTPlayer()       { return getLatestData().ntPlayer; }
    public boolean isNTPlayerU21()       { return getLatestData().ntPlayerU21; }
    public boolean isTransferListed() { return getLatestData().transferListed; }
    /* interface Noteable */
    public String getNotesTitle() { return getFullName(); }
    public String getManagerNotes() { return (managerNotes==null)?"":managerNotes; }
    public void setManagerNotes(String notes) {
        if (notes==null || notes.trim().equals("")) notes = "";
        if (notes.replaceAll("\\<[^\\>]+\\>", "").replaceAll("\\W", "").trim().equals("")) notes = "";
        managerNotes = notes;
    }
    public boolean hasManagerNotes() {
        return managerNotes!=null && !managerNotes.equals("");
    }

    public boolean wasActiveOn(Date d) { return data.containsKey(d); }

    public Object getData(int idx) {
        PlayerData a = getLatestData();
        switch(idx) {
        case 0: return new DataPair((a.isNTPlayer()||a.isNTPlayerU21())?DATA_NAME2:DATA_NAME, name, surname, CELLCOLOR_NAME, a.canPlay() ? null : Color.RED );
        case 1: return so.gui.MainFrame.getFlagIcon(countryFrom);
        case 2: return Integer.valueOf(a.getAge());
        case 3:
            if (preferredPosition == NO_POSITION) return new DataPair(DATA_POSITION, getBestPosition(), getPositionRating(getBestPosition()) );
            else return new DataPair(DATA_POSITION, preferredPosition, getPositionRating(preferredPosition), CELLCOLOR_PREF_POS);
        case 19: return new DataPair(DATA_STATUS, a.getCards(), (double)a.getInjuryDays() );
        case 34: return Character.valueOf(squadAssignment);
        case 36: return Integer.valueOf(a.getHeight());
        default:
        }
        PlayerData b = getComparisonData();
        switch (idx) {
        case 4: return new DataPair(DATA_COMPARABLE_SKILL, a.getForm(), b.getForm(), CELLCOLOR_FORM);
        case 5: return new DataPair(DATA_COMPARABLE_SKILL, a.getTacticalDiscipline(), b.getTacticalDiscipline(), CELLCOLOR_NAME);
        case 6: return new DataPair(DATA_COMPARABLE_SKILL, a.getExperience(), b.getExperience(), CELLCOLOR_NAME);
        case 7: return new DataPair(DATA_COMPARABLE_SKILL, a.getTeamWork(), b.getTeamWork(), CELLCOLOR_NAME);
        case 8: return new DataPair(DATA_COMPARABLE_SKILL, a.getStamina(), b.getStamina(), CELLCOLOR_SECONDARIES);
        case 9: return new DataPair(DATA_COMPARABLE_SKILL, a.getPace(), b.getPace(), CELLCOLOR_SECONDARIES);
        case 10: return new DataPair(DATA_COMPARABLE_SKILL, a.getTechnique(), b.getTechnique(), CELLCOLOR_SECONDARIES);
        case 11: return new DataPair(DATA_COMPARABLE_SKILL, a.getPassing(), b.getPassing(), CELLCOLOR_SECONDARIES);
        case 12: return new DataPair(DATA_COMPARABLE_SKILL, a.getKeeper(), b.getKeeper(), CELLCOLOR_PRIMARIES);
        case 13: return new DataPair(DATA_COMPARABLE_SKILL, a.getDefender(), b.getDefender(), CELLCOLOR_PRIMARIES);
        case 14: return new DataPair(DATA_COMPARABLE_SKILL, a.getPlaymaker(), b.getPlaymaker(), CELLCOLOR_PRIMARIES);
        case 15: return new DataPair(DATA_COMPARABLE_SKILL, a.getScorer(), b.getScorer(), CELLCOLOR_PRIMARIES);
        case 16: return new DataPair(DATA_COMPARABLE_NUMBER, a.getMatches(), b.getMatches());
        case 17: return new DataPair(DATA_COMPARABLE_NUMBER, a.getGoals(), b.getGoals());
        case 18: return new DataPair(DATA_COMPARABLE_NUMBER, a.getAssists(), b.getAssists());
        case 20: return new DataPair(DATA_COMPARABLE_CURRENCY, a.getValue(), b.getValue());
        case 21: return new DataPair(DATA_COMPARABLE_CURRENCY, a.getSalary(), b.getSalary());
        case 22: return new DataPair(DATA_RATING, GK, getBestPosition(), getPositionRating(GK));
        case 23: return new DataPair(DATA_RATING, WB, getBestPosition(), getPositionRating(WB));
        case 24: return new DataPair(DATA_RATING, CB, getBestPosition(), getPositionRating(CB));
        case 25: return new DataPair(DATA_RATING, SW, getBestPosition(), getPositionRating(SW));
        case 26: return new DataPair(DATA_RATING, DM, getBestPosition(), getPositionRating(DM));
        case 27: return new DataPair(DATA_RATING, CM, getBestPosition(), getPositionRating(CM));
        case 28: return new DataPair(DATA_RATING, AM, getBestPosition(), getPositionRating(AM));
        case 29: return new DataPair(DATA_RATING, WM, getBestPosition(), getPositionRating(WM));
        case 30: return new DataPair(DATA_RATING, FW, getBestPosition(), getPositionRating(FW));
        case 31: return new DataPair(DATA_RATING, ST, getBestPosition(), getPositionRating(ST));
        case 32: return Integer.valueOf(id);
        //33 given from SquadPanel.java
        case 35: return Boolean.valueOf(sendToNTDB);
        default:
            return "";
        }
    }

    public int getAge()        { return getLatestData().getAge(); }
    public int getHeight()     { return getLatestData().getHeight(); }
    public int getForm()       { return getLatestData().getForm(); }
    public int getStamina()    { return getLatestData().getStamina(); }
    public int getPace()       { return getLatestData().getPace(); }
    public int getTechnique()  { return getLatestData().getTechnique(); }
    public int getPassing()    { return getLatestData().getPassing(); }
    public int getKeeper()     { return getLatestData().getKeeper(); }
    public int getDefender()   { return getLatestData().getDefender(); }
    public int getPlaymaker()  { return getLatestData().getPlaymaker(); }
    public int getScorer()     { return getLatestData().getScorer(); }
    public int getCards()      { return getLatestData().getCards(); }
    public int getInjuryDays() { return getLatestData().getInjuryDays(); }
    public int getValue()      { return getLatestData().getValue(); }
    public int getSalary()     { return getLatestData().getSalary(); }
    public int getExperience() { return getLatestData().getExperience(); }
    public int getTeamWork()   { return getLatestData().getTeamWork(); }
    public int getTacticalDiscipline() { return getLatestData().getTacticalDiscipline(); }

    public int getAge(Date d)        { return data.get(d).getAge(); }
    public int getHeight(Date d)     { return data.get(d).getHeight(); }
    public int getForm(Date d)       { return data.get(d).getForm(); }
    public int getStamina(Date d)    { return data.get(d).getStamina(); }
    public int getPace(Date d)       { return data.get(d).getPace(); }
    public int getTechnique(Date d)  { return data.get(d).getTechnique(); }
    public int getPassing(Date d)    { return data.get(d).getPassing(); }
    public int getKeeper(Date d)     { return data.get(d).getKeeper(); }
    public int getDefender(Date d)   { return data.get(d).getDefender(); }
    public int getPlaymaker(Date d)  { return data.get(d).getPlaymaker(); }
    public int getScorer(Date d)     { return data.get(d).getScorer(); }
    public int getCards(Date d)      { return data.get(d).getCards(); }
    public int getInjuryDays(Date d) { return data.get(d).getInjuryDays(); }
    public int getValue(Date d)      { return data.get(d).getValue(); }
    public int getSalary(Date d)     { return data.get(d).getSalary(); }
    public int getExperience(Date d) { return data.get(d).getExperience(); }
    public int getTeamWork(Date d)   { return data.get(d).getTeamWork(); }
    public int getTacticalDiscipline(Date d) { return data.get(d).getTacticalDiscipline(); }

    public double getPositionRating(int position) {
        if (position == NO_POSITION) return 0.0;
        position = position & ~(P_LEFT | P_RIGHT | RESERVE);
        if (positionRatings.containsKey(position)) {
            return positionRatings.get(position);
        }
        else {
            double rating = so.gui.MainFrame.getPositionRating(position, this);
            positionRatings.put(position, rating);
            return rating;
        }
    }
    public int getBestPosition() {
        if (bestPosition == NO_POSITION) {
            updatePositionRatings();
        }
        return bestPosition;
    }
    public void updatePositionRatings() {
        int L = RATEABLE_POSITIONS.length;
        double maxRating = 0.0;
        bestPosition = 0;
        double rating = 0.0;
        for (int i=0; i<L; i++) {
            rating = so.gui.MainFrame.getPositionRating(RATEABLE_POSITIONS[i], this);
            positionRatings.put(RATEABLE_POSITIONS[i], rating);
            if (rating > maxRating) {
                maxRating = rating;
                bestPosition = RATEABLE_POSITIONS[i];
            }
        }
    }

    public GraphicableData getGraphicableData() {
        if (weeklyData == null) weeklyData = new WeeklyData(MAX_WEEKS_IN_GRAPH);
        return weeklyData;
    }

    public void showManagerNotesEditor() {
        if (notesEditor == null) {
            notesEditor = new NotesEditorDialog(this);
        }
        notesEditor.setVisible(true);
    }

    /* .................................. */
    public static void setDateForComparison(Date d) { dateForComparison = d; }
    public Date getDateForComparison() { return dateForComparison; }

    /* ################################## */
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if ( !obj.getClass().equals(this.getClass()) ) return false;
        PlayerProfile p = (PlayerProfile)obj;
        return (this.id == p.id);
        //return (this.id == p.id && this.name.equals(obj.name) && this.surname.equals(obj.surname) );
    }
    public int hashCode() { return id; }

    /* Interface Comparable */
    public int compareTo(PlayerProfile obj) throws ClassCastException {
        int primary = surname.compareTo(obj.surname);
        if (primary != 0) return primary;
        int secondary = name.compareTo(obj.name);
        if (secondary != 0) return secondary;
        if (this.id > obj.id) return 1;
        else if (this.id < obj.id) return -1;
        return 0;
    }

    /* interface Encryptable */
//     public byte[] getByteArray() {
//         byte[] nameBA = null;
//         byte[] surnBA = null;
//         try {
//             nameBA = name.getBytes(Cipher.CHARSET);
//             surnBA = surname.getBytes(Cipher.CHARSET);
//         } catch (java.io.UnsupportedEncodingException uee) {
//             return null;
//         }
//         int arrLength = 4 + 2 + 2 + nameBA.length + surnBA.length + 18;
//         //byte [] byteArray = new byte[arrLength];
//         ByteBuffer bb = ByteBuffer.allocate(arrLength);
//         bb.clear();
//         bb.putInt(id).putShort(countryFrom).put((byte)nameBA.length).put(nameBA).put((byte)surnBA.length).put(surnBA);
//         PlayerData _pd = getLatestData();
//         if (_pd != null) bb.put( _pd.getByteBuffer() );
//         bb.rewind();
//         return bb.array();
//     }


    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        positionRatings = new HashMap<Integer, Double>();
        bestPosition = NO_POSITION;
        latestData = null;
        previousToLatestData = null;
        comparisonData = null;
        myDateForComparison = null;
        weeklyData = null;
        notesEditor = null;
        //! TEMP FIX, REMOVE IN VERSION 0.9!!!
//         TreeMap<Date,PlayerData> _datafix = new TreeMap<Date,PlayerData>();
//         Calendar cal = Calendar.getInstance();
//         for (java.util.Map.Entry<Date,PlayerData> par : data.entrySet()) {
//             Date d = par.getKey();
//             PlayerData pd = par.getValue();
//             cal.setTime(d);
//             cal.set(Calendar.SECOND, 0);
//             cal.set(Calendar.MILLISECOND, 0);
//             d = cal.getTime();
//             pd.date = d;
//             _datafix.put(d, pd);
//         }
//         data = _datafix;
        //!

    }

    /**
     *  Class PlayerData
     *  must be protected to be accesible from PlayerRoster
     */
    protected static class PlayerData implements java.io.Serializable {
        static final long serialVersionUID = -5427885930527380777L;

        private Date date;
        private int value; // *
        private int salary;
        private byte age;
        private short height;
        private short matches;
        private short goals;
        private short assists;
        private byte form;
        private byte stamina;
        private byte pace;
        private byte technique;
        private byte passing;
        private byte keeper;
        private byte defender;
        private byte playmaker;
        private byte scorer;
        private byte tacticalDiscipline;
        private byte experience;
        private byte teamWork;
        private byte cards;
        private byte injuryDays;
        private boolean transferListed;
        private boolean ntPlayer;
        private boolean ntPlayerU21;
        private short ntMatches;
        private short ntGoals;
        private short ntAssists;
        private byte  ntCards;

        public PlayerData() {
            this(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 0, 0, 0, 0);
        }
        public PlayerData(Date dt, int vl, int sy, int ag, int ht, int ms, int go, int as, int fm, int st, int pc, int te, int ps,
                          int kp, int df, int pm, int sc, int cd, int in, int td, int xp, int tw, boolean tl, boolean ntp,
                          boolean ntpu21, int ntM, int ntG, int ntA, int ntC) {
            date = dt;
            value = vl;
            salary = sy;
            age = (byte)ag;
            height = (short)ht;
            matches = (short)ms;
            goals = (short)go;
            assists = (short)as;
            form = (byte)fm;
            stamina = (byte)st;
            pace = (byte)pc;
            technique = (byte)te;
            passing = (byte)ps;
            keeper = (byte)kp;
            defender = (byte)df;
            playmaker = (byte)pm;
            scorer = (byte)sc;
            cards = (byte)cd;
            injuryDays = (byte)in;
            tacticalDiscipline = (byte)td;
            experience = (byte)xp;
            teamWork = (byte)tw;
            transferListed = tl;
            ntPlayer = ntp;
            ntPlayerU21 = ntpu21;
            ntMatches = (short)ntM;
            ntGoals = (short)ntG;
            ntAssists = (short)ntA;
            ntCards = (byte)ntC;
        }

        public Date  getDate()       { return date; }
        public int   getValue()      { return value; }
        public int   getSalary()     { return salary; }
        public short getAge()        { return age; }
        public short getHeight()     { return height; }
        public short getMatches()    { return matches; }
        public short getGoals()      { return goals; }
        public short getAssists()    { return assists; }
        public short getForm()       { return form; }
        public short getStamina()    { return stamina; }
        public short getPace()       { return pace; }
        public short getTechnique()  { return technique; }
        public short getPassing()    { return passing; }
        public short getKeeper()     { return keeper; }
        public short getDefender()   { return defender; }
        public short getPlaymaker()  { return playmaker; }
        public short getScorer()     { return scorer; }
        public short getCards()      { return cards; }
        public int   getInjuryDays() { return injuryDays; }
        public short getExperience() { return experience; }
        public short getTeamWork()   { return teamWork; }
        public short getTacticalDiscipline() { return tacticalDiscipline; }
        public boolean isNTPlayer()          { return ntPlayer; }
        public boolean isNTPlayerU21()          { return ntPlayerU21; }
        public boolean isTransferListed()    { return transferListed; }
        public boolean canPlay() { return (cards<3 && injuryDays<7); }

        ByteBuffer getByteBuffer() {
            ByteBuffer bb = ByteBuffer.allocate(18);
            bb.clear();
            bb.putInt(value).putInt(salary).put((byte)age);
            bb.put( (byte)((form << 2) + ( stamina >> 4)) );
            bb.put( (byte)((stamina << 4) + (pace >> 2)) );
            bb.put( (byte)((pace << 6) + technique) );
            bb.put( (byte)((passing << 2) + (keeper >> 4)) );
            bb.put( (byte)((keeper << 4) + (defender >> 2)) );
            bb.put( (byte)((defender << 6) + playmaker) );
            bb.put( (byte)((scorer << 2) + (tacticalDiscipline >> 4)) );
            bb.put( (byte)((tacticalDiscipline << 4) + (experience >> 2)) );
            bb.put( (byte)((experience << 6) + teamWork) );
            bb.rewind();
            return bb;
        }
        /* ************ Interface Serializable ************ */
//         private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
//             in.defaultReadObject();
//         }
    }

    /**
     *  Class WeeklyData
     */
    public class WeeklyData implements GraphicableData {
        private int weeksCount;
        private PlayerData [] weekData;

        protected WeeklyData(int weeks) {
            weeksCount = 0;
            weekData = null;
            //weeksCount = weeks;
            //weekData = new PlayerData[weeks];
            buildDataArray();
        }

        public final int getFieldsCount() { return 11; }

        public int size() { return weeksCount; }

        public int getFieldScale(int idx) {
            if (idx == 0) return 1000;
            return 17;
        }

        public String getTitle() { return getFullName(); }

        public Date getDate(int week) {
            if (week>=weeksCount || weekData[week] == null) return null;
            return weekData[week].getDate();
        }
        public int getData(int week, int idx) {
            if (week>=weeksCount || week<0) return NO_DATA;
            if (weekData[week] == null) return NO_DATA;
            switch(idx) {
            case 0:  return weekData[week].getValue();
            case 1:  return weekData[week].getExperience();
            case 2:  return weekData[week].getForm();
            case 3:  return weekData[week].getStamina();
            case 4:  return weekData[week].getPace();
            case 5:  return weekData[week].getTechnique();
            case 6:  return weekData[week].getPassing();
            case 7:  return weekData[week].getKeeper();
            case 8:  return weekData[week].getDefender();
            case 9:  return weekData[week].getPlaymaker();
            case 10: return weekData[week].getScorer();
            case 11: return weekData[week].getAge();
            default: return NO_DATA;
            }
        }

        private void buildDataArray() {
            if (data.isEmpty()) return;
            ArrayList<PlayerData> _wd = new ArrayList<PlayerData>();
            _wd.add( data.get( data.lastKey() ) );
            Date startOfTrainingWeekDate = skCal.setDateToFirstTrainingDay( new Date() );
            SortedMap<Date, PlayerData> auxData = data.headMap( startOfTrainingWeekDate );
            for ( ; !auxData.isEmpty() ; auxData = data.headMap(startOfTrainingWeekDate) ) {
                _wd.add( auxData.get( auxData.lastKey() ) );
                skCal.add(java.util.Calendar.WEEK_OF_YEAR, -1);
                startOfTrainingWeekDate = skCal.getTime();
            }
            weeksCount = _wd.size();
            weekData = new PlayerData[weeksCount];
            _wd.toArray(weekData);
        }
//         private void buildDataArray() {
//             if (data.isEmpty()) return;
//             weekData[0] = data.get( data.lastKey() );
//             Date startOfTrainingWeekDate = skCal.setDateToFirstTrainingDay( new Date() );
//             for (int i=1; i<weeksCount; i++) {
//                 SortedMap<Date, PlayerData> auxData = data.headMap( startOfTrainingWeekDate );
//                 if (auxData.isEmpty()) {
//                     weekData[i] = null;
//                     break;
//                 }
//                 weekData[i] = auxData.get( auxData.lastKey() );
//                 skCal.add(java.util.Calendar.WEEK_OF_YEAR, -1);
//                 startOfTrainingWeekDate = skCal.getTime();
//             }
//         }

    }

    // ..................................................
//     public void debug() {
//         System.out.println("$$$ " + getFullName() + " [" + id + "] " + getCountryFrom());
//         System.out.println("age: " + getAge() + " val=" + getValue() + " wage=" + getSalary());
//         System.out.println("cards: " + getCards() + "  injuries: " + getInjuryDays());
//         System.out.println("FORM= " + so.gui.MainFrame.getSkillLevelName(getForm()));
//         System.out.println("stam= " + so.gui.MainFrame.getSkillLevelName(getStamina()) + "\tkeep= " + so.gui.MainFrame.getSkillLevelName(getKeeper()));
//         System.out.println("pace= " + so.gui.MainFrame.getSkillLevelName(getPace()) + "\tdefe= " + so.gui.MainFrame.getSkillLevelName(getDefender()));
//         System.out.println("tech= " + so.gui.MainFrame.getSkillLevelName(getTechnique()) + "\tplay= " + so.gui.MainFrame.getSkillLevelName(getPlaymaker()));
//         System.out.println("pass= " + so.gui.MainFrame.getSkillLevelName(getPassing()) + "\tscor= " + so.gui.MainFrame.getSkillLevelName(getPassing()));
//     }

    public void updateTacticalDiscipline(Date date, int tacD, int exp, int tWork) {
        if (data.size()<1) return;
        PlayerData pd = null;
        if (date==null) pd = getLatestData();
        else {
            pd = data.get(date);
            if (pd==null) {
                SortedMap<Date, PlayerData> auxData = data.headMap( date );
                if (auxData.size()<1) return;
                pd = auxData.get( auxData.lastKey() );
            }
        }
        if (pd==null) return;
        pd.tacticalDiscipline = (byte)tacD;
        pd.experience = (byte)exp;
        pd.teamWork = (byte)tWork;
    }

}
