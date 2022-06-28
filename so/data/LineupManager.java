package so.data;

import java.util.ArrayList;
import java.util.HashMap;
import static so.Constants.Positions.*;

public class LineupManager implements java.io.Serializable {
    private static final long serialVersionUID = -3704511394518093969L;

    private Lineup [] currentLineups;
    private HashMap<String, Lineup> savedLineups;

    public LineupManager() {
        currentLineups = new Lineup[4];
        savedLineups = new HashMap<String, Lineup>();
    }


    public Lineup getCurrentLineup(int idx) {
        if (idx<0 || idx>3) return null;
        if (currentLineups[idx]==null) currentLineups[idx] = new Lineup();
        return currentLineups[idx];
    }
    public void setCurrentLineup(int idx, Lineup lineup) {
        if (idx<0 || idx>3) return;
        currentLineups[idx] = lineup;
    }

    /* ################################################################################ */
    public static class Lineup implements java.io.Serializable {
        private static final long serialVersionUID = 2472575960531983225L;
        private String name;
        private ArrayList<LineupPosition> lineUp;

        public Lineup() {
            lineUp = new ArrayList<LineupPosition>();
            lineUp.add(new LineupPosition(1, 180, 481, GK) );
            lineUp.add(new LineupPosition(2, 140, 410, CB) );
            lineUp.add(new LineupPosition(3, 220, 390, SW) );
            lineUp.add(new LineupPosition(4, 50, 400, WB) );
            lineUp.add(new LineupPosition(5, 310, 400, WB) );
            lineUp.add(new LineupPosition(6, 120, 260, CM) );
            lineUp.add(new LineupPosition(7, 240, 260, CM) );
            lineUp.add(new LineupPosition(8, 40, 160, WM) );
            lineUp.add(new LineupPosition(9, 320, 160, WM) );
            lineUp.add(new LineupPosition(10, 160, 120, FW) );
            lineUp.add(new LineupPosition(11, 210, 42, ST) );
        }

        public void setName(String n) { name = n; }
        public String getName() { return name; }
        public java.util.List<LineupPosition> getLineup() { return lineUp; }

        public int getPositionForPlayer(int pid) {
            for (LineupPosition lu : lineUp) {
                if (lu.playerId == pid) return lu.playingPosition;
            }
            return NO_POSITION;
        }

    }

    /* ################################################################################ */
    public static class LineupPosition implements java.io.Serializable {
        private static final long serialVersionUID = 5409745436257208229L;

        private int playerId;
        private int centerX;
        private int centerY;
        private int playingPosition;
        private int shirtNumber;

        public LineupPosition(int sn, int x, int y, int pos) {
            shirtNumber = (byte)sn;
            centerX = x;
            centerY = y;
            playingPosition = (short)pos;
            playerId = 0;
        }

        public int getNumber() { return shirtNumber; }
        public int getX() { return centerX; }
        public int getY() { return centerY; }
        public int getPlayingPosition() { return playingPosition; }
        public int getPlayerId() { return playerId; }

        public void setCenter(int x, int y) {
            centerX = x;
            centerY = y;
        }
        public void setCenter(java.awt.geom.Point2D p) {
            centerX = (int)p.getX();
            centerY = (int)p.getY();
        }
        public void setPlayingPosition(int pos) { playingPosition = (short)pos; }
        public void setPlayerId(int pid) { playerId = pid; }
    }

}
