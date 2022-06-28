package so.data;

import static so.Constants.*;
import static so.Constants.Positions.*;
import java.awt.Color;

public class DataPair implements Comparable {

    private int type;
    private int value;
    private int secondValue;
    private double decimal;
    private String firstName;
    private String secondName;
    private Color bgColor;
    private Color fgColor;

    // DATA_COMPARABLE_SKILL
    public DataPair(int t, int v, int s, Color c) {
        type = t;
        value = v;
        secondValue = s;
        firstName = "";
        secondName = "";
        bgColor = c;
        fgColor = null;
        decimal = 0.0;
    }
    public DataPair(int t, int v, Color c) {
        this(t, v, v, c);
    }
    // DATA_COMPARABLE_NAMED_SKILL
    // DATA_COMPARABLE_NUMBER
    // DATA_COMPARABLE_CURRENCY
    public DataPair(int t, int v, int s) {
        this(t, v, s, null);
    }
    public DataPair(int t, int v) {
        this(t, v, v, null);
    }
    // DATA_STATUS
    // DATA_POSITION
    public DataPair(int t, int v, double d, Color c) {
        this(t, v, v, c);
        decimal = d;
    }
    public DataPair(int t, int v, double d) {
        this(t, v, d, null);
    }
    // DATA_RATING
    // DATA_COMPARABLE_NAMED_SKILL
    public DataPair(int t, int v, int s, double d) {
        this(t, v, d, null);
        secondValue = s;
    }

    // DATA_POSITION_LONGNAME
    public DataPair(int t, int v, String fn, String sn) {
        type = t;
        value = v;
        firstName = fn;
        secondName = sn;
        secondValue = 0;
        bgColor = null;
        fgColor = null;
        decimal = 0.0;
    }
    // DATA_NAME
    public DataPair(int t, String fn, String sn, Color bgc, Color fgc) {
        type = t;
        firstName = fn;
        secondName = sn;
        value = 0;
        secondValue = 0;
        bgColor = bgc;
        fgColor = fgc;
        decimal = 0.0;
    }
    public DataPair(int t, String fn, String sn, Color bgc) {
        this(t, fn, sn, bgc, null);
    }
    public DataPair(int t, String fn, String sn) {
        this(t, fn, sn, null, null);
    }

    public int getType() { return type; }
    public int getValue() { return value; }
    public int getSecondValue() { return secondValue; }
    public String getFirstName() { return firstName; }
    public String getSecondName() { return secondName; }
    public Color getBackgroundColor() { return bgColor; }
    public Color getForegroundColor() { return fgColor; }
    public double getDecimalValue() { return decimal; }

    public String toString() {
        switch (type) {
        case DATA_POSITION:
            return so.gui.MainFrame.getPositionShortName(value);
        case DATA_POSITION_LONGNAME:
            return firstName + " - " + secondName;
            //return so.gui.MainFrame.getPositionShortName(value) + " - " + so.gui.MainFrame.getPositionLongName(value);
        case DATA_NAME:
            return firstName + " " + secondName;
        case DATA_COMPARABLE_NAMED_SKILL:
            return so.gui.MainFrame.getSkillLevelName(value);
        case DATA_STATUS:
            return Integer.toString(value) + "%" + Integer.toString(secondValue);
        case DATA_RATING:
            return Double.toString(decimal);
        case DATA_COMPARABLE_CURRENCY:
        case DATA_COMPARABLE_NUMBER:
        case DATA_COMPARABLE_SKILL:
        default:
            return Integer.toString(value);
        }
    }

    /* Interface Comparable */
    public int hashCode() {
        return type + value*1000 + secondValue*10 + firstName.hashCode() + secondName.hashCode() + (int)(decimal * 100000);
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        try {
            DataPair dpObj = (DataPair)obj;
            if (type != dpObj.type) return false;
            switch(type) {
            case DATA_NAME:
                return (firstName + secondName).equals( dpObj.firstName + dpObj.secondName );
            case DATA_COMPARABLE_CURRENCY:
            case DATA_COMPARABLE_NUMBER:
            case DATA_COMPARABLE_SKILL:
            case DATA_COMPARABLE_NAMED_SKILL:
            case DATA_STATUS:
            case DATA_POSITION:
                return (value == dpObj.value) && (secondValue == dpObj.secondValue);
            default:
                return this.hashCode() == obj.hashCode();
            }
        } catch (ClassCastException cce) {
            return false;
        }
    }
    public int compareTo(Object obj) throws ClassCastException {
        DataPair dpObj = (DataPair)obj;
        if (type != dpObj.type) return (type < dpObj.type)? -1 : 1;
        switch(type) {
        case DATA_NAME:
            int primary = secondName.compareTo(dpObj.secondName);
            return (primary!=0)? primary : firstName.compareTo(dpObj.firstName);
        case DATA_COMPARABLE_CURRENCY:
        case DATA_COMPARABLE_NUMBER:
        case DATA_COMPARABLE_SKILL:
        case DATA_COMPARABLE_NAMED_SKILL:
            return ((value<dpObj.value)? -10 : ((value>dpObj.value)? 10 :
                   ((secondValue<dpObj.secondValue)? 1 : ((secondValue>dpObj.secondValue)? -1 : 0 )) ));
        case DATA_POSITION:
            int position1 = value & ~(P_LEFT | P_RIGHT | RESERVE);
            int position2 = dpObj.value & ~(P_LEFT | P_RIGHT | RESERVE);
            return ((position1<position2)? -10 : ((position1>position2)? 10 :
                   ((decimal<dpObj.decimal)? -1 : ((decimal>dpObj.decimal)? 1 : 0 )) ));
        case DATA_STATUS:
            return ((secondValue<dpObj.secondValue)? -10 : ((secondValue>dpObj.secondValue)? 10 :
                   ((value<dpObj.value)? -1 : ((value>dpObj.value)? 1 : 0 )) ));
        default:
            return (this.hashCode()>obj.hashCode())? 1 : ((this.hashCode()<obj.hashCode())? -1 : 0 );
        }
    }

}
