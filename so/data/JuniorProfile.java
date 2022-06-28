package so.data;
/**
 * JuniorProfile.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import static so.Constants.Colors.*;
import static so.Constants.Positions.*;
import so.util.SokkerCalendar;
import java.util.Date;
import java.util.TreeMap;
import java.awt.Color;
import java.util.SortedMap;
//import java.util.Calendar; //! REMOVE with FIX 0.9

public class JuniorProfile implements java.io.Serializable, Comparable<JuniorProfile> {
    private static final long serialVersionUID = 7435567677481712362L;

    private int id;
    private String name;
    private String surname;
    private TreeMap<Date,JuniorData> data;
    private short weekFirstSkillup;
    private short weekLastSkillup;
    private byte formation;
    private boolean active;

    private boolean _fix095; //! USED IN 0.95 FIX REMOVE in 1.0

    transient private JuniorData latestData;
    transient private JuniorData previousWeekData;
    transient private WeeklyData weeklyData;

    static private SokkerCalendar skCal = new SokkerCalendar();

    public JuniorProfile(int i, String n, String sn) {
        this(i, n, sn, NO_POSITION);
    }

    public JuniorProfile(int i, String n, String sn, int fmt) {
        id = i;
        name = n;
        surname = sn;
        formation = (byte)fmt;
        data = new TreeMap<Date,JuniorData>();
        weekFirstSkillup = JR_NO_SKILLUP_YET;
        weekLastSkillup = JR_NO_SKILLUP_YET;
        active = false;
        latestData = null;
        previousWeekData = null;
    }

    public boolean addJuniorData(Date dt, short we, short sk, short ag) {
        if (data.containsKey(dt)) return false;
        data.put(dt, new JuniorData(dt, we, sk, ag) );
        updateLatestData();
        evalSkillupWeeks();
        return true;
    }
    public boolean addJuniorData(JuniorProfile jp) {
        if (jp == null) return false;
        if (jp.data == null) return false;
        if (jp.data.size() == 0) return false;
        if (data.containsKey( jp.data.lastKey() )) return false;
        data.put(jp.data.lastKey(), jp.data.get( jp.data.lastKey() ) );
        //! FIX for 0.95 for adding formation to old juniors . REMOVE in 1.0
        if (formation == (byte)NO_POSITION) formation = jp.formation;
        //!
        updateLatestData();
        evalSkillupWeeks();
        return true;
    }

    private void evalSkillupWeeks() {
        if (data.size() <= 1) return;
        if ( !getPreviousWeekData().isNullData() && getPreviousWeekData().getSkill() < getLatestData().getSkill() ) {
            // skill has changed, always increases
            // no need to call getLatestData() because it was previously called and data.size() was checked also
            weekLastSkillup = latestData.getWeeks();
            if (weekFirstSkillup == JR_NO_SKILLUP_YET) weekFirstSkillup = weekLastSkillup;
        }
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getFullName() { return name + " " + surname; }
    public short getFormation() { return formation; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { active = a; }
    public short getWeeks() {
        if (data.size() == 0) return JR_NO_DATA;
        if (getLatestData()==null) return JR_NO_DATA;
        return latestData.getWeeks();
    }
    public short getSkill() {
        if (data.size() == 0) return JR_NO_DATA;
        if (getLatestData()==null) return JR_NO_DATA;
        return latestData.getSkill();
    }
    public short getAge() {
        if (data.size() == 0) return JR_NO_DATA;
        if (getLatestData()==null) return JR_NO_DATA;
        return latestData.getAge();
    }
    public short getWeekFirstSkillup() { return weekFirstSkillup; }
    public short getWeekLastSkillup() { return weekLastSkillup; }
    public short getInitialWeeks() {
        if (data.size() == 0) return JR_NO_DATA;
        return data.get( data.firstKey() ).getWeeks();
    }
    public short getInitialSkill() {
        if (data.size() == 0) return JR_NO_DATA;
        return data.get( data.firstKey() ).getSkill();
    }
    public short getSkillupCount() {
        if (data.size() == 0) return 0;
        if (getLatestData()==null) return 0;
        return (short)(latestData.getSkill() - getInitialSkill());
    }

    public float getAverageWeeksToSkillup() {
        short skillupCount = getSkillupCount();
        if (skillupCount <= 1) return 0;
        float ret = (float)(weekFirstSkillup - weekLastSkillup) / (float)(skillupCount - 1);
        /* fix for weird bug, maybe REMOVE in 0.6 */
        if (ret < JR_BEST_TALENT) {
            _recalcSkillupWeeks();
            ret = (float)(weekFirstSkillup - weekLastSkillup) / (float)(skillupCount - 1);
        }
        return ret;
    }
    public Date getCurrentDate() { 
        return getLatestData().getDate();
    }

    protected JuniorData getLatestData() { // OK
        if (data.size()==0) return null;
        if (latestData == null) updateLatestData();
        return latestData;
    }
    protected JuniorData getPreviousWeekData() { // OK
        //if (data.size()<=1) return ;
        if (previousWeekData == null) updateLatestData();
        return previousWeekData;
    }
    private void updateLatestData() { // OK
        if (data.size()==0) latestData = null;
        else latestData = data.get(data.lastKey());
        if (data.size()<=1) previousWeekData = JuniorData.createNullData();
        else {
            Date startOfTrainingWeekDate = skCal.getDateOfFirstTrainingDay( data.lastKey() );
            java.util.SortedMap<Date, JuniorData> auxData = data.headMap( startOfTrainingWeekDate );
            //if (auxData.size() == 0) previousWeekData = data.get( data.firstKey() );
            if (auxData.size() == 0) previousWeekData = JuniorData.createNullData();
            else previousWeekData = data.get( auxData.lastKey() );
        }
        weeklyData = null;
    }

    /* fix in v0.502 */
    private void _recalcSkillupWeeks() {
        weekFirstSkillup = JR_NO_SKILLUP_YET;
        weekLastSkillup = JR_NO_SKILLUP_YET;
        short prevSkill = -1;
        for (JuniorData jd : data.values()) {
            if (jd.isNullData()) continue;
            if (prevSkill == -1) prevSkill = jd.getSkill();
            if ( prevSkill < jd.getSkill() ) {
                // skill has changed, always increases
                weekLastSkillup = jd.getWeeks();
                if (weekFirstSkillup == JR_NO_SKILLUP_YET) weekFirstSkillup = weekLastSkillup;
            }
            prevSkill = jd.getSkill();
        }
    }

    public Object getData(int idx) {
        //if (data.size == 0) return "";
        JuniorData a = getLatestData();
        JuniorData b = getPreviousWeekData();
        if (b.isNullData()) b = a;
        switch(idx) {
        case 0: /* name */
            return new DataPair(DATA_NAME, name, surname, active?COLOR_LIGHT_YELLOW:Color.LIGHT_GRAY ,
                                (getPreviousWeekData().isNullData()&&active ? FONTCOLOR_NEWJUNIOR : null) );
                                // ((data.size()<=1) ? FONTCOLOR_NEWJUNIOR : null) );
        case 1: /* weeks */
            return Short.valueOf( (short)a.getWeeks() );
        case 2: /* Skill */
            return new DataPair( DATA_COMPARABLE_NAMED_SKILL, a.getSkill(), b.getSkill() );
        case 3: /* skillup Count (pops) */
            return Short.valueOf( getSkillupCount() );
        case 4: /* avg weeks per skillup */
            if (getSkillupCount() == 1)
                return new Float( -Math.max(getInitialWeeks()-weekFirstSkillup, weekLastSkillup-a.getWeeks()) );
            else return new Float( getAverageWeeksToSkillup() );
        case 5: /* projected level */
            double avgWks = getAverageWeeksToSkillup();
            int projLevel = -1;
            double realLevel = 0.0;
            //if (avgWks > 0) projLevel = getInitialSkill()+1 + (int)Math.round(weekFirstSkillup / avgWks);
            if (avgWks > 0) {
                realLevel = getInitialSkill()+1 + (weekFirstSkillup / avgWks);
                projLevel = (int)Math.floor( realLevel );
            }
            else if (getSkillupCount() == 0) {
                avgWks = Math.max(getInitialWeeks() - a.getWeeks() + 0.5, JR_BEST_TALENT);
                realLevel = JR_GUESSED_SKILL;
                projLevel = (int)Math.floor( getInitialSkill() + (getInitialWeeks() / avgWks) );
            }
            else {
                avgWks = Math.max(getInitialWeeks() - weekFirstSkillup, weekFirstSkillup - a.getWeeks());
                avgWks = Math.max(avgWks, JR_BEST_TALENT);
                realLevel = JR_ESTIMATED_SKILL;
                projLevel = (int)Math.floor( getInitialSkill()+1 + (weekFirstSkillup / avgWks) );
            }
            return new DataPair( DATA_COMPARABLE_NAMED_SKILL, projLevel, a.getSkill(), realLevel );
        case 6: /* initial weeks */
            return Short.valueOf( (short)getInitialWeeks() );
        case 7: /* initial skill */
            int initialSkill = getInitialSkill();
            return new DataPair( DATA_COMPARABLE_NAMED_SKILL, initialSkill, 2*initialSkill-a.getSkill() );
        case 8: /* weeks since last pop */
            if (weekLastSkillup == JR_NO_SKILLUP_YET) return Short.valueOf( (short)(getInitialWeeks() - a.getWeeks()) );
            return Short.valueOf( (short)(weekLastSkillup - a.getWeeks()) );
        case 9: /* money spent on this junior */
            return Long.valueOf( (getInitialWeeks() - a.getWeeks()) * COST_JR_PLACE );
        case 10: /* formation : 0 - GK ,  1 - player*/
            int _fmt = getFormation();
            if (_fmt != NO_POSITION) _fmt = (_fmt*2 + 1) * 10;
            return Integer.valueOf( _fmt );
        case 11: /* age */
            int _age = getAge();
            if (_age == JR_NO_DATA) return " ";
            return Integer.toString( _age );
        default:
            return "";
        }
    }

    public GraphicableData getGraphicableData() {
        if (weeklyData == null) weeklyData = new WeeklyData(MAX_WEEKS_IN_GRAPH);
        return weeklyData;
    }

    /* ################################## */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ( !obj.getClass().equals(this.getClass()) ) return false;
        JuniorProfile p = (JuniorProfile)obj;
        return (this.id == p.id);
    }
    public int hashCode() { return id; }

    /* Interface Comparable */
    public int compareTo(JuniorProfile obj) throws ClassCastException {
        int w1 = this.getWeeks();
        int w2 = obj.getWeeks();
        if (w1<w2) return -1;
        if (w1>w2) return 1;
        int secondary = surname.compareTo(obj.surname);
        if (secondary != 0) return secondary;
        int tertiary = name.compareTo(obj.name);
        if (tertiary != 0) return tertiary;
        if (this.id > obj.id) return 1;
        else if (this.id < obj.id) return -1;
        return 0;
    }

    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        //! FIX for formation and age REMOVE IN 1.0
        if (_fix095 == false) {
            formation = (byte)NO_POSITION;
            _fix095 = true;
        }

        //!

        //! FIX, REMOVE IN 0.9!!!!!!
//         TreeMap<Date,JuniorData> _datafix = new TreeMap<Date,JuniorData>();
//         Calendar cal = Calendar.getInstance();
//         for (java.util.Map.Entry<Date,JuniorData> par : data.entrySet()) {
//             Date d = par.getKey();
//             JuniorData jd = par.getValue();
//             cal.setTime(d);
//             cal.set(Calendar.SECOND, 0);
//             cal.set(Calendar.MILLISECOND, 0);
//             d = cal.getTime();
//             jd.date = d;
//             _datafix.put(d, jd);
//         }
//         data = _datafix;
        //!
    }



    /**
     *  Class JuniorData
     */
    protected static class JuniorData implements java.io.Serializable {
        private static final long serialVersionUID = -1359430675611413537L;

        private Date date;
        private byte weeks;
        private byte skill;
        private byte age;

        public JuniorData() {
            this(null, (short)0, (short)0, (short)0);
        }
        public JuniorData(Date dt, short we, short sk, short ag) {
            date = dt;
            weeks = (byte)we;
            skill = (byte)sk;
            age = (byte)ag;
        }

        public static JuniorData createNullData() { return new JuniorData(null, (byte)NO_DATA, (byte)NO_DATA, (byte)NO_DATA); }
        public Date getDate() { return date; }
        public short getWeeks() { return (short)weeks; }
        public short getSkill() { return (short)skill; }
        public short getAge()   { return (short)age; }
        public boolean isNullData() { return date==null; }

        /* ************ Interface Serializable ************ */
        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
            //! FIX for formation and age REMOVE IN 1.0
            if (age == 0) age = (byte)JR_NO_DATA;
            //!
        }

    } // end class JuniorData

    // ---------------
    public void debug() {
        System.out.println("# " + getFullName() + " [" + getSkill() + "]  initialSk:" + getInitialSkill() + " iwks:"+getInitialWeeks());
        System.out.println("     wk1pop:" + weekFirstSkillup + "  lastPop:" + weekLastSkillup);
        System.out.println("     avgWK2pop:" + getAverageWeeksToSkillup());
//         JuniorData jx = data.get( data.firstKey() );
//         System.out.println("  $ date:" + jx.getDate().toString());
//         System.out.println("     weeks:" + jx.getWeeks() + " sk:" + jx.getSkill());
        for (JuniorData jd : data.values()) {
            if (!jd.isNullData()) {
                System.out.println("  ¬ date:" + jd.getDate().toString());
                System.out.println("     weeks:" + jd.getWeeks() + " sk:" + jd.getSkill());
            }
            else System.out.println("  & null data");
        }
    }


    /**
     *  Class WeeklyData
     */
    public class WeeklyData implements GraphicableData {
        private int weeksCount;
        private JuniorData [] weekData;

        protected WeeklyData(int weeks) {
            weeksCount = weeks;
            weekData = new JuniorData[weeks];
            buildDataArray();
        }

        public int size() { return weeksCount; }
        public int getFieldsCount() { return 1; }
        public int getFieldScale(int idx) { return 17; }

        public String getTitle() { return getFullName(); }

        public Date getDate(int week) {
            if (week>=weeksCount || weekData[week] == null) return null;
            return weekData[week].getDate();
        }
        public int getData(int week, int idx) {
            if (week>=weeksCount || week<0) return NO_DATA;
            if (weekData[week] == null) return NO_DATA;
            switch(idx) {
            case 0:  return weekData[week].getSkill();
            default: return NO_DATA;
            }
        }

        private void buildDataArray() {
            if (data.isEmpty()) return;
            weekData[0] = data.get( data.lastKey() );
            Date startOfTrainingWeekDate = skCal.setDateToFirstTrainingDay( isActive()?new Date():data.lastKey() );
            for (int i=1; i<weeksCount; i++) {
                SortedMap<Date, JuniorData> auxData = data.headMap( startOfTrainingWeekDate );
                if (auxData.isEmpty()) {
                    weekData[i] = null;
                    weeksCount = i;
                    break;
                }
                weekData[i] = auxData.get( auxData.lastKey() );
                skCal.add(java.util.Calendar.WEEK_OF_YEAR, -1);
                startOfTrainingWeekDate = skCal.getTime();
            }
        }

    }

}
