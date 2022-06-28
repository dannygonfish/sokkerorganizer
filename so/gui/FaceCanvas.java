package so.gui;

import static so.Constants.Positions.*;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class FaceCanvas extends javax.swing.JPanel {

    public static final String SUBDIR_BASE   = "/base/";
    public static final String SUBDIR_BEARDS = "/beards/";
    public static final String SUBDIR_EYES   = "/eyes/";
    public static final String SUBDIR_MOUTHS = "/mouths/";
    public static final String SUBDIR_NOSES  = "/noses/";

    private int code;
    private ImageIcon fondo;
    private ImageIcon cara;
    private ImageIcon barba;
    private ImageIcon ojos;
    private ImageIcon boca;
    private ImageIcon nariz;
    private BufferedImage playerFace;

    public FaceCanvas() {
        super();
        Dimension d = new Dimension(47, 49);
        setSize(d);
        setMaximumSize(d);
        setMinimumSize(d);
        setPreferredSize(d);
        fondo  = so.gui.MainFrame.getImageIcon(so.Constants.DIRNAME_FACES + "/back.png");
        cara  = null;
        barba = null;
        ojos  = null;
        boca  = null;
        nariz = null;
        code = 0;
        playerFace = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        buildFace();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(playerFace, 0, 0, null);
    }

    public void clear() {
        setBack(0);
        setCode(0,0);
        buildFace();
        repaint();
    }

    public void setBack(int p) {
        if (p>=GK && p<DEF) fondo  = so.gui.MainFrame.getImageIcon(so.Constants.DIRNAME_FACES + "/backGK.png");
        else if (p>=DEF && p<MID) fondo  = so.gui.MainFrame.getImageIcon(so.Constants.DIRNAME_FACES + "/backDEF.png");
        else if (p>=MID && p<ATT) fondo  = so.gui.MainFrame.getImageIcon(so.Constants.DIRNAME_FACES + "/backMID.png");
        else if (p>=ATT) fondo  = so.gui.MainFrame.getImageIcon(so.Constants.DIRNAME_FACES + "/backATT.png");
        else fondo = so.gui.MainFrame.getImageIcon(so.Constants.DIRNAME_FACES + "/back.png");
    }

    public void setCode(int c, int country) {
        code = c;
        if (code > 0) {
            /* race */
            int race = getRaceForCountry(country);

            switch (race) {
            case 5:
                loadIndoArabicFace();
                break;
            case 4:
                loadEasternAsianFace();
                break;
            case 3:
                loadAfricanFace();
                break;
            case 2:
                loadAmerindianFace();
                break;
            case 1:
            default:
                loadCaucasianFace();
            }
        }
        buildFace();
    }

    private void buildFace() {
        Graphics2D g2d = playerFace.createGraphics();
        g2d.drawImage(fondo.getImage(), 0, 0, null);
        if (code==0) return;
        g2d.drawImage(cara.getImage(), 0, 0, null);
        if (barba != null) g2d.drawImage(barba.getImage(), 0, 0, null);
        g2d.drawImage(ojos.getImage(), 0, 0, null);
        g2d.drawImage(boca.getImage(), 0, 0, null);
        g2d.drawImage(nariz.getImage(), 0, 0, null);
    }

    /* ############################################################################### */
    private void loadCaucasianFace() {
        /* Face width */
        // bit 3
        int faFA = (code & 8)/8 + 1;
        /* complexion */
        // bit 2
        int faHU = (code & 4)/4 + 1;
        /* Hair Style */
        //int [] bitsHA = { 0,8,16 };
        int faHA = (getValFromBits(code, new int[]{0,8,16} )+faFA-1)%8 + 1;
        /* hair colour */
        //int [] bitsHC = { 4,17 };
        int faHC = getValFromBits(code, new int[]{4,17} )%3 + 1;
        /* beard shadow */
        // bit 1
        int faFH = (code & 2)/2 + 1;
        cara = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_BASE +
                                  "FA" + faFA + "HU" + faHU + "HA" + faHA + "HC" + faHC + ".gif");
        if (faFH == 1) barba = null;
        else barba = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_BEARDS + "FA" + faFA + ".gif");
        /* Eyes */
        //int [] bitsE = { 5,13,15 };
        int ey = (getValFromBits(code, new int[]{5,13,15} )+faFH-1)%8 + 1;
        ojos = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_EYES + "EY" + ey + ".gif");

        /* Mouth */
        //int [] bitsM = { 7,11,14,9 };
        int _mo = (getValFromBits(code, new int[]{7,11,14,9} )) % 14;
        int moMO = 0;
        int moHC = 0;
        if (_mo < 5) {
            moMO = _mo + 4;
            moHC = 0;
        }
        else {
            _mo -= 5;
            moMO = _mo/3 + 1;
            moHC = _mo%3 + 1;
        }
        boca = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_MOUTHS + "MO" + moMO + "HC" + moHC + ".gif");

        /* Nose */
        //int [] bitsN = { 6,12,10 };
        int no = (getValFromBits(code, new int[]{6,12,10} )+faHU+(code&1)-2)%8 + 1;
        nariz = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_NOSES + "NO" + no + ".gif");
    }

    /* ############################################################################### */
    private void loadAmerindianFace() {
        /* Face width */
        // bit 3
        int faFA = (code & 8)/8 + 1;
        /* Hair Style */
        //int [] bitsHA = { 8,0,16 };
        int faHA = (getValFromBits(code, new int[]{8,0,16} )+faFA-1)%8 + 1;
        /* complexion */
        /* hair colour */
        // bit 2
        int faHU = (code & 4)/4;
        // bit 4
        int faHC1 = (code & 16)/16;
        // bit 17
        int faHC2 = (code & 131072)/131072;
        int faHQ = ((faHU+faHC1)%2 + faHC2)%2 + 1;
        /* beard shadow */
        // bit 1
        int faFH = (code & 2)/2 + 1;
        cara = so.gui.MainFrame.getImageIcon(getDirName(2) + SUBDIR_BASE +
                                  "FA" + faFA + "HU" + faHQ + "HA" + faHA + "HC" + faHQ + ".gif");
        if (faFH == 1) barba = null;
        else barba = so.gui.MainFrame.getImageIcon(getDirName(2) + SUBDIR_BEARDS + "FA" + faFA + ".gif");
        /* Eyes */
        //int [] bitsE = { 5,13,15 };
        int ey = getValFromBits(code, new int[]{5,13,15} ) + 1;
        ojos = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_EYES + "EY" + ey + ".gif");

        /* Mouth */
        //int [] bitsM = { 7,11,14,9 };
        int _mo = (getValFromBits(code, new int[]{7,11,14,9} )+faHU) % 11;
        int moMO = 0;
        int moHC = 0;
        if (_mo < 5) {
            moMO = _mo + 4;
            moHC = 0;
        }
        else {
            _mo -= 5;
            moMO = _mo/2 + 1;
            moHC = _mo%2 + 1;
        }
        boca = so.gui.MainFrame.getImageIcon(getDirName(2) + SUBDIR_MOUTHS + "MO" + moMO + "HC" + moHC + ".gif");

        /* Nose */
        //int [] bitsN = { 6,12,10 };
        int no = getValFromBits(code, new int[]{6,12,10} ) + 1;
        nariz = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_NOSES + "NO" + no + ".gif");
    }
    /* ############################################################################### */
    private void loadAfricanFace() {
        /* Face width */
        // bit 3
        int faFA = (code & 8)/8 + 1;
        /* complexion */
        // bit 2
        int faHU = (code & 4)/4 + 1;
        /* Hair Style */
        //int [] bitsHA = { 0,8,5 };
        int faHA = (getValFromBits(code, new int[]{0,8,5} )+faFA-1)%8 + 1;
        /* hair colour */
        //int [] bitsHC = { 4,17 };
        //int faHC = getValFromBits(code, bitsHC)%3 + 1;
        /* beard shadow */
        // bit 1
        int faFH = (code & 2)/2 + 1;
        cara = so.gui.MainFrame.getImageIcon(getDirName(3) + SUBDIR_BASE +
                                  "FA" + faFA + "HU" + faHU + "HA" + faHA + "HC1FH" + faFH + ".gif");
        barba = null;
        /* Eyes */
        //int [] bitsE = { 4,13,15 };
        int ey = getValFromBits(code, new int[]{4,13,15} ) + 1;
        ojos = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_EYES + "EY" + ey + ".gif");

        /* Mouth */
        //int [] bitsM = { 7,11,14,9 };
        int _mo = (getValFromBits(code, new int[]{7,11,14,9} )+faHU-1) % 8;
        int moMO = _mo + 1;
        int moHC = (moMO>3)?0:1;
        boca = so.gui.MainFrame.getImageIcon(getDirName(3) + SUBDIR_MOUTHS + "MO" + moMO + "HC" + moHC + ".gif");

        /* Nose */
        //int [] bitsN = { 6,12,10 };
        int no = getValFromBits(code, new int[]{6,12,10} ) + 1;
        nariz = so.gui.MainFrame.getImageIcon(getDirName(1) + SUBDIR_NOSES + "NO" + no + ".gif");
    }
    /* ############################################################################### */
    private void loadEasternAsianFace() {
        /* Face width */
        // bit 3
        int faFA = (code & 8)/8 + 1;
        /* complexion */
        // bit 2
        int faHU = (code & 4)/4 + 1;
        /* Hair Style */
        //int [] bitsHA = { 0,8,7 };
        int faHA = (getValFromBits(code, new int[]{0,8,7} )+faFA-1)%8 + 1;
        /* hair colour */
        //int [] bitsHC = { 4,17 };
        //int faHC = getValFromBits(code, bitsHC)%3 + 1;
        int faHC = 3 - faHU;
        /* beard shadow */
        // bit 1
        //int faFH = (code & 2)/2 + 1;
        cara = so.gui.MainFrame.getImageIcon(getDirName(4) + SUBDIR_BASE +
                                  "FA" + faFA + "HU" + faHU + "HA" + faHA + "HC" + faHC + "FH1.gif");
        barba = null;
        /* Eyes */
        //int [] bitsE = { 5,13,12 };
        int ey = getValFromBits(code, new int[]{5,13,12} ) + 1;
        ojos = so.gui.MainFrame.getImageIcon(getDirName(4) + SUBDIR_EYES + "EY" + ey + ".gif");

        /* Mouth */
        //int [] bitsM = { 4,11,14,9 };
        int _mo = (getValFromBits(code, new int[]{4,11,14,9} )+faHU-1) % 8;
        int moMO = _mo + 1;
        int moHC = (moMO>3)?0:1;
        boca = so.gui.MainFrame.getImageIcon(getDirName(4) + SUBDIR_MOUTHS + "MO" + moMO + "HC" + moHC + ".gif");

        /* Nose */
        //int [] bitsN = { 6,1,10 };
        int no = getValFromBits(code, new int[]{6,1,10} ) + 1;
        nariz = so.gui.MainFrame.getImageIcon(getDirName(4) + SUBDIR_NOSES + "NO" + no + ".gif");
    }
    /* ############################################################################### */
    private void loadIndoArabicFace() {
        /* Face width */
        // bit 3
        int faFA = (code & 8)/8 + 1;
        /* complexion */
        // bit 2
        int faHU = (code & 4)/4 + 1;
        /* Hair Style */
        //int [] bitsHA = { 0,8,6 };
        int faHA = (getValFromBits(code, new int[]{0,8,6} )+faFA-1)%8 + 1;
        /* hair colour */
        //int [] bitsHC = { 6,17 };
        //int faHC = getValFromBits(code, bitsHC)%3 + 1;
        /* beard shadow */
        // bit 1
        int faFH = (code & 2)/2 + 1;
        cara = so.gui.MainFrame.getImageIcon(getDirName(5) + SUBDIR_BASE +
                                  "FA" + faFA + "HU" + faHU + "HA" + faHA + "HC1FH" + faFH + ".gif");
        barba = null;
        /* Eyes */
        //int [] bitsE = { 5,13,15 };
        int ey = getValFromBits(code, new int[]{5,13,15} ) + 1;
        ojos = so.gui.MainFrame.getImageIcon(getDirName(4) + SUBDIR_EYES + "EY" + ey + ".gif");

        /* Mouth */
        //int [] bitsM = { 7,11,14,9 };
        int _mo = (getValFromBits(code, new int[]{7,11,14,9} )+faHU-1) % 8;
        int moMO = _mo + 1;
        int moHC = (moMO>3)?0:1;
        boca = so.gui.MainFrame.getImageIcon(getDirName(5) + SUBDIR_MOUTHS + "MO" + moMO + "HC" + moHC + ".gif");

        /* Nose */
        //int [] bitsN = { 4,12,10 };
        int no = getValFromBits(code, new int[]{4,12,10} ) + 1;
        nariz = so.gui.MainFrame.getImageIcon(getDirName(4) + SUBDIR_NOSES + "NO" + no + ".gif");
    }
    /* ############################################################################### */

    private int getValFromBits(int c, int [] bits) {
        for (int i=0; i<bits.length; i++) bits[i] = (c & (int)Math.pow(2, bits[i]));
        int res = 0;
        for (int i=0; i<bits.length; i++) if (bits[i]>0) res += (int)Math.pow(2, i);
        return res;
    }

    private String getDirName(int race) {
        return so.Constants.DIRNAME_FACES + '/' + race;
    }

    private int getRaceForCountry(int country) {
        int mainEthnia = 1;
        int secondEthnia = -1;
        switch(country) {
        /* Caucásicos */
        case 42: //Chile
            secondEthnia = 2;
        case 71: //Andorra
        case 6: //Australia
        case 46: //Belarus
        case 28: //België
        case 47: //Bosna i Hercegovina
        case 54: //Bulgaria
        case 5: //Canada
        case 13: //Ceská republika
        case 40: //Cymru
        case 29: //Danmark
        case 15: //Deutschland
        case 33: //Eesti
        case 3: //England
        case 17: //España
        case 16: //France
        case 66: //Hayastan
        case 49: //Hellas
        case 34: //Hrvatska
        case 32: //Ireland
        case 63: //Ísland
        case 52: //Israel
        case 10: //Italia
        case 23: //Lietuva
        case 8: //Magyarország
        case 56: //Makedonija
        case 67: //Malta
        case 69: //Moldova
        case 12: //Nederland
        case 2: //New Zealand
        case 27: //Norge
        case 30: //Österreich
        case 1: //Polska
        case 20: //Portugal
        case 9: //România
        case 43: //Rossiya
        case 24: //Schweiz 
        case 7: //Scotland
        case 53: //Shqipëria
        case 26: //Slovenija
        case 14: //Slovensko
        case 39: //Srbija i Crna Gora
        case 25: //Suomi
        case 31: //Sverige
        case 4: //USA
            mainEthnia = 1;
            break;
        /* Amerindios */
        case 19: //Argentina
            secondEthnia = 1;
        case 57: //Bolivia
        case 36: //Colombia
            if (country == 36) secondEthnia = 3;
        case 58: //Ecuador
            if (country == 58) secondEthnia = 3;
        case 68: //Honduras
        case 18: //México
        case 37: //Perú
        case 44: //Venezuela
        case 35: //Uruguay
            mainEthnia = 2;
            break;
        /* Afros */
        case 70: //Costa Rica
        case 21: //Brasil
            if (country == 21) secondEthnia = 1;
        case 38: //Nigeria
        case 61: //República Dominicana
        case 22: //South Africa
            mainEthnia = 3;
            break;
        /* Asiatico-Orientales */
        case 65: //Indonesia
        case 60: //Hong Kong
        case 59: //Malaysia
        case 62: //Nippon
            mainEthnia = 4;
            break;
        /* Indo-Arabigo */
        case 51: //India
        case 41: //Türkiye
            mainEthnia = 5;
            break;
        default:
        }
        if (secondEthnia == -1) secondEthnia = mainEthnia;
        int race = mainEthnia - 1;
        switch(code % 27) {
        case 5:
            race += 1;
            break;
        case 12:
            race += 2;
            break;
        case 20:
            race += 3;
            break;
        case 25:
            race += 4;
            break;
        case 3:
        case 7:
        case 16:
        case 23:
            race = secondEthnia - 1;
            break;
        default:
        }
        race = race%5 + 1;
        return race;
    }

}
