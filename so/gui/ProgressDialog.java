package so.gui;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import java.awt.Cursor;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class ProgressDialog extends JDialog implements KeyListener {
    private JProgressBar bar;

    public ProgressDialog(java.awt.Frame frame) {
        super(frame, true); // modal
        //super("", false, false, false, false);

        bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(true);
        bar.setString("XXXXXWWWWWXXXXXWWWWWXXXXXWWWWWXXXXX");
        add( bar );
        setResizable(false);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        addKeyListener(this);
        pack();
        setLocationRelativeTo(frame);
        bar.setString("");
        bar.setIndeterminate(false);
    }

    public void startProgress(int max) {
        bar.setValue(0);
        bar.setMaximum(max);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Runnable showProgressBar = new Runnable() {
                public void run() {
                    setVisible(true);
                }
            };
        //new Thread(showProgressBar).start();
        javax.swing.SwingUtilities.invokeLater(showProgressBar);
    }

    public void setBarString(String s) {
        bar.setString(s);
        bar.setValue( bar.getValue()+1 );
    }

    public void endProgress() {
        setCursor(null);
        setVisible(false);
        bar.setString("");
    }

    public void advanceProgress() {
        bar.setValue( bar.getValue()+1 );
    }
    public void advanceProgress(int skip) {
        bar.setValue( bar.getValue()+skip );
    }
    public void extendMaximumBy(int addToMax) {
        if (addToMax<1) return;
        bar.setMaximum(bar.getMaximum() + addToMax);
    }

    // ********************* interface KeyListener ***************************
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.isAltDown()) {
            if (e.getKeyCode()==KeyEvent.VK_SPACE) endProgress();
        }
    }
    public void keyReleased(KeyEvent e) { ; }
    public void keyTyped(KeyEvent e) { ; }

}
