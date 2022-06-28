package so.data;

import so.gui.NotesEditorDialog;
import java.util.TreeMap;
import java.util.Date;
import static so.Constants.TEAMID_NO_TEAM;
import java.util.Calendar; //! REMOVE with FIX 0.9

public class TeamDetails implements java.io.Serializable, Noteable {
    private static final long serialVersionUID = 4380612725218068516L;

    private int id;
    private int userId;
    private int countryId;
    private String name;
    private int regionId;
    private Date creationDate;
    private int kit1Shirt;
    private int kit1Pants;
    private int kit2Shirt;
    private int kit2Pants;
    private int kitKShirt;
    private int kitKPants;
    private TreeMap<Date,TeamData> data;
    private boolean _registered; // for registering # of SO users; REMOVE IN 1.0
    private boolean _registered2; // for registering # of SO users; REMOVE IN 1.0
    private int _registerCount2;
    private String managerNotes;

    transient private TeamData latestData;
    transient private TeamData previousToLatestData;

    transient private NotesEditorDialog notesEditor;

    public TeamDetails() {
        this(TEAMID_NO_TEAM, 0, "", 0, null, 0, 0, 0, 0, 0, 0);
    }
    public TeamDetails(int i, int c, String n, int rid, Date cDate, int k1s, int k1p, int k2s, int k2p, int kks, int kkp) {
        id = i;
        countryId = c;
        name = n;
        regionId = rid;
        creationDate = cDate;
        kit1Shirt = k1s;
        kit1Pants = k1p;
        kit2Shirt = k2s;
        kit2Pants = k2p;
        kitKShirt = kks;
        kitKPants = kkp;
        data = new TreeMap<Date,TeamData>();
        _registered = false;  // REMOVE IN 1.0
        _registered2 = false; // REMOVE IN 1.0
        _registerCount2 = 0;   // REMOVE IN 1.0
        userId = 0;
        managerNotes = "";
        notesEditor = null;
    }

    public void setRegistered(boolean reg) { _registered = reg; }
    public void setRegistered2(boolean reg) { _registered2 = reg; }
    public boolean isRegistered() { return _registered; }
    public boolean isRegistered2() { return _registered2; }
    public int getRegisterCount2() { return _registerCount2; }
    public void addRegisterCount2() { _registerCount2++; }

    public boolean addTeamData(Date dt, long m, int f, int fcm, float r) {
        if (data.containsKey(dt)) return false;
        data.put(dt, new TeamData(dt, m, f, fcm, r));
        updateLatestData();
        return true;
    }

//     public boolean addTeamData(TeamDetails td) {
//         if (td == null) return false;
//         if (td.data == null) return false;
//         if (td.data.size() == 0) return false;
//         Date lastDate = td.data.lastKey();
//         if (data.containsKey( lastDate )) return false;
//         data.put(lastDate, td.data.get( lastDate ) );
//         updateLatestData();
//         return true;
//     }

    public boolean updateTeamDetails(Date updatingDate, int i, int c, String n, int rid, int uid, Date creatDate,
                                     int k1s, int k1p, int k2s, int k2p, int kks, int kkp) {
        if ( (this.id != i) && (this.id != TEAMID_NO_TEAM) ) return false;
        if ( this.id==TEAMID_NO_TEAM || data.size()==0 || data.lastKey().getTime()<updatingDate.getTime() ) {
            this.id = i;
            this.name = n;
            this.regionId = rid;
            this.creationDate = creatDate;
            this.countryId = c;
            this.kit1Shirt = k1s;
            this.kit1Pants = k1p;
            this.kit2Shirt = k2s;
            this.kit2Pants = k2p;
            this.kitKShirt = kks;
            this.kitKPants = kkp;
            so.gui.MainFrame.setCountry(countryId);
        }
        // FIX
        if (this.creationDate == null) this.creationDate = creatDate;
        if (this.userId == 0) userId = uid;
        return true;
    }

    public int    getId()         { return id; }
    public int    getCountryId()  { return countryId; }
    public String getName()       { return name; }
    public int    getRegionId()   { return regionId; }
    public Date getCreationDate() { return creationDate; }
    public int    getUserId()     { return userId; }

    public Date getDate()        { return (latestData==null) ? null : latestData.getDate(); }
    public long getMoney()       { return (latestData==null) ? 0  : latestData.getMoney(); }
    public int  getFans()        { return (latestData==null) ? 0  : latestData.getFans(); }
    public int  getFanClubMood() { return (latestData==null) ? 0  : latestData.getFanClubMood(); }
    public float getRank()       { return (latestData==null) ? 0f : latestData.getRank(); }

    public long getMoney(Date d)       { return (data.containsKey(d)) ? data.get(d).getMoney() : 0; }
    public int  getFans(Date d)        { return (data.containsKey(d)) ? data.get(d).getFans() : 0; }
    public int  getFanClubMood(Date d) { return (data.containsKey(d)) ? data.get(d).getFanClubMood() : 0; }
    public float getRank(Date d)       { return (data.containsKey(d)) ? data.get(d).getRank() : 0f; }

    /* interface Noteable */
    public String getNotesTitle() { return name; }
    public String getManagerNotes() { return (managerNotes==null)?"":managerNotes; }
    public void setManagerNotes(String notes) {
        if (notes==null || notes.trim().equals("")) notes = "";
        if (notes.replaceAll("\\<[^\\>]+\\>", "").replaceAll("\\W", "").trim().equals("")) notes = "";
        managerNotes = notes;
    }
    public boolean hasManagerNotes() {
        return managerNotes!=null && !managerNotes.equals("");
    }

    public void showManagerNotesEditor() {
        if (notesEditor == null) {
            notesEditor = new NotesEditorDialog(this);
        }
        notesEditor.setVisible(true);
    }

    /* **************************************************************** */
    protected TeamData getLatestData() {
        if (data.size()==0) return null;
        if (latestData == null) updateLatestData();
        return latestData;
    }
    protected TeamData getPreviousToLatestData() {
        if (previousToLatestData == null) updateLatestData();
        return previousToLatestData;
    }
    private void updateLatestData() {
        if (data.size()==0) latestData = null;
        else latestData = data.get(data.lastKey());
        if (data.size()<=1) previousToLatestData = latestData;
        else previousToLatestData = data.get( data.headMap( data.lastKey() ).lastKey() );
    }

    /* **************************************************************** */
    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        updateLatestData();
        notesEditor = null;
        //! FIX, REMOVE IN 0.9!!!!!!
        TreeMap<Date,TeamData> _datafix = new TreeMap<Date,TeamData>();
        Calendar cal = Calendar.getInstance();
        for (java.util.Map.Entry<Date,TeamData> par : data.entrySet()) {
            Date d = par.getKey();
            TeamData td = par.getValue();
            cal.setTime(d);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            d = cal.getTime();
            td.date = d;
            _datafix.put(d, td);
        }
        data = _datafix;
        //!
    }

    /* **************************************************************** */
    /**
     *  Class TeamData
     */
    protected static class TeamData implements java.io.Serializable {
        private static final long serialVersionUID = -6956784801699794189L;

        private Date date;
        private long money;
        private short fans;
        private byte fanClubMood;
        private float rank;

        public TeamData() {
            this(null, 0, 0, 0, 0f);
        }
        public TeamData(Date dt, long m, int f, int fcm, float r) {
            date = dt;
            money = m;
            fans = (short)f;
            fanClubMood = (byte)fcm;
            rank = r;
        }

        public Date getDate()        { return date; }
        public long getMoney()       { return money; }
        public int  getFans()        { return fans; }
        public int  getFanClubMood() { return fanClubMood; }
        public float getRank()       { return rank; }
    }

}
