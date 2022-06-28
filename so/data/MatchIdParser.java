package so.data;
/**
 * MatchIdParser.java
 *
 * @author Daniel González Fisher
 */

import static so.Constants.Labels.*;
import so.text.LabelManager;
import so.util.Utils;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.*;

public class MatchIdParser implements java.beans.PropertyChangeListener {
    private static final String VALUE = "value";
    private static final String MIME_HTML = "text/html";
    private static final String MATCHID_SEARCH_STRING = "matchID=";

    private LabelManager labelManager;
    private MutableList list;
    private JFormattedTextField textField;

    public MatchIdParser(LabelManager lm) {
        labelManager = lm;
        list = new MutableList();
        textField = new JFormattedTextField(Integer.valueOf(0));
        textField.addPropertyChangeListener(VALUE, this);
        textField.setToolTipText(lm.getLabel(TXT_TT_TYPE_MATCHIDS_HERE));
        list.setToolTipText(lm.getLabel(TXT_TT_DRAG_MATCHES_HERE));
    }

    public List<Integer> getMatchesList() {
        textField.setText("");
        //JPanel panel = new JPanel();
        Box panel = new Box(BoxLayout.Y_AXIS);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(250, 160));
        panel.add(textField);
        panel.add(sp);
        int ret = JOptionPane.showConfirmDialog(so.gui.MainFrame.getOwnerForDialog(), panel,
                                                labelManager.getLabel(TXT_INPUT_MATCH_IDS), JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.PLAIN_MESSAGE);
        if (ret==JOptionPane.CLOSED_OPTION || ret==JOptionPane.CANCEL_OPTION) return null;
        return list.getIntegerList();
    }

    /* ============================================================ */
    private static class MutableList extends JList {
        private DefaultListModel model;

        public MutableList() {
            super(new DefaultListModel());
            model = (DefaultListModel)getModel();
            setTransferHandler(new HTMLTransferHandler());
            setPrototypeCellValue(Integer.valueOf(888888888));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setLayoutOrientation(JList.VERTICAL_WRAP);
            setVisibleRowCount(-1);
            //setPreferredSize(new Dimension(256,160));
        }

        public void addElement(Object obj) {
            if (!model.contains(obj)) model.addElement(obj);
        }

        public List<Integer> getIntegerList() {
            ArrayList<Integer> mids = new ArrayList<Integer>();
            for (int i=0; i<model.size(); i++) {
                if (model.elementAt(i) instanceof Integer) mids.add((Integer)model.elementAt(i));
            }
            return mids;
        }

    }   

    /* ============================================================ */
    private static class HTMLTransferHandler extends TransferHandler {

        public HTMLTransferHandler() {
            super();
        }

        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            for (DataFlavor df : transferFlavors) {
                if (df.isMimeTypeEqual(MIME_HTML)) return true;
            }
            return false;
        }
        public boolean importData(JComponent comp, Transferable t) {
            MutableList ml = (MutableList)comp;

            if (!canImport(comp, t.getTransferDataFlavors())) return false;

            try {
                String str = (String)t.getTransferData(new DataFlavor("text/html; class=java.lang.String"));
                importMatchIdListFromHTML(str, ml);
                return true;
            } catch (UnsupportedFlavorException ufe) {
                //System.out.println("importData: unsupported data flavor");
            } catch (java.io.IOException ioe) {
                //System.out.println("importData: I/O exception");
            } catch (ClassNotFoundException cnfe) {
                //System.out.println("importData: Class not found exception");
            }
            return super.importData(comp, t);
        }

        public int importMatchIdListFromHTML(String html, MutableList ml) {
            int c = 0;
            for (int idx=0; (idx = html.indexOf(MATCHID_SEARCH_STRING, idx)) > -1; c++) {
                idx += MATCHID_SEARCH_STRING.length();
                int id = Utils.getIntFromString(html, idx);
                if (id > 0) ml.addElement(Integer.valueOf(id));
            }
            ml.revalidate();
            return c;
        }

    }

    /* interface PropertyChangeListener */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(VALUE)) {
            if (evt.getSource().equals(textField)) {
                Integer mid = (Integer)textField.getValue();
                if (mid>0) list.addElement(mid);
                textField.setText("");
            }
        }
    }

}
