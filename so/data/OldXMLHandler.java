package so.data;

import static so.Constants.*;
import static so.Constants.Labels.*;
import so.util.DebugFrame;
import java.util.HashSet;
import java.util.Date;
import org.w3c.dom.*;

public class OldXMLHandler {
    private static int teamId = 0;

    public static int parseTeam(Document doc, Date date, TeamDetails td) {
        Node team = doc.getElementsByTagName("team").item(0);
        int id = Integer.parseInt( team.getAttributes().getNamedItem("id").getNodeValue() );
        Element eteam = (Element)team;
        String name = getTagValue(eteam, "name");
        int countryId = Integer.parseInt( getAttributeValue(eteam, "country", "id") );
        int regionId = Integer.parseInt( getAttributeValue(eteam, "region", "id") );
        //String regionName = getTagValue(eteam, "region");
        long money = Long.parseLong( getTagValue(eteam, "money") );
        int fans = Integer.parseInt( getTagValue(eteam, "fanclubcount") );
        int fanClubMood = Integer.parseInt( getTagValue(eteam, "fanclubmood") );
        teamId = id;
        if (td.updateTeamDetails(date, id, countryId, name, regionId, 0, new Date(0), 0,0,0,0,0,0)) {
            td.addTeamData(date, money, fans, fanClubMood, 0f);
            return PARSE_TEAM;
        }
        return PARSE_FAILED;
    }

    public static int parsePlayers(Document doc, Date date, PlayerRoster roster) {
        HashSet<PlayerProfile> parsedPlayers = new HashSet<PlayerProfile>();
        NodeList players = doc.getElementsByTagName("player");
        for (int i=0, N=players.getLength(); i<N; i++) {
            Node pnode = players.item(i);
            int id = Integer.parseInt( pnode.getAttributes().getNamedItem("id").getNodeValue() );
            if (pnode.getNodeType() == Node.ELEMENT_NODE) {
                Element pelement = (Element)pnode;
                String name    = getTagValue(pelement, "name");
                String surname = getTagValue(pelement, "surname");
                short country  = Short.parseShort( getAttributeValue(pelement, "countryfrom", "id") );
                int vl   = Integer.parseInt( getTagValue(pelement, "value") );
                int sy   = Integer.parseInt( getTagValue(pelement, "salary") );
                short ag = Short.parseShort( getTagValue(pelement, "age") );
                short ms = Short.parseShort( getTagValue(pelement, "matches") );
                short go = Short.parseShort( getTagValue(pelement, "goals") );
                short as = Short.parseShort( getTagValue(pelement, "assists") );
                short fm = Short.parseShort( getTagValue(pelement, "form") );
                short st = Short.parseShort( getTagValue(pelement, "stamina") );
                short pc = Short.parseShort( getTagValue(pelement, "pace") );
                short te = Short.parseShort( getTagValue(pelement, "technique") );
                short ps = Short.parseShort( getTagValue(pelement, "passing") );
                short kp = Short.parseShort( getTagValue(pelement, "keeper") );
                short df = Short.parseShort( getTagValue(pelement, "defender") );
                short pm = Short.parseShort( getTagValue(pelement, "playmaker") );
                short sc = Short.parseShort( getTagValue(pelement, "scorer") );
                short cd = Short.parseShort( getTagValue(pelement, "cards") );
                int in = Math.round( Float.parseFloat( getTagValue(pelement, "injurydays") ) );
                PlayerProfile player = new PlayerProfile(id, name, surname, country, teamId, 0);
                player.addPlayerData(date, vl, sy, ag, ms, go, as, fm, st, pc, te, ps, kp, df, pm, sc, cd, in, 0, 0, 0, false, false, 0, 0, 0, 0);
                parsedPlayers.add(player);
            }
        } //end for
        roster.updatePlayers( parsedPlayers , date );
        return PARSE_PLAYERS;
    }

public static int parseJuniors(Document doc, Date date, JuniorSchool school) {
        HashSet<JuniorProfile> parsedJuniors = new HashSet<JuniorProfile>();
        NodeList juniors = doc.getElementsByTagName("junior");
        for (int i=0, N=juniors.getLength(); i<N; i++) {
            Node jnode = null;
            try {
                jnode = juniors.item(i);
                int id = Integer.parseInt( jnode.getAttributes().getNamedItem("id").getNodeValue() );
                if (jnode.getNodeType() == Node.ELEMENT_NODE) {
                    Element jelement = (Element)jnode;
                    String name    = getTagValue(jelement, "name");
                    String surname = getTagValue(jelement, "surname");
                    short weeks = Short.parseShort( getTagValue(jelement, "weeks") );
                    short skill = Short.parseShort( getTagValue(jelement, "skill") );
                    JuniorProfile junior = new JuniorProfile(id, name, surname);
                    junior.addJuniorData(date, weeks, skill);
                    parsedJuniors.add(junior);
                }
            } catch (Exception e) {
                System.err.println ( jnode.getNodeName() );
                e.printStackTrace();
                continue;
            }
        } //end for
        school.updateJuniors( parsedJuniors, date );
        return PARSE_JUNIORS;
    }

//     protected boolean parseStadium(Document doc, Date date) {
//         Node arena = doc.getElementsByTagName("arena").item(0);
//         if (arena == null) return false;
//         if (arena.getNodeType() != Node.ELEMENT_NODE) return false;
//         Element e_arena = (Element)arena;
//         String name = getTagValue(e_arena, "name");
//         parsedStadium = new Stadium(name);
//         Stadium.StadiumData stadiumData = new Stadium.StadiumData(date);
//         NodeList standNodes = e_arena.getElementsByTagName("stand");
//         for (int i=0, N=standNodes.getLength(); i<N; i++) {
//             Node snode = standNodes.item(i);
//             String location = snode.getAttributes().getNamedItem("location").getNodeValue();
//             if (snode.getNodeType() == Node.ELEMENT_NODE) {
//                 Element selement = (Element)snode;
//                 int capacity = Integer.parseInt( getTagValue(selement, "capacity") );
//                 short type = Short.parseShort( getTagValue(selement, "type") );
//                 short _sroof = Short.parseShort( getTagValue(selement, "roof") );
//                 boolean roof = (_sroof == 1);
//                 float days = Float.parseFloat( getTagValue(selement, "days") );
//                 stadiumData.addStand(location, capacity, type, days, roof);
//             }
//         }
//         parsedStadium.addStadiumData(date, stadiumData);
//         return true;
//     }

    public static int parseCoaches(Document doc, Date date, CoachOffice office) {
        HashSet<CoachOffice.CoachProfile> parsedCoaches = new HashSet<CoachOffice.CoachProfile>();
        try {
            NodeList coaches = doc.getElementsByTagName("coach");
            for (int i=0, N=coaches.getLength(); i<N; i++) {
                Node cnode = coaches.item(i);
                int id = Integer.parseInt( cnode.getAttributes().getNamedItem("id").getNodeValue() );
                //int _signed = Integer.parseInt( cnode.getAttributes().getNamedItem("signed").getNodeValue() );
                if (cnode.getNodeType() == Node.ELEMENT_NODE) {
                    Element celement = (Element)cnode;
                    String name    = getTagValue(celement, "name");
                    String surname = getTagValue(celement, "surname");
                    String job = getTagValue(celement, "job");
                    short country  = Short.parseShort( getAttributeValue(celement, "countryfrom", "id") );
                    short age = Short.parseShort( getTagValue(celement, "age") );
                    int salary = Integer.parseInt( getTagValue(celement, "salary") );
                    short generalskill = Short.parseShort( getTagValue(celement, "generalskill") );
                    short stamina    = Short.parseShort( getTagValue(celement, "stamina") );
                    short pace       = Short.parseShort( getTagValue(celement, "pace") );
                    short technique  = Short.parseShort( getTagValue(celement, "technique") );
                    short passing    = Short.parseShort( getTagValue(celement, "passing") );
                    short keepers    = Short.parseShort( getTagValue(celement, "keepers") );
                    short defenders  = Short.parseShort( getTagValue(celement, "defenters") );
                    short playmakers = Short.parseShort( getTagValue(celement, "playmakers") );
                    short scorers    = Short.parseShort( getTagValue(celement, "scorers") );
                    parsedCoaches.add( new CoachOffice.CoachProfile(id, name, surname, job, country, age, salary,
                                                                    generalskill, stamina, pace, technique, passing,
                                                                    keepers, defenders, playmakers, scorers) );
                }
            } //end for
        } catch (Exception ex) {
            ex.printStackTrace();
            return PARSE_FAILED;
        }
        office.updateCoaches(parsedCoaches, date);
        return PARSE_TRAINING;
    }

    private static String getTagValue(Element ele, String tag) {
        try {
            return ele.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
        } catch (NullPointerException npe) {
            return "";
        }
    }
    private static String getAttributeValue(Element ele, String tag, String attr) {
        return ele.getElementsByTagName(tag).item(0).getAttributes().getNamedItem(attr).getNodeValue();
    }

}
