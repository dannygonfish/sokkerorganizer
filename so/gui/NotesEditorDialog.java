package so.gui;

import static so.Constants.Labels.*;
import so.util.Dialog;
import so.data.Noteable;
import javax.swing.*;
import javax.swing.text.*;

public class NotesEditorDialog extends Dialog {
    private Noteable noteable;
    //private PlayerProfile player;
    private JEditorPane textPane;
    private JOptionPane optionPane;

//     public NotesEditorDialog(TeamProfile tp) {
//         super(MainFrame.getLabel(TXT_MANAGER_NOTES) + " : " + pp.getFullName(), false);

//     }

    public NotesEditorDialog(Noteable ntble) {
        super(MainFrame.getLabel(TXT_MANAGER_NOTES) + " : " + ntble.getNotesTitle(), false);
        this.noteable = ntble;
        textPane = new JEditorPane("text/html", noteable.getManagerNotes());

        JPopupMenu popup = new JPopupMenu();
        popup.add( createMenuItem(new DefaultEditorKit.CutAction(), MainFrame.getLabel(TXT_CUT)) );
        popup.add( createMenuItem(new DefaultEditorKit.CopyAction(), MainFrame.getLabel(TXT_COPY)) );
        popup.add( createMenuItem(new DefaultEditorKit.PasteAction(), MainFrame.getLabel(TXT_PASTE)) );
        popup.addSeparator();
        popup.add( createMenuItem(new StyledEditorKit.BoldAction(), MainFrame.getLabel(TXT_BOLD)) );
        popup.add( createMenuItem(new StyledEditorKit.ItalicAction(), MainFrame.getLabel(TXT_ITALIC)) );
        popup.add( createMenuItem(new StyledEditorKit.UnderlineAction(), MainFrame.getLabel(TXT_UNDERLINE)) );
        textPane.setComponentPopupMenu(popup);

        optionPane = new JOptionPane(new JScrollPane(textPane), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        setContentPane(optionPane);        

        optionPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    String prop = e.getPropertyName();
                    if (NotesEditorDialog.this.isVisible() && (e.getSource() == optionPane) &&
                        (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        /* If you were going to check something before closing the window, you'd do it here. */
                        NotesEditorDialog.super.setVisible(false);
                        int value = ((Integer)optionPane.getValue()).intValue();
                        if (value == JOptionPane.OK_OPTION) {
                            noteable.setManagerNotes(textPane.getText());
                            MainFrame.refreshPlayerNotesIcon();
                        }
                    }
                }
            });

        setDefaultCloseOperation(Dialog.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (isVisible()) return;
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            textPane.setText(noteable.getManagerNotes());
            pack();
        }
        super.setVisible(visible);
    }

    protected JMenuItem createMenuItem(Action action, String name) {
        action.putValue(Action.NAME, name);
        //action.putValue(Action.SHORT_DESCRIPTION, "ToolTip!!");
        //action.putValue(Action.SMALL_ICON, UIManager.getIcon("OptionPane.questionIcon"));
        return new JMenuItem(action);
    }
}
