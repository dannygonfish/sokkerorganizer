package so.data;

import java.util.TreeMap;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.Date;


public class Stadium implements java.io.Serializable {
    private static final long serialVersionUID = -4376924323281302979L;
    private static final String [] LOCATIONS = { "N", "S", "E", "W", "NE", "NW", "SE", "SW" };

    public static final short SEATS    = 3;
    public static final short BENCHES  = 2;
    public static final short TERRACES = 1;
    public static final short STANDING = 0;

    private String name;
    private TreeMap<Date,StadiumData> data;
    transient private StadiumData latestData;
    transient private StadiumData previousToLatestData;

    public Stadium(String n) {
        name = n;
        data = new TreeMap<Date,StadiumData>();
    }
    public Stadium() {
        this("");
    }

    public boolean addStadiumData(Date date, StadiumData sd) {
        if (sd == null) return false;
        if (data.size() != 0) {
            if (data.containsKey(date)) return false;
            if (dataIsRedundant(sd)) return false;
        }
        data.put(date, sd);
        updateLatestData();
        return true;
    }

    private boolean dataIsRedundant(StadiumData sd) {
        if (data.size() == 0) return false;
        SortedMap<Date, StadiumData> _map = data.headMap( sd.getDate() );
        if (_map.isEmpty()) return false;
        return _map.get(_map.lastKey()).hasSameData(sd);
    }

    public boolean updateStadium(Stadium st, Date updatingDate) {
        if (st == null) return false;
        if (st.data == null) return false;
        if (st.data.size() == 0) return false;
        if (data.size() == 0) name = st.name;
        else if (updatingDate.compareTo( data.lastKey() ) > 0) name = st.name;
        addStadiumData(st.getDate(), st.getLatestData());
        return true;
    }

    public String getName() { return name; }
    public Date getDate() { return (latestData==null) ? null : latestData.getDate(); }

    /* **************************************************************** */
    protected StadiumData getLatestData() {
        if (data.size()==0) return null;
        if (latestData == null) updateLatestData();
        return latestData;
    }
    protected StadiumData getPreviousToLatestData() {
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
    }

    /* **************************************************************** */
    /**
     *  Class StadiumData
     */
    public static class StadiumData implements java.io.Serializable {
        private static final long serialVersionUID = 330743460352014181L;

        private Date date;
        private HashMap<String, StandSection> stands;

        public StadiumData(Date dt) {
            date = dt;
            stands = new HashMap<String, StandSection>(9, 0.95f);
        }

        public Date getDate() { return date; }

        public void addStand(String l, int c, short t, float d, boolean r) {
            stands.put(l, new StandSection(l, c, t, d, r));
        }

        public boolean hasSameData(StadiumData sd) {
            for (String loc : LOCATIONS) {
                if (!this.stands.get(loc).equals(sd.stands.get(loc))) return false;
            }
            return true;
        }

        /* **************************************************************** */
        /**
         *  Class StandSection
         */
        protected static class StandSection implements java.io.Serializable {
            private static final long serialVersionUID = -6690426895680891125L;

            private String location;
            private int capacity;
            private byte type;
            private float daysLeft;
            private boolean roof;

            public StandSection() {
                this("", 0, (short)0, 0f, false);
            }
            public StandSection(String l, int c, short t, float d, boolean r) {
                location = l;
                capacity = c;
                type = (byte)(t & 0xff);
                daysLeft = d;
                roof = r;
            }

            public boolean equals(Object obj) {
                if (obj == null) return false;
                if (!(obj instanceof StandSection)) return false;
                StandSection ss = (StandSection)obj;
                if (!this.location.equals(ss.location)) return false;
                if (this.capacity != ss.capacity) return false;
                if (this.type != ss.type) return false;
                if (this.daysLeft != ss.daysLeft) return false;
                if (this.roof != ss.roof) return false;
                return true;
            }
            public int hashCode() {
                int loc = 0;
                for (loc=0; loc<LOCATIONS.length; loc++) if (location.equals(LOCATIONS[loc])) break;
                return (roof?1:0) + (int)type*10 + (int)(daysLeft*100) + capacity*1000 + loc*10000;
            }

        }

    }

}
