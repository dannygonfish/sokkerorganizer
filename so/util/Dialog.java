package so.util;

import javax.swing.*;

public class Dialog extends JDialog {

    public Dialog(String title, boolean modal) {
        super(so.gui.MainFrame.getOwnerForDialog(), title, modal);
        //if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            //System.out.println("supWind.Dec.: " + supportsWindowDecorations);
            if (supportsWindowDecorations) {
                super.setUndecorated(true);
                getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
                //getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
            }
            //}
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


//     private static int styleFromMessageType(int messageType) {
//         switch (messageType) {
//         case ERROR_MESSAGE:
//             return JRootPane.ERROR_DIALOG;
//         case QUESTION_MESSAGE:
//             return JRootPane.QUESTION_DIALOG;
//         case WARNING_MESSAGE:
//             return JRootPane.WARNING_DIALOG;
//         case INFORMATION_MESSAGE:
//             return JRootPane.INFORMATION_DIALOG;
//         case PLAIN_MESSAGE:
//         default:
//             return JRootPane.PLAIN_DIALOG;
//         }
//     }

}
