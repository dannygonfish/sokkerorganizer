package so.util;

import java.util.Date;

public class Utils {

    public static int getIntFromString(String str, int offset) {
        if (offset<0) return -1;
        try {
            char c;
            StringBuilder _sub = new StringBuilder();
            boolean firstChar = true;
            for (int i = offset; i<str.length(); i++) {
                c = str.charAt(i);
                if (firstChar && c=='-') _sub.append(c);
                if (Character.isDigit(c)) {
                    _sub.append(c);
                    firstChar = false;
                }
                else if (Character.isSpaceChar(c)) continue;
                else break;
            }
            if (_sub.length() == 0) return -1;
            return Integer.parseInt( _sub.toString() );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static long getLongFromString(String str, int offset) {
        if (offset<0) return -1;
        try {
            char c;
            StringBuilder _sub = new StringBuilder();
            boolean firstChar = true;
            for (int i = offset; i<str.length(); i++) {
                c = str.charAt(i);
                if (firstChar && c=='-') _sub.append(c);
                if (Character.isDigit(c)) {
                    _sub.append(c);
                    firstChar = false;
                }
                else if (Character.isSpaceChar(c)) continue;
                else break;
            }
            if (_sub.length() == 0) return -1;
            return Long.parseLong( _sub.toString() );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /* ========== VISUALS ==========*/

    public static void setJTableVisibleRowCount(javax.swing.JTable table, int rows){ 
        table.setPreferredScrollableViewportSize(new java.awt.Dimension( 
                table.getPreferredScrollableViewportSize().width, 
                rows*table.getRowHeight() )
        ); 
    }

}
