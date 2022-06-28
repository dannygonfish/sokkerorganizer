package so.util;

import java.util.Date;
import java.text.DateFormat;

public class FormattedDateHolder {
    private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);

    private Date date;

    public FormattedDateHolder(Date d) {
        date = d;
    }

    public Date getDate() { return date; }
    public String toString() { return dateFormat.format(date); }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if ( !obj.getClass().equals(this.getClass()) ) return false;
        FormattedDateHolder fdh = (FormattedDateHolder)obj;
        return (this.date.equals(fdh.date));
    }
    public int hashCode() { return date.hashCode(); }

}
