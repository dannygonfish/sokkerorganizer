package so.data;

public class Score implements java.io.Serializable, Comparable<Score> {
    private static final long serialVersionUID = -923455302092348490L;

    private byte local;
    private byte visitor;

    public Score() {
        local = 0;
        visitor = 0;
    }

    public Score(int l, int v) {
        if (l<0) l = 0;
        if (v<0) v = 0;
        local = (byte)l;
        visitor = (byte)v;
    }

    public int getLocal() { return (int)local; }
    public int getVisitor() { return (int)visitor; }
    public int getDifference() { return (int)local - (int)visitor; }

    //public String toString() { return '(' + Byte.toString(local) + ':' + Byte.toString(visitor) + ')'; }
    public String toString() { return Byte.toString(local) + " : " + Byte.toString(visitor); }


    /* Interface Comparable */
    public int hashCode() {
        return (int)local*1000 + (int)visitor;
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Score)) return false;
        Score sc2 = (Score)obj;
        return local==sc2.local && visitor==sc2.visitor;
    }
    public int compareTo(Score sc2) {
        int dif1 = this.getDifference();
        int dif2 = sc2.getDifference();
        if (dif1 == dif2) return (this.local==sc2.local)? 0 : ( this.local<sc2.local ? -1 : 1 );
        else return (dif1 < dif2)? -1 : 1;
    }
}
