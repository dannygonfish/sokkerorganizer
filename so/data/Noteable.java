package so.data;



public interface Noteable {

    public String getNotesTitle();

    public String getManagerNotes();

    public void setManagerNotes(String notes);

    public boolean hasManagerNotes();

}