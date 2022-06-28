package so.gui;

import so.So;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.RenderingHints;
import java.util.Calendar;

public final class LoadingFrame extends javax.swing.JFrame {
    private JLabel loadingText;

    public LoadingFrame(String title) {
        super(title);
        setUndecorated(true);
        java.net.URL url = null;
        //Calendar cal = Calendar.getInstance();
        //if (cal.get(Calendar.MONTH)==Calendar.DECEMBER && cal.get(Calendar.DAY_OF_MONTH)==25) url = So.class.getResource( '/' + FILENAME_IMG_SPLASH_XMAS );
        //else if (cal.get(Calendar.MONTH)==Calendar.JANUARY && cal.get(Calendar.DAY_OF_MONTH)==1) url = So.class.getResource( '/' + FILENAME_IMG_SPLASH_NYEAR );
        //else
        url = So.class.getResource( '/' + so.Constants.FILENAME_IMG_SPLASH );
        ImageIcon splash = null;
        if (url == null) splash = new ImageIcon();
        else splash = new ImageIcon(url);
        final BufferedImage buffImg = new BufferedImage(480, 160, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = buffImg.createGraphics(); 
        g2d.drawImage(splash.getImage(), 0, 0, null);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawString("v" + So.getVersion(), 360, 140);
        loadingText = new JLabel("Loading SO...");
        loadingText.setOpaque(false);
        loadingText.setForeground(java.awt.Color.WHITE);
        loadingText.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        setContentPane(new javax.swing.JPanel(new java.awt.BorderLayout()) {
                public void paintComponent(java.awt.Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(buffImg, 0, 0, null);
                }

            } );

        add(loadingText, java.awt.BorderLayout.SOUTH);
        setIconImage(So.loadSoImage());
        setSize(480, 160);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void displayLoadingMessage(String msg) {
        loadingText.setText(msg);
    }

}

// public final class LoadingFrame extends java.awt.Frame {
//     private JLabel loadingText;
//     private BufferedImage buffImg;

//     public LoadingFrame(String title) {
//         super(title);
//         setUndecorated(true);
//         java.net.URL url = null;
//         //Calendar cal = Calendar.getInstance();
//         //if (cal.get(Calendar.MONTH)==Calendar.DECEMBER && cal.get(Calendar.DAY_OF_MONTH)==25) url = So.class.getResource( '/' + FILENAME_IMG_SPLASH_XMAS );
//         //else if (cal.get(Calendar.MONTH)==Calendar.JANUARY && cal.get(Calendar.DAY_OF_MONTH)==1) url = So.class.getResource( '/' + FILENAME_IMG_SPLASH_NYEAR );
//         //else
//         url = So.class.getResource( '/' + so.Constants.FILENAME_IMG_SPLASH );
//         ImageIcon splash = null;
//         if (url == null) splash = new ImageIcon();
//         else splash = new ImageIcon(url);
//         buffImg = new BufferedImage(480, 160, BufferedImage.TYPE_INT_ARGB);
//         java.awt.Graphics2D g2d = buffImg.createGraphics(); 
//         g2d.drawImage(splash.getImage(), 0, 0, null);
//         g2d.setColor(java.awt.Color.WHITE);
//         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//         g2d.drawString("v" + So.getVersion(), 360, 140);
//         //JLabel loadingImage = new JLabel( new ImageIcon(buffImg) );
//         loadingText = new JLabel("Loading SO...");
//         loadingText.setOpaque(false);
//         loadingText.setForeground(java.awt.Color.WHITE);
//         loadingText.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
//         //add(loadingImage);
//         add(loadingText, java.awt.BorderLayout.SOUTH);
//         setIconImage(So.loadSoImage());
//         //pack();
//         setSize(480, 160);
//         setResizable(false);
//         setLocationRelativeTo(null);
//         setVisible(true);
//     }

//     public void paint(java.awt.Graphics g) {
//         g.drawImage(buffImg, 0, 0, null);
//         super.paint(g);
//     }

//     public void displayLoadingMessage(String msg) {
//         loadingText.setText(msg);
//     }

// }
