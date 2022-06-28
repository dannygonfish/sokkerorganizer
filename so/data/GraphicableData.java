package so.data;

public interface GraphicableData {

    /* returns the number of data for each field */
    public int size();

    /* returns the number of fields that have curves drawn in the Graph */
    public int getFieldsCount();

    /* returns the scale of the Y-axis for field "idx" */
    public int getFieldScale(int idx);

    /* returns the date of week "week". Week represents weeks ago */
    public java.util.Date getDate(int week);

    /* returns the data for field "idx" on week "week" */
    public int getData(int week, int idx);

    /* returns the title of the graph */
    public String getTitle();

}
