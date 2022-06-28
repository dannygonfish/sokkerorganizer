package so.data;
/**
 * CoachOffice.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import so.util.SokkerCalendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.Calendar; //! REMOVE with FIX 0.9
import java.util.List;
import java.util.ArrayList;

public class CoachOffice implements java.io.Serializable {
    private static final long serialVersionUID = -3912970306106190306L;

    public static final String OLD_JT_HEAD      = "head";
    public static final String OLD_JT_JUNIORS   = "juniors";
    public static final String OLD_JT_ASSISTANT = "assistant";
    public static final String OLD_JT_NONE      = "none";
    public static final String [] JOBS = { OLD_JT_NONE, OLD_JT_HEAD, OLD_JT_ASSISTANT, OLD_JT_JUNIORS };

    private TreeMap<Integer,CoachProfile> coachRoster;
//     private TreeMap<Date,TrainingData> data;
    private TreeMap<Date,TrainingData2> data2;
    transient private TrainingData2 latestData;
    transient private TrainingData2 previousToLatestData;

    transient private ArrayList<CoachProfile> currentTrainersList;
    transient private WeeklyData weeklyData;

    private static SokkerCalendar skCal = new SokkerCalendar();
    private static final int [] trTypeMap = {0,5,9,11,8,7,10,12,6};

    public CoachOffice() {
        coachRoster = new TreeMap<Integer,CoachProfile>();
//         data = null;
        data2 = new TreeMap<Date,TrainingData2>();
        currentTrainersList = null;
        TrainingData2 td = new TrainingData2(new Date());
        td.setTraining(NO_DATA, NO_DATA);
        data2.put(td.getDate(), td);
        weeklyData = null;
    }

    public boolean updateTraining(int type, int pos, Date updatingDate) {
        return updateTraining(type, pos, updatingDate, false);
    }
    public boolean updateTraining(int type, int pos, Date updatingDate, boolean manuallySet) {
        // see if the exact date is already in data
        if (data2.containsKey( updatingDate )) data2.get(updatingDate).setTraining(type, pos);
        else {
            SortedMap<Date,TrainingData2> _map = data2.headMap( updatingDate );
            // if not see whether there are no previous dates
            if (_map.isEmpty()) {
                TrainingData2 td = new TrainingData2(updatingDate);
                td.setTraining(type, pos);
                data2.put(updatingDate, td);
                updateLatestData();
            }
            // there are previous dates: check the previous data
            else {
                TrainingData2 td = _map.get( _map.lastKey() );
                if (td.getTrainingType()==NO_DATA && !manuallySet) td.setTraining(type, pos);
                else if (td.getTrainingType()!=type || td.getTrainingPos()!=pos) {
                    td = new TrainingData2(td);
                    td.setTraining(type, pos);
                    td.setDate(updatingDate);
                    data2.put(updatingDate, td);
                    updateLatestData();
                }
            }
        }
        weeklyData = null;
        return true;
    }

    public boolean updateCoaches(Set<CoachProfile> coaches, Date updatingDate) {
        if (coaches == null) return false;
        TrainingData2 td = new TrainingData2(updatingDate);
        for (CoachProfile cp : coaches) {
            coachRoster.put(cp.id, cp);
            td.addCoach(cp.id, cp.jobType);
        }
        if (data2.containsKey( updatingDate )) {
            td.setTraining(data2.get(updatingDate));
            data2.put(updatingDate, td);
            updateLatestData();
        }
        else {
            SortedMap<Date,TrainingData2> _map = data2.headMap( updatingDate );
            if (_map.isEmpty()) {
                data2.put(updatingDate, td);
                updateLatestData();
            }
            else {
                TrainingData2 _td = _map.get( _map.lastKey() );
                if (!_td.hasSameCoaches(td)) {
                    td.setTraining(_td);
                    data2.put(updatingDate, td);
                    updateLatestData();
                }
                else return false;
            }
        }
        weeklyData = null;
        return true;
    }

    public TrainingData2 getTrainingData(Date date) {
        if (data2.isEmpty()) return null;
        if (date==null) return getLatestData();
        skCal.setDateToFirstTrainingDay(date);
        skCal.add(java.util.Calendar.DAY_OF_MONTH, 7);
        /* resulting date will be at next training day at 00:00:00.000 */
        Date upperLimit = skCal.getTime();
        SortedMap<Date,TrainingData2> _map = data2.headMap( upperLimit );
        if (_map.isEmpty()) return data2.get(data2.firstKey());
        else return _map.get( _map.lastKey() );
    }

    public Set<Date> getDates() { return data2.keySet(); }
    public boolean hasData() { return !data2.isEmpty(); }

    //public Date getDate() { return (latestData==null) ? null : latestData.getDate(); }
//     public GraphicableData getGraphicableData() {
//         if (weeklyData == null) weeklyData = new WeeklyData();
//         return weeklyData;
//     }
    public WeeklyData getWeeklyData() {
        if (weeklyData == null) weeklyData = new WeeklyData();
        return weeklyData;
    }

    /* **************************************************************** */
    protected TrainingData2 getLatestData() {
        if (data2.size()==0) return null;
        if (latestData == null) updateLatestData();
        return latestData;
    }
    protected TrainingData2 getPreviousToLatestData() {
        if (previousToLatestData == null) updateLatestData();
        return previousToLatestData;
    }
    private void updateLatestData() {
        if (data2.size()==0) latestData = null;
        else latestData = data2.get(data2.lastKey());
        if (data2.size()<=1) previousToLatestData = latestData;
        else previousToLatestData = data2.get( data2.headMap( data2.lastKey() ).lastKey() );
    }
    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        //! FIX, REMOVE IN 0.9!!!!!!
//         if (data!=null) {
//             TreeMap<Date,TrainingData2> _datafix = new TreeMap<Date,TrainingData2>();
//             Calendar cal = Calendar.getInstance();
//             for (java.util.Map.Entry<Date,TrainingData> par : data.entrySet()) {
//                 Date d = par.getKey();
//                 TrainingData td = par.getValue();
//                 cal.setTime(d);
//                 cal.set(Calendar.SECOND, 0);
//                 cal.set(Calendar.MILLISECOND, 0);
//                 d = cal.getTime();
//                 td.setDate(d);
//                 _datafix.put(d, new TrainingData2(td));
//             }
//             data2 = _datafix;
//             data = null;
//         }
        //!
        updateLatestData();
    }
    /* **************************************************************** */
    /* **************************************************************** */

    /**
     *  Class TrainingData
     */
    public class TrainingData2 implements java.io.Serializable {
        private static final long serialVersionUID = 5014805856942721497L;

        private Date date;
        private short trainingType; // 1 - 8
        private short trainingPos;  // 0 - 3
        private int headCoachID;
        private int juniorCoachID;
        private HashSet<Integer> assistantCoachIDs;
        private HashSet<Integer> idleCoachIDs;

        transient private ArrayList<CoachProfile> trainersList;

        public TrainingData2(Date d) {
            date = d;
            trainingType = NO_DATA;
            trainingPos = NO_DATA;
            headCoachID = NO_DATA;
            juniorCoachID = NO_DATA;
            assistantCoachIDs = new HashSet<Integer>(4);
            idleCoachIDs = new HashSet<Integer>(4);
            trainersList = null;
        }
        public TrainingData2(TrainingData2 td) {
            date = td.date;
            trainingType = td.trainingType;
            trainingPos = td.trainingPos;
            headCoachID = td.headCoachID;
            juniorCoachID = td.juniorCoachID;
            assistantCoachIDs = new HashSet<Integer>(td.assistantCoachIDs);
            idleCoachIDs = new HashSet<Integer>(td.idleCoachIDs);
            trainersList = null;
        }
        // REMOVE in 0.9 !!!
//         public TrainingData2(TrainingData td1) {
//             date = td1.date;
//             trainingType = td1.trainingType;
//             trainingPos = td1.trainingPos;
//             headCoachID = td1.headCoachID;
//             juniorCoachID = td1.juniorCoachID;
//             assistantCoachIDs = new HashSet<Integer>(td1.assistantCoachIDs);
//             idleCoachIDs = new HashSet<Integer>(td1.idleCoachIDs);
//             trainersList = null;
//         } // end REMOVE

        public Date getDate() { return date; }
        private void setDate(Date d) { date = d; }
        public int getTrainingType() { return trainingType; }
        public int getTrainingPos() { return trainingPos; }

        public boolean hasSameCoaches(TrainingData2 td) {
            if (headCoachID!=td.headCoachID || juniorCoachID!=td.juniorCoachID) return false;
            if (!assistantCoachIDs.equals(td.assistantCoachIDs)) return false;
            if (!idleCoachIDs.equals(td.idleCoachIDs)) return false;
            return true;
        }

        public void addCoach(int id, int job) {
            switch (job) {
            case JOB_HEAD: headCoachID = id; break;
            case JOB_JUNIORS: juniorCoachID = id; break;
            case JOB_ASSISTANT: assistantCoachIDs.add(id); break;
            default: idleCoachIDs.add(id);
            }
            trainersList = null;
        }

        private void setTraining(int type, int pos) {
            trainingType = (short)type;
            trainingPos = (short)pos;
            weeklyData = null;
        }
        private void setTraining(TrainingData2 td) {
            trainingType = td.trainingType;
            trainingPos = td.trainingPos;
            weeklyData = null;
        }
        public List<CoachProfile> getTrainers() {
            if (trainersList == null) {
                trainersList = new ArrayList<CoachProfile>(8);
                if (coachRoster.containsKey(headCoachID)) trainersList.add(coachRoster.get(headCoachID));
                for (int id : this.assistantCoachIDs) {
                    if (coachRoster.containsKey(id)) trainersList.add(coachRoster.get(id));
                }
                if (coachRoster.containsKey(juniorCoachID)) trainersList.add(coachRoster.get(juniorCoachID));
                for (int id : this.idleCoachIDs) {
                    if (coachRoster.containsKey(id)) trainersList.add(coachRoster.get(id));
                }
            }
            return trainersList;
        }

        public int getCoachJob(int coachId) {
            if (coachId == headCoachID) return JOB_HEAD;
            else if (coachId == juniorCoachID) return JOB_JUNIORS;
            else if (assistantCoachIDs.contains(coachId)) return JOB_ASSISTANT;
            else return JOB_IDLE;
        }

        public int getTrainersWage() {
            List<CoachProfile> l = getTrainers();
            int wage = 0;
            if (l != null) {
                for (CoachProfile cp : l) wage += cp.salary;
            }
            return wage;
        }

        public int getHeadCoachSkillLevelForCurrentTraining() {
            return getHeadCoachSkillLevel(trainingType);
        }
        public int getHeadCoachSkillLevel(int skill) {
            if (!coachRoster.containsKey(headCoachID) || trainingType==NO_DATA) return NO_DATA;
            if (skill<1 || skill>=trTypeMap.length) return NO_DATA;
            Object aux = coachRoster.get( headCoachID ).getData( trTypeMap[ skill ] );
            if (aux instanceof Integer) return ((Integer)aux).intValue();
            else return NO_DATA;
        }

        public int getAssistantsLevelSum() {
            int sum = 0;
            for (int cid : assistantCoachIDs) {
                if (coachRoster.containsKey( cid )) sum += coachRoster.get( cid ).generalSkill;
            }
            return sum;
        }

    }

    /* **************************************************************** */
    /**
     *  Class CoachProfile
     */
    public static class CoachProfile implements java.io.Serializable {
        private static final long serialVersionUID = -3044040976014357395L;

        private int id;
        private String name;
        private String surname;
        private byte  jobType;
        private short country;
        private short age;
        private int   salary;
        private short generalSkill;
        private short stamina;
        private short pace;
        private short technique;
        private short passing;
        private short keepers;
        private short defenders;
        private short playmakers;
        private short scorers;

        /* old parsing, job is a String */
        public CoachProfile(int i, String n, String sn, String j, short cy, short ag, int sy, short gs,
                            short st, short pc, short te, short ps, short kp, short df, short pm, short sc) {
            this(i, n, sn, 0, cy, ag, sy, gs, st, pc, te, ps, kp, df, pm, sc);
            if (j.equals(OLD_JT_HEAD)) jobType = JOB_HEAD;
            else if (j.equals(OLD_JT_JUNIORS)) jobType = JOB_JUNIORS;
            else if (j.equals(OLD_JT_ASSISTANT)) jobType = JOB_ASSISTANT;
            else jobType = JOB_IDLE;
        }

        /* new Parsing, job is a byte */
        public CoachProfile(int i, String n, String sn, int j, short cy, short ag, int sy, short gs,
                            short st, short pc, short te, short ps, short kp, short df, short pm, short sc) {
            id = i;
            name = n;
            surname = sn;
            jobType = (byte)j;
            country = cy;
            age = ag;
            salary = sy;
            generalSkill = gs;
            stamina = st;
            pace = pc;
            technique = te;
            passing = ps;
            keepers = kp;
            defenders = df;
            playmakers = pm;
            scorers = sc;
        }

        public int getId() { return id; }
        public int getJobType() { return jobType; }

        public Object getData(int idx) {
            switch(idx) {
                //case  0: return Byte.valueOf(jobType);
            case  0: return Integer.valueOf(id);
            case  1: return name + ' ' + surname;
            case  2: return so.gui.MainFrame.getFlagIcon(country);
            case  3: return Integer.valueOf(age);
            case  4: return Integer.valueOf(generalSkill);
            case  5: return Integer.valueOf(stamina);
            case  6: return Integer.valueOf(pace);
            case  7: return Integer.valueOf(technique);
            case  8: return Integer.valueOf(passing);
            case  9: return Integer.valueOf(keepers);
            case 10: return Integer.valueOf(defenders);
            case 11: return Integer.valueOf(playmakers);
            case 12: return Integer.valueOf(scorers);
            case 13: return Long.valueOf(salary);
            default: return " ";
            }
        }

        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof CoachProfile)) return false;
            return this.id == ((CoachProfile)obj).id;
        }

        public int hashCode() {
            return id;
        }

    }


    /**
     *  Class WeeklyData
     */
    public class WeeklyData {
        private int weeksCount;
        private TrainingData2 [] weekData;

        protected WeeklyData() {
            weeksCount = 0;
            weekData = null;
            buildDataArray();
        }

//         public final int getFieldsCount() { return 3; }
//         public final int getFieldScale(int idx) { return 17; }
//         public String getTitle() { return "TRAINING"; }
        public int size() { return weeksCount; }

        public Date getDate(int week) {
            if (week>=weeksCount || weekData[week] == null) return null;
            return weekData[week].getDate();
        }
        public int getData(int week, int idx, int option) {
            if (week>=weeksCount || week<0) return NO_DATA;
            if (weekData[week] == null) return NO_DATA;
            switch(idx) {
            case 0:  return weekData[week].getTrainingType();
            case 1:  return weekData[week].getTrainingPos();
            case 2:  return weekData[week].getHeadCoachSkillLevelForCurrentTraining();
            case 3:  return weekData[week].getHeadCoachSkillLevel(option);
            case 4:  return weekData[week].getAssistantsLevelSum();
            default: return NO_DATA;
            }
        }

        private void buildDataArray() {
            if (data2.isEmpty()) return;
            ArrayList<TrainingData2> _wd = new ArrayList<TrainingData2>();
            _wd.add( data2.get( data2.lastKey() ) );
            Date startOfTrainingWeekDate = skCal.setDateToFirstTrainingDay( new Date() );
            SortedMap<Date, TrainingData2> auxData = data2.headMap( startOfTrainingWeekDate );
            for ( ; !auxData.isEmpty() ; auxData = data2.headMap(startOfTrainingWeekDate) ) {
                _wd.add( auxData.get( auxData.lastKey() ) );
                skCal.add(java.util.Calendar.WEEK_OF_YEAR, -1);
                startOfTrainingWeekDate = skCal.getTime();
            }
            weeksCount = _wd.size();
            weekData = new TrainingData2[weeksCount];
            _wd.toArray(weekData);
        }

    }

}
