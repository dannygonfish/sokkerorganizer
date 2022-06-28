package so.data;


public class TableColumnData implements java.io.Serializable {
    private static final long serialVersionUID = 6787145261144270867L;

    private int modelIndex;
    private int width;
    private boolean active;

    public TableColumnData(int m, int w, boolean a) {
        modelIndex = m;
        width = w;
        active = a;
    }
    public TableColumnData(int m, int w) {
        this(m,w,true);
    }
    public int getModelIndex() { return modelIndex; }
    public int getWidth() { return width; }
    public boolean isActive() { return active; }

    public void setWidth(int w) { width = w; }
    public void setActive(boolean a) { active = a; }

    public boolean equals(Object obj) {
        if (obj instanceof TableColumnData) {
            return this.modelIndex == ((TableColumnData)obj).modelIndex;
        }
        return false;
    }

    public int hashCode() {
        return (modelIndex+1)*10 + width*1000 + (active?1:0);
    }

}
