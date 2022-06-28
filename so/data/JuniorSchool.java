package so.data;
/**
 * JuniorSchool.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;

public class JuniorSchool implements java.io.Serializable {
    private static final long serialVersionUID = -6814090821957567378L;

    private int teamid;
    private TreeMap<Integer, JuniorProfile> allJuniors;
    transient private ArrayList<JuniorProfile> activeJuniors;
    transient private ArrayList<JuniorProfile> formerJuniors;

    public JuniorSchool(int id) {
        teamid = id;
        allJuniors = new TreeMap<Integer, JuniorProfile>();
        activeJuniors = new ArrayList<JuniorProfile>();
        formerJuniors = new ArrayList<JuniorProfile>();
    }
    public JuniorSchool() {
        this(TEAMID_NO_TEAM);
    }

    public int getJuniorsCount() { return activeJuniors.size(); }
    public ArrayList<JuniorProfile> getJuniorsList() { return activeJuniors; }
    public ArrayList<JuniorProfile> getFormerJuniorsList() { return formerJuniors; }

    public void deleteJunior(JuniorProfile junior) {
        formerJuniors.remove(junior);
        activeJuniors.remove(junior);
        allJuniors.remove(Integer.valueOf(junior.getId()));
    }

    public boolean updateJuniors(Set<JuniorProfile> juniors, Date updatingJuniorsDate) {
        if (juniors==null) return false;
        Date currentJuniorsDate = null;
        if (activeJuniors.size() > 0) currentJuniorsDate = activeJuniors.get(0).getCurrentDate();
        boolean isThisNewUpdate = currentJuniorsDate==null || updatingJuniorsDate.compareTo(currentJuniorsDate)>=0;
        for (JuniorProfile j : juniors) {
            int id = j.getId();
            if (allJuniors.containsKey(id)) {
                JuniorProfile junior = allJuniors.get(id);
                junior.addJuniorData(j);
                if ( isThisNewUpdate && !junior.isActive() ) {
                    junior.setActive(true);
                    formerJuniors.remove(junior);
                    activeJuniors.add(junior);
                }
            }
            else {
                // do not add him if he is not in the latest set of juniors and this is not a real update
                // i.e. if his Date is previous than the latest Date of the current juniors and this is not a real update
                // updatingJuniorsDate = j.getCurrentDate(); ??? está de más ???
                allJuniors.put(id, j);
                if (isThisNewUpdate) {
                    j.setActive(true);
                    activeJuniors.add(j);
                }
                else formerJuniors.add(j);
            }
        }
        // players not updated are not active anymore
        // !!! only if this is really an update, not an older insertion
        if (isThisNewUpdate) {
            JuniorProfile [] auxArray = new JuniorProfile[activeJuniors.size()];
            auxArray = activeJuniors.toArray(auxArray);
            for (int i=0; i<auxArray.length; i++) {
                if ( !juniors.contains(auxArray[i]) ) {  // equals
                    activeJuniors.remove(auxArray[i]);
                    auxArray[i].setActive(false);
                    formerJuniors.add(auxArray[i]);
                }
            }
        }
        Collections.sort(activeJuniors);
        Collections.sort(formerJuniors);
        return true;
    }

    /* ************ Interface Serializable ************ */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        activeJuniors = new ArrayList<JuniorProfile>();
        formerJuniors = new ArrayList<JuniorProfile>();
        for (Map.Entry<Integer, JuniorProfile> junior : allJuniors.entrySet()) {
            if (junior.getValue().isActive()) activeJuniors.add( junior.getValue() );
            else formerJuniors.add( junior.getValue() );
        }
        Collections.sort(activeJuniors);
        Collections.sort(formerJuniors);
    }

}
