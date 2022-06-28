package so.util;

import java.util.Date;

public class SokkerWeek implements java.io.Serializable {
    private static final long serialVersionUID = 50663764137947L;
    private static SokkerCalendar skCal = new SokkerCalendar();

    private int season;
    private int weekOfSeason;
    /* inclusive */
    private Date beginDate;
    private Date endDateIncl;
    /* exclusive */
    private Date endDate;

    public SokkerWeek(Date begin, Date end) {
        beginDate = begin;
        endDate = end;
        endDateIncl = new Date( endDate.getTime()-60000 );
        skCal.setTime(endDate);
        season = skCal.getSeason();
        weekOfSeason = skCal.getWeekOfSeason();
    }
    public SokkerWeek(Date inweek) {
        beginDate = skCal.setDateToFirstTrainingDay(inweek);
        skCal.add(SokkerCalendar.DAY_OF_MONTH, 7);
        endDate = skCal.getTime();
        skCal.add(SokkerCalendar.MINUTE, -1);
        endDateIncl = skCal.getTime();
        season = skCal.getSeason();
        weekOfSeason = skCal.getWeekOfSeason();
    }

    public Date getBeginDate() { return beginDate; }
    public Date getEndDate() { return endDate; }
    public Date getEndDateInclusive() { return endDateIncl; }
    public int getSeason() { return season; }
    public int getWeekOfSeason() { return weekOfSeason; }

    public boolean contains(Date date) {
        return (date.after(beginDate) && date.before(endDate)) || date.equals(beginDate);
    }


}
