package so.data;
/**
 * DataEncapsulator.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.*;
import java.io.*;

public class DataEncapsulator extends AbstractData implements Serializable {
    private static final long serialVersionUID = 2097954719484048051L;

    private TeamDetails teamDetails;
    private PlayerRoster playerRoster;
    private JuniorSchool juniorSchool;
    private Stadium stadium;
    private CoachOffice coachOffice;
    private LineupManager lineupManager;

    public DataEncapsulator() {
        this(FILENAME_TEAMDATA);
    }
    public DataEncapsulator(String filename) {
        super(filename);
        teamDetails = null;
        playerRoster = null;
        juniorSchool = null;
        stadium = null;
        coachOffice = null;
        lineupManager = null;
    }

    public TeamDetails getTeamDetails() {
        if (teamDetails == null) teamDetails = new TeamDetails();
        return teamDetails;
    }
    public PlayerRoster getPlayerRoster() {
        if (playerRoster == null) playerRoster = new PlayerRoster();
        playerRoster.setTeamId( teamDetails.getId() );
        return playerRoster;
    }
    public JuniorSchool getJuniorSchool() {
        if (juniorSchool == null) juniorSchool = new JuniorSchool();
        return juniorSchool;
    }
    public Stadium getStadium() {
        if (stadium == null) stadium = new Stadium();
        return stadium;
    }
    public CoachOffice getCoachOffice() {
        if (coachOffice == null) coachOffice = new CoachOffice();
        return coachOffice;
    }
    public LineupManager getLineupManager() {
        if (lineupManager == null) lineupManager = new LineupManager();
        return lineupManager;
    }

    public void setTeamDetails(TeamDetails td)     { teamDetails = td; }
    public void setPlayerRoster(PlayerRoster pr)   { playerRoster = pr; }
    public void setJuniorSchool(JuniorSchool js)   { juniorSchool = js; }
    public void setStadium(Stadium st)             { stadium = st; }
    public void setCoachOffice(CoachOffice co)     { coachOffice = co; }
    public void setLineupManager(LineupManager lm) { lineupManager = lm; }

    public static DataEncapsulator load() {
        return loadObject( new DataEncapsulator() );
    }
}
