package so.util;

import java.util.GregorianCalendar;
import java.util.Date;
import java.util.TimeZone;

public class SokkerCalendar extends GregorianCalendar {

    public static int initialSeason = 1;
    public static int dayInitialSeason = 6;
    public static int monthInitialSeason = AUGUST;
    public static int yearInitialSeason = 2006;
    public static int TRAINING_DAY = THURSDAY;
    public static int LEAGUE_DAY   = SUNDAY;
    public static int FRIENDLY_DAY = WEDNESDAY;

    private GregorianCalendar auxGreg;

    private static SokkerCalendar SKCAL = new SokkerCalendar();
    private static TimeZone localTZ = TimeZone.getDefault();
    private static TimeZone polskaTZ = TimeZone.getTimeZone("Europe/Warsaw");

    public SokkerCalendar() {
        super();
        setMinimalDaysInFirstWeek(1);
        auxGreg = new GregorianCalendar(yearInitialSeason, monthInitialSeason, dayInitialSeason);
        auxGreg.setMinimalDaysInFirstWeek(1);
    }
    public SokkerCalendar(int y, int m, int d) {
        super(y, m, d);
        setMinimalDaysInFirstWeek(1);
        auxGreg = new GregorianCalendar(yearInitialSeason, monthInitialSeason, dayInitialSeason);
        auxGreg.setMinimalDaysInFirstWeek(1);
    }

    public SokkerCalendar(Date date) {
        this();
        setTime(date);
    }

    private int getTotalWeek() {
        auxGreg.set(yearInitialSeason, monthInitialSeason, dayInitialSeason);
        long adj = (getDaysAfterStartOfWeek(this) - getDaysAfterStartOfWeek(auxGreg)) * 24L * 60L * 60L * 1000L;
        long adj2 = get(HOUR_OF_DAY)*60L*60L*1000L + get(MINUTE)*60L*1000L + get(SECOND)*1000L + get(MILLISECOND);
        long dif = this.getTimeInMillis() - auxGreg.getTimeInMillis() - adj - adj2;
        double weeks = dif/(1000.0*60.0*60.0*24.0*7.0);
        //System.out.print(getTime().toString() +" @  " + Math.ceil(weeks) + " --: ");
        //System.out.println(" "+getDaysAfterStartOfWeek(this)+" / "+getDaysAfterStartOfWeek(auxGreg) +" == " +weeks);
        return (int)Math.ceil(weeks);
    }
    public int getWeekOfSeason() {
        int seasonWeek = getTotalWeek() % 16 + 1;
        if (seasonWeek < 1) seasonWeek += 16;
        return seasonWeek;
    }

    public int getSeason() {
        return (getTotalWeek() + initialSeason*16) / 16;
    }

    private static int getDaysAfterStartOfWeek(java.util.Calendar cal) {
        int days = cal.get(DAY_OF_WEEK) - cal.getFirstDayOfWeek();
        return days<0 ? days+7 : days ;
    }

    public Date setDateToFirstTrainingDay(Date currentTime) {
        Date d = getDateOfFirstTrainingDay(currentTime);
        setTime(d);
        return d;
    }

    public Date getDateOfFirstTrainingDay(Date currentTime) {
        auxGreg.setTime(currentTime);
        int days = auxGreg.get(DAY_OF_WEEK) - TRAINING_DAY;
        if (days<0) days += 7;
        auxGreg.set(AM_PM, AM);
        auxGreg.set(MILLISECOND, 0);
        auxGreg.set(SECOND, 0);
        auxGreg.set(MINUTE, 0);
        auxGreg.set(HOUR, 0);
        auxGreg.add(DAY_OF_MONTH, -days);
        return auxGreg.getTime();
    }

    public boolean isSameTrainingWeekAs(int dayOfMonth) {
        auxGreg.set(AM_PM, AM);
        auxGreg.set(get(YEAR), get(MONTH), dayOfMonth, 0, 0, 0);
        auxGreg.set(MILLISECOND, 0);
        int days = auxGreg.get(DAY_OF_WEEK) - TRAINING_DAY;
        if (days<0) days += 7;
        auxGreg.add(DAY_OF_MONTH, -days);

        days = this.get(DAY_OF_WEEK) - TRAINING_DAY;
        if (days<0) days += 7;
        auxGreg.add(DAY_OF_MONTH, days);

        if (auxGreg.get(DAY_OF_MONTH) != this.get(DAY_OF_MONTH)) return false;
        if (auxGreg.get(MONTH) != this.get(MONTH)) return false;
        if (auxGreg.get(YEAR) != this.get(YEAR)) return false;
        return true;
    }

    public int getMaxDaysInMonth() {
        auxGreg.setTime(this.getTime());
		auxGreg.set(DAY_OF_MONTH, 1);
		auxGreg.add(MONTH, 1);
		auxGreg.add(DATE, -1);
		return auxGreg.get(DATE);
    }

    public int getTrainingWeek(Date date) {
        setDateToFirstTrainingDay(date);
        add(DAY_OF_MONTH, 7);
        return getWeekOfSeason();
    }

    /* =========================================================================== */
    public static Date getDateOfWeeksAgo(int weeksAgo) {
        SKCAL.setTime(new Date());
        SKCAL.add(DAY_OF_MONTH, -7*weeksAgo);
        return SKCAL.getTime();
    }

    public static int getWeeksAgo(Date dateInWeek) {
        long diff = SKCAL.getDateOfFirstTrainingDay(new Date()).getTime();
        diff = diff - SKCAL.getDateOfFirstTrainingDay(dateInWeek).getTime();
        //diff = (diff/1000.0/60.0/60.0/24.0/7.0);
        diff = Math.round(diff/604800000.0);
        return (int)diff;
    }
    public static SokkerCalendar getSharedInstance() { return SKCAL; }

    public static Date normalizeFromPolishTimeZone(Date date) {
        return new Date(date.getTime() - polskaTZ.getOffset(date.getTime()) + localTZ.getOffset(date.getTime()));
    }

}
