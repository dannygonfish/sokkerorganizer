package so.util;
/**
 * DebugFrame.java
 *
 * @author Daniel González Fisher
 */

import javax.swing.*;


@SuppressWarnings("serial")
public class DebugFrame extends JFrame {
    private JTextArea jta;

    public DebugFrame() {
        this(true);
    }
    public DebugFrame(boolean visible) {
        super("Error");
        if (visible) jta = new JTextArea("Please report this error via email to sokker@cybercafedelsur.cl\nor SK-mail to danny\n\nSokker Organizer " + (so.So.JAVAWS?"ONLINE v":" v")+ so.So.getVersion() + "\n",30,80);
        else jta = new JTextArea("",30,80);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(new JScrollPane(jta));
        pack();
        setVisible(visible);
    }
    public DebugFrame(String s) {
        this();
        setText(s);
    }
    public DebugFrame(Exception e) {
        this();
        setError(e);
    }

    public void setText(String t) {
        jta.append(t);
    }
    public void append(String t) {
        jta.append(t);
        if (!isVisible()) setVisible(true);
        jta.setCaretPosition( jta.getText().length() );
    }
    public void setError(Exception e) {
        if (e==null) return;
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        jta.append(sw.toString());
        pw.close();
    }

}
